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
import br.com.utfpr.gerenciamento.server.util.DateUtil;
import br.com.utfpr.gerenciamento.server.util.EmailUtils;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
public class NadaConstaServiceImpl extends CrudServiceImpl<NadaConsta, Long>
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

  @Override
  @Transactional(readOnly = true)
  public List<NadaConstaResponseDto> findAllByUsername(String username) {
    var usuario = usuarioService.findByUsername(username);
    if (usuario == null) {
      return Collections.emptyList();
    }
    return nadaConstaRepository.findAllByUsuario(usuario).stream().map(this::convertToDto).toList();
  }

  @Override
  public NadaConstaResponseDto convertToDto(NadaConsta entity) {
    var dto = modelMapper.map(entity, NadaConstaResponseDto.class);
    if (entity.getUsuario() != null) {
      dto.setUsuarioUsername(entity.getUsuario().getUsername());
    }
    return dto;
  }

  public void postSave(NadaConsta entity) {
    if (entity == null || entity.getUsuario() == null) return;
    Usuario usuario = entity.getUsuario();
    List<Emprestimo> emprestimosAbertos =
        emprestimoService.findAllEmprestimosAbertosByUsuario(usuario.getUsername());
    if (emprestimosAbertos == null) {
      emprestimosAbertos = Collections.emptyList();
    }
    if (entity.getStatus() == NadaConstaStatus.COMPLETED && emprestimosAbertos.isEmpty()) {
      String destinatario = systemConfigService.getEmailNadaConsta();
      DateTimeFormatter formatter =
          DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy").withLocale(DateUtil.PT_BR);
      Map<String, Object> templateData = new HashMap<>();
      templateData.put("nomeAluno", usuario.getNome());
      templateData.put("registroAcademico", usuario.getDocumento());
      templateData.put(
          "dataFormatada",
          entity.getCreatedAt() != null
              ? entity.getCreatedAt().format(formatter)
              : LocalDateTime.now().format(formatter));
      templateData.put("logoUrl", systemConfigService.getLogoUrl());
      if (org.springframework.transaction.support.TransactionSynchronizationManager
          .isSynchronizationActive()) {
        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
              @Override
              public void afterCommit() {
                eventPublisher.publishEvent(
                    new NadaConstaEmitidoEvent(
                        NadaConstaServiceImpl.this, destinatario, templateData));
              }
            });
      } else {
        eventPublisher.publishEvent(
            new NadaConstaEmitidoEvent(NadaConstaServiceImpl.this, destinatario, templateData));
      }
    } else if (entity.getStatus() == NadaConstaStatus.PENDING && !emprestimosAbertos.isEmpty()) {
      List<Map<String, Object>> itensPendentesTemplate = new ArrayList<>();
      for (Emprestimo emp : emprestimosAbertos) {
        if (emp.getEmprestimoItem() != null) {
          for (var emprestimoItem : emp.getEmprestimoItem()) {
            Map<String, Object> itemMap = new HashMap<>();
            var item = emprestimoItem.getItem();
            itemMap.put("itemNome", item != null ? item.getNome() : "-");
            itemMap.put(
                "dataEmprestimo", emp.getDataEmprestimo().format(DateUtil.BR_DATE_FORMATTER));
            itemMap.put(
                "dataPrevistaDevolucao",
                emp.getPrazoDevolucao() != null
                    ? emp.getPrazoDevolucao().format(DateUtil.BR_DATE_FORMATTER)
                    : "-");
            itensPendentesTemplate.add(itemMap);
          }
        }
      }
      Map<String, Object> templateData = new HashMap<>();
      templateData.put("nomeAluno", usuario.getNome());
      templateData.put("emprestimos", itensPendentesTemplate);
      String to = usuario.getEmail();
      if (EmailUtils.isValidEmail(to)) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
          TransactionSynchronizationManager.registerSynchronization(
              new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                  eventPublisher.publishEvent(
                      new NadaConstaPendenciasEvent(NadaConstaServiceImpl.this, to, templateData));
                }
              });
        } else {
          eventPublisher.publishEvent(
              new NadaConstaPendenciasEvent(NadaConstaServiceImpl.this, to, templateData));
        }
      }
    }
  }

  @Override
  public NadaConstaResponseDto solicitarNadaConsta(String documento) {
    Usuario usuario = usuarioService.findByDocumento(documento);
    if (usuario == null) {
      throw new RuntimeException("Usuário não encontrado para o documento informado.");
    }
    // Pre-check for open Nada Consta solicitation
    if (usuarioService.hasSolicitacaoNadaConstaPendingOrCompleted(usuario.getUsername())) {
      throw new NadaConstaException(
          "Já existe uma solicitação de Nada Consta em aberto ou concluída para este usuário.");
    }
    List<Emprestimo> emprestimosAbertos =
        emprestimoService.findAllEmprestimosAbertosByUsuario(usuario.getUsername());
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
    nadaConsta = nadaConstaRepository.save(nadaConsta);
    nadaConsta.setSendAt(LocalDateTime.now());
    nadaConstaRepository.save(nadaConsta);
    usuario.setAtivo(false);
    usuarioService.save(usuario);
    postSave(nadaConsta);
    return convertToDto(nadaConsta);
  }
}
