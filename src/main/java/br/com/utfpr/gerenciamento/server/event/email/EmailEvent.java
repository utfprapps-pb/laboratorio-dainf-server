package br.com.utfpr.gerenciamento.server.event.email;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Evento base para envio de emails após commit de transação.
 *
 * <p>Este evento abstrato fornece a infraestrutura comum para todos os eventos de email no sistema.
 * Os eventos concretos devem:
 *
 * <ul>
 *   <li>Estender esta classe
 *   <li>Armazenar apenas IDs de entidades (não entidades completas)
 *   <li>Conter dados necessários para construir o template de email
 * </ul>
 *
 * <p><b>Padrão de Uso:</b>
 *
 * <pre>
 * // 1. Em um service @Transactional
 * Emprestimo saved = emprestimoRepository.save(emprestimo);
 * eventPublisher.publishEvent(new EmprestimoFinalizadoEvent(saved.getId()));
 *
 * // 2. Evento é enfileirado mas não disparado ainda (transaction em andamento)
 *
 * // 3. Transaction comita com sucesso
 *
 * // 4. EmailEventListener recebe o evento APÓS commit
 *
 * // 5. Listener carrega entidade em nova transação e envia email
 * </pre>
 *
 * <p><b>Benefícios:</b>
 *
 * <ul>
 *   <li>Email enviado apenas se transação commit com sucesso
 *   <li>Falha no email não causa rollback do negócio
 *   <li>Sem risco de LazyInitializationException (nova transação)
 *   <li>Fácil adicionar @Async ou message queue no futuro
 * </ul>
 *
 * @author Rodrigo Izidoro
 * @since 2025-10-22
 */
@Getter
public abstract class EmailEvent extends ApplicationEvent {

  private final String recipient;
  private final String subject;
  private final String templateName;

  /**
   * Construtor do evento de email.
   *
   * @param source Objeto que publicou o evento (geralmente this do service)
   * @param recipient Email do destinatário
   * @param subject Assunto do email
   * @param templateName Nome do template FreeMarker (sem extensão)
   */
  protected EmailEvent(Object source, String recipient, String subject, String templateName) {
    super(source);
    this.recipient = recipient;
    this.subject = subject;
    this.templateName = templateName;
  }

  // Note: Template data preparation is handled directly by EmailEventListener
  // using type-specific logic. Events only carry essential data (IDs, flags).
}
