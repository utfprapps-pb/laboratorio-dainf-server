package br.com.utfpr.gerenciamento.server.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.dto.EmprestimoResponseDto;
import br.com.utfpr.gerenciamento.server.dto.NadaConstaResponseDto;
import br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto;
import br.com.utfpr.gerenciamento.server.enumeration.NadaConstaStatus;
import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.model.EmprestimoItem;
import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.model.NadaConsta;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.repository.NadaConstaRepository;
import br.com.utfpr.gerenciamento.server.service.EmailService;
import br.com.utfpr.gerenciamento.server.service.EmprestimoService;
import br.com.utfpr.gerenciamento.server.service.SystemConfigService;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class NadaConstaServiceImplTest {
  private UsuarioResponseDto buildUsuarioResponseDto(Usuario usuario) {
    UsuarioResponseDto usuarioDto = new UsuarioResponseDto();
    usuarioDto.setId(usuario.getId());
    usuarioDto.setNome(usuario.getNome());
    usuarioDto.setUsername(usuario.getUsername());
    usuarioDto.setDocumento(usuario.getDocumento());
    usuarioDto.setEmail(usuario.getEmail());
    usuarioDto.setTelefone(null);
    usuarioDto.setPermissoes(null);
    usuarioDto.setFotoUrl(null);
    usuarioDto.setEmailVerificado(false);
    usuarioDto.setAuthorities(null);
    return usuarioDto;
  }

  private NadaConstaRepository nadaConstaRepository;
  private UsuarioService usuarioService;
  private EmprestimoService emprestimoService;
  private EmailService emailService;
  private SystemConfigService systemConfigService;
  private ModelMapper modelMapper;
  private NadaConstaServiceImpl service;

  @BeforeEach
  void setup() {
    nadaConstaRepository = Mockito.mock(NadaConstaRepository.class);
    usuarioService = Mockito.mock(UsuarioService.class);
    emprestimoService = Mockito.mock(EmprestimoService.class);
    emailService = Mockito.mock(EmailService.class);
    systemConfigService = Mockito.mock(SystemConfigService.class);
    modelMapper = Mockito.mock(ModelMapper.class);
    service =
        new NadaConstaServiceImpl(
            nadaConstaRepository,
            usuarioService,
            modelMapper,
            emprestimoService,
            emailService,
            systemConfigService);
    // Corrige o modelMapper para retornar um DTO válido
    when(modelMapper.map(any(), eq(NadaConstaResponseDto.class)))
        .thenReturn(new NadaConstaResponseDto());
    // Mock padrão para hasSolicitacaoNadaConstaPendingOrCompleted
    when(usuarioService.hasSolicitacaoNadaConstaPendingOrCompleted(anyString())).thenReturn(false);
  }

  @Test
  void shouldSendDeclarationAndDeactivateUserWhenNoPendingLoans() {
    Usuario usuario =
        Usuario.builder()
            .id(1L)
            .nome("Aluno Teste")
            .username("aluno@utfpr.edu.br")
            .documento("123456")
            .email("aluno@utfpr.edu.br")
            .ativo(true)
            .build();
    UsuarioResponseDto usuarioDto = buildUsuarioResponseDto(usuario);
    when(usuarioService.findByDocumento("123456")).thenReturn(usuarioDto);
    when(usuarioService.toEntity(usuarioDto)).thenReturn(usuario);
    when(emprestimoService.findAllEmprestimosAbertosByUsuario(anyString())).thenReturn(List.of());
    when(systemConfigService.getEmailNadaConsta()).thenReturn("destino@utfpr.edu.br");
    NadaConsta nadaConsta =
        NadaConsta.builder()
            .id(1L)
            .usuario(usuario)
            .status(NadaConstaStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .createdBy("Aluno Teste")
            .build();
    when(nadaConstaRepository.save(any())).thenReturn(nadaConsta);
    doNothing()
        .when(emailService)
        .sendEmailWithTemplate(any(), anyString(), anyString(), anyString());
    when(usuarioService.save(any(Usuario.class))).thenReturn(usuarioDto);
    NadaConstaResponseDto dto = service.solicitarNadaConsta("123456");
    assertNotNull(dto);
    verify(emailService)
        .sendEmailWithTemplate(
            any(),
            eq("destino@utfpr.edu.br"),
            eq("Declaração Nada Consta"),
            eq("nada-consta-declaracao.html"));
    verify(usuarioService).save(any(Usuario.class));
  }

  @Test
  void shouldSendPendingLoansEmailWhenUserHasPendingLoans() {
    Usuario usuario =
        Usuario.builder()
            .id(1L)
            .nome("Aluno Teste")
            .username("aluno@utfpr.edu.br")
            .documento("123456")
            .email("aluno@utfpr.edu.br")
            .ativo(true)
            .build();
    UsuarioResponseDto usuarioDto = new UsuarioResponseDto();
    usuarioDto.setId(usuario.getId());
    usuarioDto.setNome(usuario.getNome());
    usuarioDto.setUsername(usuario.getUsername());
    usuarioDto.setDocumento(usuario.getDocumento());
    usuarioDto.setEmail(usuario.getEmail());
    usuarioDto.setTelefone(null);
    usuarioDto.setPermissoes(null);
    usuarioDto.setFotoUrl(null);
    usuarioDto.setEmailVerificado(false);
    usuarioDto.setAuthorities(null);
    when(usuarioService.findByDocumento("123456")).thenReturn(usuarioDto);
    when(usuarioService.toEntity(usuarioDto)).thenReturn(usuario);
    when(modelMapper.map(usuarioDto, Usuario.class)).thenReturn(usuario);
    Item item = new Item();
    item.setNome("Notebook");
    EmprestimoItem emprestimoItem = new EmprestimoItem();
    emprestimoItem.setItem(item);
    Emprestimo emprestimo = new Emprestimo();
    emprestimo.setEmprestimoItem(List.of(emprestimoItem));
    emprestimo.setDataEmprestimo(LocalDate.now());
    emprestimo.setPrazoDevolucao(LocalDate.now().plusDays(7));
    EmprestimoResponseDto emprestimoDto = new EmprestimoResponseDto();
    emprestimoDto.setId(1L);
    emprestimoDto.setDataEmprestimo(emprestimo.getDataEmprestimo());
    emprestimoDto.setPrazoDevolucao(emprestimo.getPrazoDevolucao());
    when(emprestimoService.findAllEmprestimosAbertosByUsuario(usuario.getUsername()))
        .thenReturn(List.of(emprestimoDto));
    when(emprestimoService.toEntity(emprestimoDto)).thenReturn(emprestimo);
    when(modelMapper.map(emprestimoDto, Emprestimo.class)).thenReturn(emprestimo);
    when(nadaConstaRepository.save(any()))
        .thenReturn(
            NadaConsta.builder()
                .id(1L)
                .usuario(usuario)
                .status(NadaConstaStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .createdBy("Aluno Teste")
                .build());
    doNothing()
        .when(emailService)
        .sendEmailWithTemplate(any(), anyString(), anyString(), anyString());
    NadaConstaResponseDto dto = service.solicitarNadaConsta("123456");
    assertNotNull(dto);
    ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
    verify(emailService)
        .sendEmailWithTemplate(
            captor.capture(),
            eq("aluno@utfpr.edu.br"),
            eq("Pendências de Empréstimos"),
            eq("pendencias-emprestimos.html"));
    Object emprestimosObj = captor.getValue().get("emprestimos");
    assertNotNull(emprestimosObj);
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> itens = (List<Map<String, Object>>) emprestimosObj;
    assertEquals(1, itens.size());
    // O nome do item é "Notebook" conforme simulado
    assertEquals("Notebook", itens.get(0).get("itemNome"));
  }

  @Test
  void shouldThrowExceptionWhenUserNotFound() {
    when(usuarioService.findByDocumento("999999")).thenReturn(null);
    assertThrows(RuntimeException.class, () -> service.solicitarNadaConsta("999999"));
  }
}
