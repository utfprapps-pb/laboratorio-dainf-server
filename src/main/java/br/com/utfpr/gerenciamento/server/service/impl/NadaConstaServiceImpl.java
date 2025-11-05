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
  public NadaConstaResponseDto convertToDto(NadaConsta nadaConsta) {
    NadaConstaResponseDto dto = modelMapper.map(nadaConsta, NadaConstaResponseDto.class);
    if (dto == null) {
      return null;
    }
    if (nadaConsta != null && nadaConsta.getUsuario() != null) {
      dto.setUsuarioUsername(nadaConsta.getUsuario().getUsername());
    } else {
      dto.setUsuarioUsername(null);
    }
    return dto;
  }

  @Override
  @Transactional
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
                emprestimosAbertos == null || emprestimosAbertos.isEmpty()
                    ? NadaConstaStatus.COMPLETED
                    : NadaConstaStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .createdBy(usuario.getUsername())
            .build();
    nadaConsta.setSendAt(LocalDateTime.now());
    nadaConsta = nadaConstaRepository.save(nadaConsta);
    usuario.setAtivo(false);
    usuarioService.save(usuario);

    if (emprestimosAbertos == null || emprestimosAbertos.isEmpty()) {
      String destinatario = systemConfigService.getEmailNadaConsta();
      if (!EmailUtils.isValidEmail(destinatario)) {
        log.warn("Email de Nada Consta não enviado - email do sistema inválido: {}", destinatario);
        return convertToDto(nadaConsta);
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
        return convertToDto(nadaConsta);
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
    return convertToDto(nadaConsta);
  }
}
