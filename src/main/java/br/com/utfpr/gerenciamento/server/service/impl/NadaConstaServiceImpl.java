package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.dto.NadaConstaResponseDto;
import br.com.utfpr.gerenciamento.server.enumeration.NadaConstaStatus;
import br.com.utfpr.gerenciamento.server.event.nadaConsta.NadaConstaEmitidoEvent;
import br.com.utfpr.gerenciamento.server.event.nadaConsta.NadaConstaPendenciasEvent;
import br.com.utfpr.gerenciamento.server.exception.NadaConstaException;
import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.model.NadaConsta;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.repository.NadaConstaRepository;
import br.com.utfpr.gerenciamento.server.service.EmprestimoService;
import br.com.utfpr.gerenciamento.server.service.NadaConstaService;
import br.com.utfpr.gerenciamento.server.service.SystemConfigService;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import br.com.utfpr.gerenciamento.server.util.EmailUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementação do serviço de operações relacionadas ao Nada Consta.
 *
 * <p>Responsável por gerenciar solicitações, verificação de pendências, invalidação e conversão de
 * entidades Nada Consta.
 */
@Slf4j
@Service
public class NadaConstaServiceImpl extends CrudServiceImpl<NadaConsta, Long, NadaConstaResponseDto>
    implements NadaConstaService {

  // Constantes extraídas das strings duplicadas
  private static final String USUARIO_NAO_ENCONTRADO =
      "Usuário não encontrado para o documento informado.";
  private static final String SOLICITACAO_EXISTENTE =
      "Já existe uma solicitação de Nada Consta em aberto ou concluída para este usuário.";
  private static final String EMAIL_NADA_CONSTA_INVALIDO =
      "Email de Nada Consta não enviado - email do sistema inválido: {}";
  private static final String EMAIL_PENDENCIAS_INVALIDO =
      "Email de pendências de Nada Consta não enviado - usuário sem email válido: {}";
  private static final String FORMAT_PT_BR = "dd 'de' MMMM 'de' yyyy";
  private static final String LOCALE_PT_BR = "pt-BR";
  private static final String NADA_CONSTA_NAO_ENCONTRADO = "Nada Consta não encontrado.";
  private static final String NADA_CONSTA_STATUS_PENDING =
      "Nada Consta não está com status PENDING.";
  private static final String USUARIO = "usuario";
  private static final String NADA_CONSTA = "nadaConsta";
  private static final String NOME_ALUNO = "nomeAluno";
  private static final String REGISTRO_ACADEMICO = "registroAcademico";
  private static final String DATA_FORMATADA = "dataFormatada";
  private static final String EMPRESTIMOS = "emprestimos";
  private static final String ITEM_NOME = "itemNome";

  private final NadaConstaRepository nadaConstaRepository;
  private final UsuarioService usuarioService;
  private final ModelMapper modelMapper;
  private final EmprestimoService emprestimoService;
  private final SystemConfigService systemConfigService;
  private final ApplicationEventPublisher eventPublisher;

  public NadaConstaServiceImpl(
      NadaConstaRepository nadaConstaRepository,
      UsuarioService usuarioService,
      ModelMapper modelMapper,
      EmprestimoService emprestimoService,
      SystemConfigService systemConfigService,
      ApplicationEventPublisher eventPublisher) {
    this.nadaConstaRepository = nadaConstaRepository;
    this.usuarioService = usuarioService;
    this.modelMapper = modelMapper;
    this.emprestimoService = emprestimoService;
    this.systemConfigService = systemConfigService;
    this.eventPublisher = eventPublisher;
  }

  @Override
  protected JpaRepository<NadaConsta, Long> getRepository() {
    return nadaConstaRepository;
  }

  /**
   * Converte uma entidade NadaConsta para um NadaConstaResponseDto.
   *
   * @param entity Entidade NadaConsta a ser convertida
   * @return DTO correspondente à entidade NadaConsta
   */
  @Override
  public NadaConstaResponseDto toDto(NadaConsta entity) {
    return modelMapper.map(entity, NadaConstaResponseDto.class);
  }

  /**
   * Converte um NadaConstaResponseDto para a entidade NadaConsta.
   *
   * @param nadaConstaResponseDto DTO de resposta a ser convertido
   * @return Entidade NadaConsta correspondente
   */
  @Override
  public NadaConsta toEntity(NadaConstaResponseDto nadaConstaResponseDto) {
    return modelMapper.map(nadaConstaResponseDto, NadaConsta.class);
  }

  @Override
  @Transactional(readOnly = true)
  public List<NadaConstaResponseDto> findAllByUsername(String username) {
    Usuario usuario = usuarioService.toEntity(usuarioService.findByUsername(username));
    if (usuario == null) {
      return Collections.emptyList();
    }
    return nadaConstaRepository.findAllByUsuario(usuario).stream().map(this::toDto).toList();
  }

  /**
   * Solicita uma declaração de Nada Consta para o usuário informado.
   *
   * @param documento Documento do usuário
   * @return Dados da solicitação de Nada Consta
   */
  @Override
  @Transactional
  public NadaConstaResponseDto solicitarNadaConsta(String documento) {
    Usuario usuario = usuarioService.toEntity(usuarioService.findByDocumento(documento));
    if (usuario == null) {
      throw new NadaConstaException(USUARIO_NAO_ENCONTRADO);
    }
    // Pre-check for open Nada Consta solicitation
    if (usuarioService.hasSolicitacaoNadaConstaPendingOrCompleted(usuario.getUsername())) {
      throw new NadaConstaException(SOLICITACAO_EXISTENTE);
    }
    List<Emprestimo> emprestimosAbertos =
        emprestimoService.findAllEmprestimosAbertosByUsuario(usuario.getUsername()).stream()
            .map(emprestimoService::toEntity)
            .toList();
    NadaConsta nadaConsta =
        NadaConsta.builder()
            .usuario(usuario)
            .status(
                emprestimosAbertos.isEmpty()
                    ? NadaConstaStatus.COMPLETED
                    : NadaConstaStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .createdBy(usuario.getUsername())
            .build();
    nadaConsta.setSendAt(LocalDateTime.now());
    nadaConsta = nadaConstaRepository.save(nadaConsta);
    usuario.setAtivo(false);
    usuarioService.save(usuario);

    if (emprestimosAbertos.isEmpty()) {
      String destinatario = systemConfigService.getEmailNadaConsta();
      if (!EmailUtils.isValidEmail(destinatario)) {
        log.warn(EMAIL_NADA_CONSTA_INVALIDO, EmailUtils.maskEmail(destinatario));
        return toDto(nadaConsta);
      }
      Map<String, Object> templateData = new HashMap<>();
      templateData.put(USUARIO, usuario);
      templateData.put(NADA_CONSTA, nadaConsta);
      templateData.put(NOME_ALUNO, usuario.getNome());
      templateData.put(REGISTRO_ACADEMICO, usuario.getDocumento());
      DateTimeFormatter formatter =
          DateTimeFormatter.ofPattern(FORMAT_PT_BR, Locale.forLanguageTag(LOCALE_PT_BR));
      templateData.put(DATA_FORMATADA, LocalDate.now().format(formatter));
      eventPublisher.publishEvent(
          new NadaConstaEmitidoEvent(this, destinatario, templateData, usuario.getEmail()));
    } else {
      String destinatario = usuario.getEmail();
      if (!EmailUtils.isValidEmail(destinatario)) {
        log.warn(EMAIL_PENDENCIAS_INVALIDO, usuario.getNome());
        return toDto(nadaConsta);
      }
      Map<String, Object> templateData = new HashMap<>();
      templateData.put(USUARIO, usuario);
      templateData.put(NOME_ALUNO, usuario.getNome());
      templateData.put(REGISTRO_ACADEMICO, usuario.getDocumento());
      DateTimeFormatter formatter =
          DateTimeFormatter.ofPattern(FORMAT_PT_BR, Locale.forLanguageTag(LOCALE_PT_BR));
      templateData.put(DATA_FORMATADA, LocalDate.now().format(formatter));
      templateData.put(
          EMPRESTIMOS,
          emprestimosAbertos.stream()
              .flatMap(
                  e ->
                      e.getEmprestimoItem() != null
                          ? e.getEmprestimoItem().stream()
                          : java.util.stream.Stream.empty())
              .map(
                  item -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put(ITEM_NOME, item.getItem() != null ? item.getItem().getNome() : null);
                    return map;
                  })
              .toList());
      eventPublisher.publishEvent(new NadaConstaPendenciasEvent(this, destinatario, templateData));
    }
    return toDto(nadaConsta);
  }

  /**
   * Verifica se as pendências do usuário foram resolvidas e atualiza o status da solicitação.
   * Dispara evento de conclusão caso não haja pendências.
   *
   * @param id Identificador da solicitação de Nada Consta
   * @return Dados atualizados da solicitação
   */
  @Override
  @Transactional
  public NadaConstaResponseDto verificarPendenciasNadaConsta(Long id) {
    NadaConsta nadaConsta =
        nadaConstaRepository
            .findById(id)
            .orElseThrow(() -> new NadaConstaException(NADA_CONSTA_NAO_ENCONTRADO));
    if (nadaConsta.getStatus() != NadaConstaStatus.PENDING) {
      throw new NadaConstaException(NADA_CONSTA_STATUS_PENDING);
    }
    Usuario usuario = nadaConsta.getUsuario();
    List<Emprestimo> emprestimosAbertos =
        emprestimoService.findAllEmprestimosAbertosByUsuario(usuario.getUsername()).stream()
            .map(emprestimoService::toEntity)
            .toList();
    if (emprestimosAbertos.isEmpty()) {
      nadaConsta.setStatus(NadaConstaStatus.COMPLETED);
      nadaConstaRepository.save(nadaConsta);
      // Dispara evento de conclusão
      String destinatario = systemConfigService.getEmailNadaConsta();
      if (EmailUtils.isValidEmail(destinatario)) {
        Map<String, Object> templateData = new HashMap<>();
        templateData.put(USUARIO, usuario);
        templateData.put(NADA_CONSTA, nadaConsta);
        eventPublisher.publishEvent(new NadaConstaEmitidoEvent(this, destinatario, templateData));
      } else {
        log.warn(EMAIL_NADA_CONSTA_INVALIDO, EmailUtils.maskEmail(destinatario));
      }
    }
    return convertToDto(nadaConsta);
  }

  /**
   * Invalida uma declaração de Nada Consta emitida. Atualiza o status para INVALIDATED e reativa o
   * usuário vinculado. Interpreta que o aluno retornou para estudo na universidade.
   *
   * @param id Identificador da solicitação de Nada Consta
   * @return Dados atualizados da solicitação
   */
  @Override
  @Transactional
  public NadaConstaResponseDto invalidarNadaConsta(Long id) {
    NadaConsta nadaConsta =
        nadaConstaRepository
            .findById(id)
            .orElseThrow(() -> new NadaConstaException(NADA_CONSTA_NAO_ENCONTRADO));
    if (nadaConsta.getStatus() != NadaConstaStatus.COMPLETED) {
      throw new NadaConstaException(
          "Só é possível invalidar Nada Consta emitido (status COMPLETED).");
    }
    nadaConsta.setStatus(NadaConstaStatus.INVALIDATED);
    nadaConstaRepository.save(nadaConsta);
    // Reativa o usuário vinculado
    Usuario usuario = nadaConsta.getUsuario();
    usuario.setAtivo(true);
    usuarioService.save(usuario);
    log.info("Nada Consta id={} invalidado (status INVALIDATED) e usuário reativado.", id);
    return convertToDto(nadaConsta);
  }

  /**
   * Reenvia uma declaração de Nada Consta já emitida para o email do usuário.
   *
   * @param id Identificador da solicitação de Nada Consta
   * @return true se o reenvio foi bem-sucedido, false caso contrário
   */
  @Override
  @Transactional
  public boolean reenviarNadaConsta(Long id) {
    NadaConsta nadaConsta = nadaConstaRepository.findById(id).orElse(null);
    if (nadaConsta == null) {
      return false;
    }
    // Verifica se o status é COMPLETED
    if (nadaConsta.getStatus() != NadaConstaStatus.COMPLETED) {
      log.warn(
          "Reenvio de Nada Consta não realizado - status inválido: {}", nadaConsta.getStatus());
      return false;
    }
    Usuario usuario = nadaConsta.getUsuario();
    String destinatario = systemConfigService.getEmailNadaConsta();
    if (!EmailUtils.isValidEmail(destinatario)) {
      log.warn(
          "Reenvio de Nada Consta não realizado - email do sistema inválido: {}",
          EmailUtils.maskEmail(destinatario));
      return false;
    }
    String cc = null;
    if (EmailUtils.isValidEmail(usuario.getEmail())) {
      cc = usuario.getEmail();
    } else {
      log.warn(
          "Reenvio de Nada Consta não realizado - email do usuário inválido: {}",
          EmailUtils.maskEmail(usuario.getEmail()));
    }
    Map<String, Object> templateData = new HashMap<>();
    templateData.put(USUARIO, usuario);
    templateData.put(NADA_CONSTA, nadaConsta);
    templateData.put(NOME_ALUNO, usuario.getNome());
    templateData.put(REGISTRO_ACADEMICO, usuario.getDocumento());
    DateTimeFormatter formatter =
        DateTimeFormatter.ofPattern(FORMAT_PT_BR, Locale.forLanguageTag(LOCALE_PT_BR));
    LocalDate createdDate =
        nadaConsta.getCreatedAt() != null
            ? nadaConsta.getCreatedAt().toLocalDate()
            : LocalDate.now();
    templateData.put(DATA_FORMATADA, createdDate.format(formatter));
    eventPublisher.publishEvent(new NadaConstaEmitidoEvent(this, destinatario, templateData, cc));
    return true;
  }

  @Override
  public NadaConstaResponseDto convertToDto(NadaConsta entity) {
    return modelMapper.map(entity, NadaConstaResponseDto.class);
  }
}
