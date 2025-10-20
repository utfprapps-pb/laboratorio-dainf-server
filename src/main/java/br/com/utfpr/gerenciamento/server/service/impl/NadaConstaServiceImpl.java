package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.dto.NadaConstaResponseDto;
import br.com.utfpr.gerenciamento.server.enumeration.NadaConstaStatus;
import br.com.utfpr.gerenciamento.server.exception.NadaConstaException;
import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.model.NadaConsta;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.repository.NadaConstaRepository;
import br.com.utfpr.gerenciamento.server.service.EmailService;
import br.com.utfpr.gerenciamento.server.service.EmprestimoService;
import br.com.utfpr.gerenciamento.server.service.NadaConstaService;
import br.com.utfpr.gerenciamento.server.service.SystemConfigService;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NadaConstaServiceImpl extends CrudServiceImpl<NadaConsta, Long>
    implements NadaConstaService {

  private final NadaConstaRepository nadaConstaRepository;
  private final UsuarioService usuarioService;
  private final ModelMapper modelMapper;
  private final EmprestimoService emprestimoService;
  private final EmailService emailService;
  private final SystemConfigService systemConfigService;

  public NadaConstaServiceImpl(
      NadaConstaRepository nadaConstaRepository,
      UsuarioService usuarioService,
      ModelMapper modelMapper,
      EmprestimoService emprestimoService,
      EmailService emailService,
      SystemConfigService systemConfigService) {
    this.nadaConstaRepository = nadaConstaRepository;
    this.usuarioService = usuarioService;
    this.modelMapper = modelMapper;
    this.emprestimoService = emprestimoService;
    this.emailService = emailService;
    this.systemConfigService = systemConfigService;
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

  @Override
  @Transactional
  public NadaConstaResponseDto solicitarNadaConsta(String documento) {
    Usuario usuario = usuarioService.findByDocumento(documento);
    if (usuario == null) {
      throw new RuntimeException("Usuário não encontrado para o documento informado.");
    }
    // Pre-check for open Nada Consta solicitation
    if (usuarioService
            instanceof br.com.utfpr.gerenciamento.server.service.impl.UsuarioServiceImpl impl
        && impl.hasSolicitacaoNadaConstaPendingOrCompleted(usuario.getUsername())) {
      throw new NadaConstaException(
          "Já existe uma solicitação de Nada Consta em aberto ou concluída para este usuário.");
    }
    List<Emprestimo> emprestimosAbertos =
        emprestimoService.findAllEmprestimosAbertosByUsuario(usuario.getUsername());
    NadaConsta nadaConsta =
        NadaConsta.builder()
            .usuario(usuario)
            .status(NadaConstaStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .createdBy(usuario.getUsername())
            .build();
    nadaConsta = nadaConstaRepository.save(nadaConsta);
    if (emprestimosAbertos.isEmpty()) {
      // Buscar e-mail de destino via SystemConfigService
      String destinatario = systemConfigService.getEmailNadaConsta();
      // Monta dados para o template
      DateTimeFormatter formatter =
          DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy")
              .withLocale(java.util.Locale.forLanguageTag("pt-BR"));
      Map<String, Object> templateData = new HashMap<>();
      templateData.put("nomeAluno", usuario.getNome());
      templateData.put("registroAcademico", usuario.getDocumento());
      templateData.put("dataFormatada", LocalDateTime.now().format(formatter));
      templateData.put("logoUrl", systemConfigService.getLogoUrl());
      emailService.sendEmailWithTemplate(
          templateData, destinatario, "Declaração Nada Consta", "nada-consta-declaracao.html");
      nadaConsta.setStatus(NadaConstaStatus.COMPLETED);
      nadaConsta.setSendAt(LocalDateTime.now());
      nadaConstaRepository.save(nadaConsta);
      usuario.setAtivo(false);
      usuarioService.save(usuario);
    } else {
      // Monta lista de itens pendentes para o template
      List<Map<String, Object>> itensPendentesTemplate = new ArrayList<>();
      for (Emprestimo emp : emprestimosAbertos) {
        if (emp.getEmprestimoItem() != null) {
          for (var emprestimoItem : emp.getEmprestimoItem()) {
            Map<String, Object> itemMap = new HashMap<>();
            var item = emprestimoItem.getItem();
            itemMap.put("itemNome", item != null ? item.getNome() : "-");
            itemMap.put(
                "dataEmprestimo",
                emp.getDataEmprestimo().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            itemMap.put(
                "dataPrevistaDevolucao",
                emp.getPrazoDevolucao() != null
                    ? emp.getPrazoDevolucao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    : "-");
            itensPendentesTemplate.add(itemMap);
          }
        }
      }
      Map<String, Object> templateData = new HashMap<>();
      templateData.put("nomeAluno", usuario.getNome());
      templateData.put("emprestimos", itensPendentesTemplate);
      emailService.sendEmailWithTemplate(
          templateData,
          usuario.getEmail(),
          "Pendências de Empréstimos",
          "pendencias-emprestimos.html");
      nadaConsta.setStatus(NadaConstaStatus.PENDING);
      nadaConsta.setSendAt(LocalDateTime.now());
      nadaConstaRepository.save(nadaConsta);
      usuario.setAtivo(false);
      usuarioService.save(usuario);
    }
    return convertToDto(nadaConsta);
  }
}
