package br.com.utfpr.gerenciamento.server.event.email;

import br.com.utfpr.gerenciamento.server.event.emprestimo.EmprestimoDevolvidoEvent;
import br.com.utfpr.gerenciamento.server.event.emprestimo.EmprestimoFinalizadoEvent;
import br.com.utfpr.gerenciamento.server.event.emprestimo.EmprestimoPrazoAlteradoEvent;
import br.com.utfpr.gerenciamento.server.event.emprestimo.EmprestimoPrazoProximoEvent;
import br.com.utfpr.gerenciamento.server.event.item.EstoqueMinNotificacaoEvent;
import br.com.utfpr.gerenciamento.server.event.nadaConsta.NadaConstaEmitidoEvent;
import br.com.utfpr.gerenciamento.server.event.nadaConsta.NadaConstaPendenciasEvent;
import br.com.utfpr.gerenciamento.server.event.usuario.UsuarioCriadoEvent;
import br.com.utfpr.gerenciamento.server.exception.EntityNotFoundException;
import br.com.utfpr.gerenciamento.server.mapper.EmprestimoTemplateMapper;
import br.com.utfpr.gerenciamento.server.model.Email;
import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.repository.EmprestimoRepository;
import br.com.utfpr.gerenciamento.server.repository.UsuarioRepository;
import br.com.utfpr.gerenciamento.server.service.EmailService;
import br.com.utfpr.gerenciamento.server.service.RelatorioService;
import br.com.utfpr.gerenciamento.server.util.EmailUtils;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JasperExportManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listener centralizado para processar eventos de email após commit de transação.
 *
 * <p>Este componente recebe todos os eventos {@link EmailEvent} publicados no sistema e os processa
 * de forma assíncrona APÓS o commit da transação que os gerou.
 *
 * <p><b>Características:</b>
 *
 * <ul>
 *   <li>Executa em NOVA transação (seguro para lazy loading)
 *   <li>Falhas no envio de email NÃO causam rollback do negócio
 *   <li>Email enviado apenas se transação original commitou com sucesso
 *   <li>Logging de falhas para monitoramento
 * </ul>
 *
 * <p><b>Fluxo de Execução:</b>
 *
 * <pre>
 * 1. Service publica EmailEvent dentro de @Transactional
 * 2. Spring enfileira o evento mas NÃO dispara listener ainda
 * 3. Transaction comita com sucesso
 * 4. Spring dispara este listener APÓS commit
 * 5. Listener prepara template data (em nova transação)
 * 6. EmailService envia o email
 * 7. Se falhar, loga erro mas NÃO afeta transação original
 * </pre>
 *
 * <p><b>Extensibilidade:</b>
 *
 * <p>Para adicionar processamento assíncrono no futuro: 1. Adicione @Async na classe 2. Configure
 * ThreadPoolTaskExecutor em @Configuration 3. Emails serão processados em threads separadas
 *
 * @author Rodrigo Izidoro
 * @since 2025-10-22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailEventListener {

  /** Timeout em segundos para transação de processamento de email. */
  private static final int EMAIL_TRANSACTION_TIMEOUT_SECONDS = 30;

  /** ID do relatório Jasper de estoque mínimo (itens que atingiram quantidade mínima). */
  private static final long RELATORIO_ESTOQUE_MINIMO_ID = 6L;

  /**
   * Endereço de email remetente (conta SMTP autenticada).
   *
   * <p>Usa spring.mail.username para garantir conformidade SPF/DMARC com Gmail/SMTP autenticado.
   */
  @Value("${spring.mail.username}")
  private String emailFrom;

  /**
   * URL base do frontend para construir links de confirmação.
   *
   * <p>Exemplo: https://lab.utfpr.edu.br
   */
  @Value("${utfpr.front.url}")
  private String frontBaseUrl;

  private final EmailService emailService;
  private final EmprestimoRepository emprestimoRepository;
  private final EmprestimoTemplateMapper templateMapper;
  private final RelatorioService relatorioService;
  private final UsuarioRepository usuarioRepository;

  /**
   * Processa eventos de email após commit da transação de forma assíncrona com retry automático.
   *
   * <p>Este metodo é chamado automaticamente pelo Spring quando qualquer evento do tipo {@link
   * EmailEvent} é publicado E a transação que o publicou fez commit com sucesso.
   *
   * <p><b>IMPORTANTE:</b> Se a transação original fizer rollback, este metodo NÃO será chamado.
   *
   * <p><b>ASSÍNCRONO:</b> Executa em thread pool dedicado (emailTaskExecutor) para não bloquear
   * thread da requisição original.
   *
   * <p><b>RETRY AUTOMÁTICO:</b> Em caso de falhas transientes de email (SMTP timeout, connection
   * refused, etc.), o metodo será retentado automaticamente até 3 vezes com backoff exponencial
   * (2s, 4s, 8s).
   *
   * @param event Evento de email contendo dados para envio
   */
  @Retryable(
      retryFor = {MailException.class},
      backoff = @Backoff(delay = 2000, multiplier = 2))
  @Async("emailTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Transactional(
      readOnly = true,
      propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW,
      timeout = EMAIL_TRANSACTION_TIMEOUT_SECONDS)
  public void handleEmailEvent(EmailEvent event) {
    Object templateData = null;
    try {
      templateData = prepareTemplateDataForEvent(event);
    } catch (EntityNotFoundException | IllegalArgumentException e) {
      log.error(
          "Erro não-retryável ao preparar dados do template para email {} para {}: {}",
          event.getSubject(),
          EmailUtils.maskEmail(event.getRecipient()),
          e.getMessage(),
          e);
      return;
    }
    String cc = null;
    if (event instanceof NadaConstaEmitidoEvent nadaConstaEmitidoEvent) {
      cc = nadaConstaEmitidoEvent.getCc();
    }
    processEmailWithTemplate(
        templateData, event.getRecipient(), event.getSubject(), event.getTemplateName(), cc);
  }

  /**
   * Processa eventos de notificação de estoque mínimo com relatório PDF anexado.
   *
   * <p>Este handler especializado processa eventos {@link EstoqueMinNotificacaoEvent} de forma
   * assíncrona APÓS commit da transação, gerando relatório Jasper e enviando email com anexo.
   *
   * <p><b>Características:</b>
   *
   * <ul>
   *   <li>✅ Relatório PDF gerado dinamicamente (Jasper Report ID 6)
   *   <li>✅ Email com anexo enviado de forma assíncrona
   *   <li>✅ Retry automático em caso de falhas transientes (MailException)
   *   <li>✅ Falhas não afetam transação de negócio original
   * </ul>
   *
   * @param event Evento de notificação de estoque mínimo
   */
  @Retryable(
      retryFor = {MailException.class},
      backoff = @Backoff(delay = 2000, multiplier = 2))
  @Async("emailTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Transactional(
      readOnly = true,
      propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW,
      timeout = EMAIL_TRANSACTION_TIMEOUT_SECONDS)
  public void handleEstoqueMinNotificacaoEvent(EstoqueMinNotificacaoEvent event) {
    try {
      byte[] reportPdf =
          JasperExportManager.exportReportToPdf(
              relatorioService.generateReport(RELATORIO_ESTOQUE_MINIMO_ID, null));
      String conteudo = emailService.buildTemplateEmail(null, event.getTemplateName());
      Email email =
          Email.builder()
              .para(event.getRecipient())
              .de(emailFrom)
              .titulo(event.getSubject())
              .conteudo(conteudo)
              .build();
      email.addFile("itensAtingiramEstoqueMin.pdf", reportPdf);
      processEmailWithAttachment(email, event.getRecipient(), event.getSubject());
    } catch (MailException e) {
      log.warn(
          "Falha temporária ao enviar notificação de estoque mínimo para {} (tentará novamente): {}",
          EmailUtils.maskEmail(event.getRecipient()),
          e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error(
          "Erro não-retryável ao processar notificação de estoque mínimo para {}: {}",
          EmailUtils.maskEmail(event.getRecipient()),
          e.getMessage(),
          e);
    }
  }

  // REMOVIDOS: Handlers duplicados para NadaConstaEmitidoEvent e NadaConstaPendenciasEvent

  /**
   * Helper to process email sending with template and logging, with retryable exception
   * propagation.
   */
  private void processEmailWithTemplate(
      Object templateData, String recipient, String subject, String templateName, String cc) {
    try {
      if (cc != null) {
        log.info(
            "Processando envio de email: {} para {} (CC: {})",
            subject,
            EmailUtils.maskEmail(recipient),
            EmailUtils.maskEmail(cc));
        emailService.sendEmailWithTemplate(templateData, recipient, subject, templateName, cc);
        log.info(
            "Email enviado com sucesso: {} para {} (CC: {})",
            subject,
            EmailUtils.maskEmail(recipient),
            EmailUtils.maskEmail(cc));
      } else {
        log.info(
            "Processando envio de email: {} para {}", subject, EmailUtils.maskEmail(recipient));
        emailService.sendEmailWithTemplate(templateData, recipient, subject, templateName);
        log.info("Email enviado com sucesso: {} para {}", subject, EmailUtils.maskEmail(recipient));
      }
    } catch (MailException e) {
      log.warn(
          "Falha temporária ao enviar email {} para {} (tentará novamente): {}",
          subject,
          EmailUtils.maskEmail(recipient),
          e.getMessage());
      throw e;
    } catch (EntityNotFoundException | IllegalArgumentException e) {
      log.error(
          "Erro não-retryável ao processar email {} para {}: {}",
          subject,
          EmailUtils.maskEmail(recipient),
          e.getMessage(),
          e);
    }
  }

  /**
   * Helper to process email sending with Email object and logging, with retryable exception
   * propagation.
   */
  private void processEmailWithAttachment(Email email, String recipient, String subject) {
    try {
      log.info("Processando envio de email com anexo para {}", EmailUtils.maskEmail(recipient));
      emailService.enviar(email);
      log.info("Email com anexo enviado com sucesso para {}", EmailUtils.maskEmail(recipient));
    } catch (MailException e) {
      log.warn(
          "Falha temporária ao enviar email com anexo para {} (tentará novamente): {}",
          EmailUtils.maskEmail(recipient),
          e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error(
          "Erro não-retryável ao processar email com anexo para {}: {}",
          EmailUtils.maskEmail(recipient),
          e.getMessage(),
          e);
    }
  }

  /**
   * Prepara dados do template baseado no tipo de evento.
   *
   * <p>Este método detecta o tipo específico do evento e carrega os dados necessários do banco em
   * uma NOVA transação (seguro para lazy loading).
   *
   * @param event Evento de email
   * @return Objeto template preparado para FreeMarker
   */
  private Object prepareTemplateDataForEvent(EmailEvent event) {
    // Pattern matching por tipo de evento
    if (event instanceof EmprestimoFinalizadoEvent emprestimoEvent) {
      return prepareEmprestimoTemplateData(emprestimoEvent.getEmprestimoId());
    } else if (event instanceof EmprestimoDevolvidoEvent devolvidoEvent) {
      return prepareEmprestimoTemplateData(devolvidoEvent.getEmprestimoId());
    } else if (event instanceof EmprestimoPrazoAlteradoEvent prazoEvent) {
      return prepareEmprestimoTemplateData(prazoEvent.getEmprestimoId());
    } else if (event instanceof EmprestimoPrazoProximoEvent prazoProximoEvent) {
      return prepareEmprestimoTemplateData(prazoProximoEvent.getEmprestimoId());
    } else if (event instanceof EstoqueMinNotificacaoEvent) {
      // Evento de estoque mínimo não requer template data (usa apenas null para template simples)
      return null;
    } else if (event instanceof UsuarioCriadoEvent usuarioEvent) {
      return prepareUsuarioTemplateData(usuarioEvent);
    } else if (event instanceof NadaConstaEmitidoEvent nadaConstaEmitidoEvent) {
      return nadaConstaEmitidoEvent.getTemplateData();
    } else if (event instanceof NadaConstaPendenciasEvent nadaConstaPendenciasEvent) {
      return nadaConstaPendenciasEvent.getTemplateData();
    }

    // TODO: Adicionar outros tipos de eventos aqui (Reserva, etc.)

    throw new IllegalArgumentException("Tipo de evento não suportado: " + event.getClass());
  }

  /**
   * Carrega dados de um empréstimo e prepara template para email.
   *
   * <p>Este método é reutilizado por todos os eventos relacionados a Emprestimo, já que todos
   * precisam dos mesmos dados do template.
   *
   * <p><b>REFATORADO:</b> Usa EmprestimoTemplateMapper para mapeamento, seguindo SRP.
   *
   * @param emprestimoId ID do empréstimo
   * @return Map com dados do template
   */
  private Map<String, Object> prepareEmprestimoTemplateData(Long emprestimoId) {
    // Carrega empréstimo com @EntityGraph em NOVA transação (elimina N+1 queries)
    Emprestimo emprestimo =
        emprestimoRepository
            .findEmprestimoByIdWithRelations(emprestimoId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Empréstimo não encontrado para envio de email: " + emprestimoId));

    // Delega mapeamento para componente especializado
    return templateMapper.toTemplateData(emprestimo);
  }

  /**
   * Carrega dados de um usuário e prepara template para email de confirmação de cadastro.
   *
   * <p>Constrói os dados necessários para o template templateConfirmacaoCadastro, incluindo nome do
   * usuário e link de confirmação com código de verificação.
   *
   * @param event Evento de usuário criado contendo ID, email e código de verificação
   * @return Map com dados do template (nome, url)
   */
  private Map<String, Object> prepareUsuarioTemplateData(UsuarioCriadoEvent event) {
    // Carrega usuário em NOVA transação
    Usuario usuario =
        usuarioRepository
            .findById(event.getUsuarioId())
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Usuário não encontrado para envio de email: " + event.getUsuarioId()));

    // Constrói URL de confirmação
    String urlConfirmacao =
        String.format("%s/confirmar-email/%s", frontBaseUrl, event.getCodigoVerificacao());

    // Prepara dados do template FreeMarker
    Map<String, Object> templateData = new java.util.HashMap<>();
    templateData.put("usuario", usuario.getNome());
    templateData.put("url", urlConfirmacao);

    return templateData;
  }
}
