package br.com.utfpr.gerenciamento.server.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.dto.NadaConstaResponseDto;
import br.com.utfpr.gerenciamento.server.enumeration.NadaConstaStatus;
import br.com.utfpr.gerenciamento.server.event.nadaConsta.NadaConstaEmitidoEvent;
import br.com.utfpr.gerenciamento.server.event.nadaConsta.NadaConstaPendenciasEvent;
import br.com.utfpr.gerenciamento.server.exception.NadaConstaException;
import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.model.EmprestimoItem;
import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.model.NadaConsta;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.repository.NadaConstaRepository;
import br.com.utfpr.gerenciamento.server.service.EmprestimoService;
import br.com.utfpr.gerenciamento.server.service.SystemConfigService;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class NadaConstaServiceImplTest {
  private NadaConstaRepository nadaConstaRepository;
  private UsuarioService usuarioService;
  private EmprestimoService emprestimoService;
  private SystemConfigService systemConfigService;
  private ModelMapper modelMapper;
  private NadaConstaServiceImpl service;
  private ApplicationEventPublisher eventPublisher;

  @BeforeEach
  void setup() {
    nadaConstaRepository = Mockito.mock(NadaConstaRepository.class);
    usuarioService = Mockito.mock(UsuarioService.class);
    emprestimoService = Mockito.mock(EmprestimoService.class);
    systemConfigService = Mockito.mock(SystemConfigService.class);
    modelMapper = Mockito.mock(ModelMapper.class);
    eventPublisher = Mockito.spy(ApplicationEventPublisher.class);
    service =
        new NadaConstaServiceImpl(
            nadaConstaRepository,
            usuarioService,
            modelMapper,
            emprestimoService,
            systemConfigService,
            eventPublisher);
    // Corrige o modelMapper para retornar um DTO válido
    when(modelMapper.map(any(), eq(NadaConstaResponseDto.class)))
        .thenReturn(new NadaConstaResponseDto());
  }

  @Test
  void shouldSendDeclarationAndDeactivateUserWhenNoPendingLoans() {
    Usuario usuario =
        Usuario.builder()
            .id(1L)
            .nome("Aluno Teste")
            .documento("123456")
            .email("aluno@utfpr.edu.br")
            .ativo(true)
            .build();
    when(usuarioService.findByDocumento("123456")).thenReturn(usuario);
    when(emprestimoService.findAllEmprestimosAbertosByUsuario(anyString())).thenReturn(List.of());
    when(systemConfigService.getEmailNadaConsta()).thenReturn("destino@utfpr.edu.br");
    NadaConsta nadaConsta =
        NadaConsta.builder()
            .id(1L)
            .usuario(usuario)
            .status(NadaConstaStatus.COMPLETED) // Corrigido para COMPLETED
            .createdAt(LocalDateTime.now())
            .createdBy("Aluno Teste")
            .build();
    when(nadaConstaRepository.save(any())).thenReturn(nadaConsta);
    when(usuarioService.save(any(Usuario.class))).thenReturn(usuario);
    NadaConstaResponseDto dto = service.solicitarNadaConsta("123456");
    assertNotNull(dto);
    ArgumentCaptor<NadaConstaEmitidoEvent> captor =
        ArgumentCaptor.forClass(NadaConstaEmitidoEvent.class);
    verify(eventPublisher).publishEvent(captor.capture());
    NadaConstaEmitidoEvent event = captor.getValue();
    assertEquals("destino@utfpr.edu.br", event.getRecipient());
    assertEquals("Declaração Nada Consta", event.getSubject());
    assertEquals("nada-consta-declaracao.html", event.getTemplateName());
    assertNotNull(event.getTemplateData());
    verify(usuarioService).save(any(Usuario.class));
  }

  @Test
  void shouldSendPendingLoansEmailWhenUserHasPendingLoans() {
    Usuario usuario =
        Usuario.builder()
            .id(1L)
            .nome("Aluno Teste")
            .documento("123456")
            .email("aluno@utfpr.edu.br")
            .ativo(true)
            .build();
    when(usuarioService.findByDocumento("123456")).thenReturn(usuario);
    Item item = new Item();
    item.setNome("Notebook");
    EmprestimoItem emprestimoItem = new EmprestimoItem();
    emprestimoItem.setItem(item);
    Emprestimo emprestimo = new Emprestimo();
    emprestimo.setEmprestimoItem(Set.of(emprestimoItem));
    emprestimo.setDataEmprestimo(LocalDate.now());
    emprestimo.setPrazoDevolucao(LocalDate.now().plusDays(7));
    when(emprestimoService.findAllEmprestimosAbertosByUsuario(usuario.getUsername()))
        .thenReturn(List.of(emprestimo));
    when(nadaConstaRepository.save(any()))
        .thenReturn(
            NadaConsta.builder()
                .id(1L)
                .usuario(usuario)
                .status(NadaConstaStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .createdBy("Aluno Teste")
                .build());
    NadaConstaResponseDto dto = service.solicitarNadaConsta("123456");
    assertNotNull(dto);
    ArgumentCaptor<NadaConstaPendenciasEvent> captor =
        ArgumentCaptor.forClass(NadaConstaPendenciasEvent.class);
    verify(eventPublisher).publishEvent(captor.capture());
    NadaConstaPendenciasEvent event = captor.getValue();
    assertEquals("aluno@utfpr.edu.br", event.getRecipient());
    assertEquals("Pendências de Empréstimos", event.getSubject());
    assertEquals("pendencias-emprestimos.html", event.getTemplateName());
    assertNotNull(event.getTemplateData());
    Object emprestimosObj = event.getTemplateData().get("emprestimos");
    assertNotNull(emprestimosObj);
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> itens = (List<Map<String, Object>>) emprestimosObj;
    assertEquals(1, itens.size());
    assertEquals("Notebook", itens.getFirst().get("itemNome"));
  }

  @Test
  void shouldThrowExceptionWhenUserNotFound() {
    when(usuarioService.findByDocumento("999999")).thenReturn(null);
    assertThrows(RuntimeException.class, () -> service.solicitarNadaConsta("999999"));
  }

  @Test
  void shouldHandleNullEmprestimosList() {
    Usuario usuario =
        Usuario.builder().id(2L).documento("222222").email("user@utfpr.edu.br").ativo(true).build();
    when(usuarioService.findByDocumento("222222")).thenReturn(usuario);
    when(emprestimoService.findAllEmprestimosAbertosByUsuario(anyString())).thenReturn(null);
    when(systemConfigService.getEmailNadaConsta()).thenReturn("destino@utfpr.edu.br");
    NadaConsta nadaConsta =
        NadaConsta.builder()
            .id(2L)
            .usuario(usuario)
            .status(NadaConstaStatus.COMPLETED)
            .createdAt(LocalDateTime.now())
            .createdBy("user")
            .build();
    when(nadaConstaRepository.save(any())).thenReturn(nadaConsta);
    when(usuarioService.save(any(Usuario.class))).thenReturn(usuario);
    NadaConstaResponseDto dto = service.solicitarNadaConsta("222222");
    assertNotNull(dto);
    verify(eventPublisher).publishEvent(any(NadaConstaEmitidoEvent.class));
    verify(usuarioService).save(any(Usuario.class));
  }

  @Test
  void shouldHandleNullSystemConfigEmail() {
    Usuario usuario =
        Usuario.builder().id(3L).documento("333333").email("user@utfpr.edu.br").ativo(true).build();
    when(usuarioService.findByDocumento("333333")).thenReturn(usuario);
    when(emprestimoService.findAllEmprestimosAbertosByUsuario(anyString())).thenReturn(List.of());
    when(systemConfigService.getEmailNadaConsta()).thenReturn(null);
    NadaConsta nadaConsta =
        NadaConsta.builder()
            .id(3L)
            .usuario(usuario)
            .status(NadaConstaStatus.COMPLETED)
            .createdAt(LocalDateTime.now())
            .createdBy("user")
            .build();
    when(nadaConstaRepository.save(any())).thenReturn(nadaConsta);
    when(usuarioService.save(any(Usuario.class))).thenReturn(usuario);
    NadaConstaResponseDto dto = service.solicitarNadaConsta("333333");
    assertNotNull(dto);
    // Should NOT publish event if email is null/invalid
    verify(eventPublisher, never()).publishEvent(any(NadaConstaEmitidoEvent.class));
    verify(usuarioService).save(any(Usuario.class));
  }

  @Test
  void shouldHandleModelMapperReturnsNull() {
    Usuario usuario =
        Usuario.builder().id(4L).documento("444444").email("user@utfpr.edu.br").ativo(true).build();
    when(usuarioService.findByDocumento("444444")).thenReturn(usuario);
    when(emprestimoService.findAllEmprestimosAbertosByUsuario(anyString())).thenReturn(List.of());
    when(systemConfigService.getEmailNadaConsta()).thenReturn("destino@utfpr.edu.br");
    NadaConsta nadaConsta =
        NadaConsta.builder()
            .id(4L)
            .usuario(usuario)
            .status(NadaConstaStatus.COMPLETED)
            .createdAt(LocalDateTime.now())
            .createdBy("user")
            .build();
    when(nadaConstaRepository.save(any())).thenReturn(nadaConsta);
    when(usuarioService.save(any(Usuario.class))).thenReturn(usuario);
    when(modelMapper.map(any(), eq(NadaConstaResponseDto.class))).thenReturn(null);
    NadaConstaResponseDto dto = service.solicitarNadaConsta("444444");
    assertNull(dto);
    verify(eventPublisher).publishEvent(any(NadaConstaEmitidoEvent.class));
    verify(usuarioService).save(any(Usuario.class));
  }

  @Test
  void shouldThrowExceptionWhenRepositoryFails() {
    Usuario usuario =
        Usuario.builder().id(5L).documento("555555").email("user@utfpr.edu.br").ativo(true).build();
    when(usuarioService.findByDocumento("555555")).thenReturn(usuario);
    when(emprestimoService.findAllEmprestimosAbertosByUsuario(anyString())).thenReturn(List.of());
    when(systemConfigService.getEmailNadaConsta()).thenReturn("destino@utfpr.edu.br");
    when(nadaConstaRepository.save(any())).thenThrow(new RuntimeException("DB error"));
    assertThrows(RuntimeException.class, () -> service.solicitarNadaConsta("555555"));
  }

  @Test
  void shouldThrowExceptionWhenUsuarioServiceFails() {
    Usuario usuario =
        Usuario.builder().id(6L).documento("666666").email("user@utfpr.edu.br").ativo(true).build();
    when(usuarioService.findByDocumento("666666")).thenReturn(usuario);
    when(emprestimoService.findAllEmprestimosAbertosByUsuario(anyString())).thenReturn(List.of());
    when(systemConfigService.getEmailNadaConsta()).thenReturn("destino@utfpr.edu.br");
    NadaConsta nadaConsta =
        NadaConsta.builder()
            .id(6L)
            .usuario(usuario)
            .status(NadaConstaStatus.COMPLETED)
            .createdAt(LocalDateTime.now())
            .createdBy("user")
            .build();
    when(nadaConstaRepository.save(any())).thenReturn(nadaConsta);
    when(usuarioService.save(any(Usuario.class)))
        .thenThrow(new RuntimeException("User save error"));
    assertThrows(RuntimeException.class, () -> service.solicitarNadaConsta("666666"));
  }

  @Test
  void shouldHandleUserWithoutEmail() {
    Usuario usuario = Usuario.builder().id(7L).documento("777777").email(null).ativo(true).build();
    when(usuarioService.findByDocumento("777777")).thenReturn(usuario);
    when(emprestimoService.findAllEmprestimosAbertosByUsuario(anyString())).thenReturn(List.of());
    when(systemConfigService.getEmailNadaConsta()).thenReturn("destino@utfpr.edu.br");
    NadaConsta nadaConsta =
        NadaConsta.builder()
            .id(7L)
            .usuario(usuario)
            .status(NadaConstaStatus.COMPLETED)
            .createdAt(LocalDateTime.now())
            .createdBy("user")
            .build();
    when(nadaConstaRepository.save(any())).thenReturn(nadaConsta);
    when(usuarioService.save(any(Usuario.class))).thenReturn(usuario);
    NadaConstaResponseDto dto = service.solicitarNadaConsta("777777");
    assertNotNull(dto);
    verify(eventPublisher).publishEvent(any(NadaConstaEmitidoEvent.class));
    verify(usuarioService).save(any(Usuario.class));
  }

  @Test
  void shouldInvalidateCompletedNadaConstaAndReactivateUser() {
    Usuario usuario =
        Usuario.builder()
            .id(10L)
            .documento("101010")
            .email("user@utfpr.edu.br")
            .ativo(false)
            .build();
    NadaConsta nadaConsta =
        NadaConsta.builder().id(10L).usuario(usuario).status(NadaConstaStatus.COMPLETED).build();
    when(nadaConstaRepository.findById(10L)).thenReturn(java.util.Optional.of(nadaConsta));
    when(nadaConstaRepository.save(any())).thenReturn(nadaConsta);
    when(usuarioService.save(any(Usuario.class))).thenReturn(usuario);
    NadaConstaResponseDto dto = service.invalidarNadaConsta(10L);
    assertNotNull(dto);
    assertEquals(NadaConstaStatus.INVALIDATED, nadaConsta.getStatus());
    assertTrue(usuario.isAtivo());
    verify(nadaConstaRepository).save(nadaConsta);
    verify(usuarioService).save(usuario);
  }

  @Test
  void shouldThrowExceptionWhenInvalidatingNonCompletedNadaConsta() {
    Usuario usuario =
        Usuario.builder()
            .id(11L)
            .documento("111111")
            .email("user@utfpr.edu.br")
            .ativo(false)
            .build();
    NadaConsta nadaConsta =
        NadaConsta.builder().id(11L).usuario(usuario).status(NadaConstaStatus.PENDING).build();
    when(nadaConstaRepository.findById(11L)).thenReturn(java.util.Optional.of(nadaConsta));
    assertThrows(NadaConstaException.class, () -> service.invalidarNadaConsta(11L));
  }

  @Test
  void shouldThrowExceptionWhenInvalidatingNonexistentNadaConsta() {
    when(nadaConstaRepository.findById(99L)).thenReturn(java.util.Optional.empty());
    assertThrows(NadaConstaException.class, () -> service.invalidarNadaConsta(99L));
  }

  @Test
  void shouldCompletePendingNadaConstaWhenNoPendingLoans() {
    Usuario usuario =
        Usuario.builder()
            .id(12L)
            .documento("121212")
            .email("user@utfpr.edu.br")
            .ativo(true)
            .build();
    NadaConsta nadaConsta =
        NadaConsta.builder().id(12L).usuario(usuario).status(NadaConstaStatus.PENDING).build();
    when(nadaConstaRepository.findById(12L)).thenReturn(java.util.Optional.of(nadaConsta));
    when(emprestimoService.findAllEmprestimosAbertosByUsuario(usuario.getUsername()))
        .thenReturn(List.of());
    when(nadaConstaRepository.save(any())).thenReturn(nadaConsta);
    NadaConstaResponseDto dto = service.verificarPendenciasNadaConsta(12L);
    assertNotNull(dto);
    assertEquals(NadaConstaStatus.COMPLETED, nadaConsta.getStatus());
    verify(nadaConstaRepository).save(nadaConsta);
  }

  @Test
  void shouldThrowExceptionWhenVerifyingNonPendingNadaConsta() {
    Usuario usuario =
        Usuario.builder()
            .id(13L)
            .documento("131313")
            .email("user@utfpr.edu.br")
            .ativo(true)
            .build();
    NadaConsta nadaConsta =
        NadaConsta.builder().id(13L).usuario(usuario).status(NadaConstaStatus.COMPLETED).build();
    when(nadaConstaRepository.findById(13L)).thenReturn(java.util.Optional.of(nadaConsta));
    assertThrows(NadaConstaException.class, () -> service.verificarPendenciasNadaConsta(13L));
  }

  @Test
  void shouldThrowExceptionWhenVerifyingNonexistentNadaConsta() {
    when(nadaConstaRepository.findById(98L)).thenReturn(java.util.Optional.empty());
    assertThrows(NadaConstaException.class, () -> service.verificarPendenciasNadaConsta(98L));
  }
}
