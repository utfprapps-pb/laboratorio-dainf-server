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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
   * Busca todas as solicitações de Nada Consta pelo username do usuário.
   *
   * @param username Username do usuário
   * @return Lista de NadaConstaResponseDto
   */
  @Override
  public NadaConstaResponseDto toDto(NadaConsta entity) {
    return modelMapper.map(entity, NadaConstaResponseDto.class);
  }

  /**
   * Converte uma entidade NadaConsta para o DTO de resposta.
   *
   * @param nadaConsta Entidade NadaConsta
   * @return NadaConstaResponseDto correspondente
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
      throw new RuntimeException("Usuário não encontrado para o documento informado.");
    }
    // Pre-check for open Nada Consta solicitation
    if (usuarioService.hasSolicitacaoNadaConstaPendingOrCompleted(usuario.getUsername())) {
      throw new NadaConstaException(
          "Já existe uma solicitação de Nada Consta em aberto ou concluída para este usuário.");
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
        log.warn("Email de Nada Consta não enviado - email do sistema inválido: {}", destinatario);
        return toDto(nadaConsta);
      }
      Map<String, Object> templateData = new HashMap<>();
      templateData.put("usuario", usuario);
      templateData.put("nadaConsta", nadaConsta);
      eventPublisher.publishEvent(new NadaConstaEmitidoEvent(this, destinatario, templateData));
    } else {
      String destinatario = usuario.getEmail();
      if (!EmailUtils.isValidEmail(destinatario)) {
        log.warn(
            "Email de pendências de Nada Consta não enviado - usuário sem email válido: {}",
            usuario.getNome());
        return toDto(nadaConsta);
      }
      Map<String, Object> templateData = new HashMap<>();
      templateData.put("usuario", usuario);
      templateData.put(
          "emprestimos",
          emprestimosAbertos.stream()
              .flatMap(
                  e ->
                      e.getEmprestimoItem() != null
                          ? e.getEmprestimoItem().stream()
                          : java.util.stream.Stream.empty())
              .map(
                  item -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("itemNome", item.getItem() != null ? item.getItem().getNome() : null);
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
            .orElseThrow(() -> new NadaConstaException("Nada Consta não encontrado."));
    if (nadaConsta.getStatus() != NadaConstaStatus.PENDING) {
      throw new NadaConstaException("Nada Consta não está com status PENDING.");
    }
    Usuario usuario = nadaConsta.getUsuario();
    List<Emprestimo> emprestimosAbertos =
        emprestimoService.findAllEmprestimosAbertosByUsuario(usuario.getUsername());
    if (emprestimosAbertos == null || emprestimosAbertos.isEmpty()) {
      nadaConsta.setStatus(NadaConstaStatus.COMPLETED);
      nadaConstaRepository.save(nadaConsta);
      // Dispara evento de conclusão
      String destinatario = systemConfigService.getEmailNadaConsta();
      if (EmailUtils.isValidEmail(destinatario)) {
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("usuario", usuario);
        templateData.put("nadaConsta", nadaConsta);
        eventPublisher.publishEvent(new NadaConstaEmitidoEvent(this, destinatario, templateData));
      } else {
        log.warn("Email de Nada Consta não enviado - email do sistema inválido: {}", destinatario);
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
            .orElseThrow(() -> new NadaConstaException("Nada Consta não encontrado."));
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
}
