package br.com.utfpr.gerenciamento.server.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.dto.EmprestimoResponseDto;
import br.com.utfpr.gerenciamento.server.dto.NadaConstaResponseDto;
import br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto;
import br.com.utfpr.gerenciamento.server.enumeration.NadaConstaStatus;
import br.com.utfpr.gerenciamento.server.event.nadaConsta.NadaConstaEmitidoEvent;
import br.com.utfpr.gerenciamento.server.event.nadaConsta.NadaConstaPendenciasEvent;
import br.com.utfpr.gerenciamento.server.exception.NadaConstaException;
import br.com.utfpr.gerenciamento.server.fixture.MockFactory;
import br.com.utfpr.gerenciamento.server.fixture.UsuarioFactory;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class NadaConstaServiceImplTest {
  private final UsuarioFactory usuarioFactory = new UsuarioFactory();
  private final MockFactory mockFactory = new MockFactory();

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
    eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
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
    // Mock padrão para hasSolicitacaoNadaConstaPendingOrCompleted
    when(usuarioService.hasSolicitacaoNadaConstaPendingOrCompleted(anyString())).thenReturn(false);
  }

  static Stream<Arguments> exceptionScenarios() {
    return Stream.of(
        Arguments.of("Repository Error", "555555", "nadaConstaRepository.save"),
        Arguments.of("UsuarioService Error", "666666", "usuarioService.save"));
  }

  static Stream<Arguments> nullHandlingScenarios() {
    return Stream.of(
        Arguments.of("Null Emprestimos List", "222222", null, "shouldPublishEvent"),
        Arguments.of("Null System Config Email", "333333", List.of(), "shouldNotPublishEvent"),
        Arguments.of("Null ModelMapper Result", "444444", List.of(), "shouldPublishEvent"),
        Arguments.of("Null User Email", "777777", List.of(), "shouldPublishEvent"));
  }

  @ParameterizedTest(name = "shouldThrowExceptionWhen{0}")
  @MethodSource("exceptionScenarios")
  void shouldThrowExceptionWhenSystemFails(
      String scenarioName, String documento, String mockMethod) {
    Usuario usuario = usuarioFactory.criarUsuarioBasico(documento, "Test User");
    usuario.setId(Long.parseLong(documento.substring(0, 1)));

    mockFactory.configurarUsuarioServicePorDocumento(usuarioService, documento, usuario);
    when(emprestimoService.findAllEmprestimosAbertosByUsuario(anyString())).thenReturn(List.of());
    when(systemConfigService.getEmailNadaConsta()).thenReturn("destino@utfpr.edu.br");

    if (mockMethod.equals("nadaConstaRepository.save")) {
      when(nadaConstaRepository.save(any())).thenThrow(new RuntimeException("DB error"));
    } else if (mockMethod.equals("usuarioService.save")) {
      NadaConsta nadaConsta =
          NadaConsta.builder()
              .id(Long.parseLong(documento.substring(0, 1)))
              .usuario(usuario)
              .status(NadaConstaStatus.COMPLETED)
              .createdAt(LocalDateTime.now())
              .createdBy("user")
              .build();
      when(nadaConstaRepository.save(any())).thenReturn(nadaConsta);
      when(usuarioService.save(any(Usuario.class)))
          .thenThrow(new RuntimeException("User save error"));
    }

    assertThrows(RuntimeException.class, () -> service.solicitarNadaConsta(documento));
  }

  @ParameterizedTest(name = "shouldHandle{0}")
  @MethodSource("nullHandlingScenarios")
  void shouldHandleNullScenarios(
      String scenarioName, String documento, List<?> emprestimos, String eventBehavior) {
    Usuario usuario;
    if (scenarioName.equals("Null User Email")) {
      usuario = usuarioFactory.criarUsuarioSemEmail(documento, "Test User");
    } else {
      usuario = usuarioFactory.criarUsuarioBasico(documento, "Test User");
    }
    usuario.setId(Long.parseLong(documento.substring(0, 1)));

    mockFactory.configurarUsuarioServicePorDocumento(usuarioService, documento, usuario);
    when(emprestimoService.findAllEmprestimosAbertosByUsuario(anyString()))
        .thenReturn((List) emprestimos);
    when(systemConfigService.getEmailNadaConsta()).thenReturn("destino@utfpr.edu.br");

    if (scenarioName.equals("Null System Config Email")) {
      when(systemConfigService.getEmailNadaConsta()).thenReturn(null);
    }

    if (scenarioName.equals("Null ModelMapper Result")) {
      when(modelMapper.map(any(), eq(NadaConstaResponseDto.class))).thenReturn(null);
    }

    NadaConsta nadaConsta =
        NadaConsta.builder()
            .id(Long.parseLong(documento.substring(0, 1)))
            .usuario(usuario)
            .status(NadaConstaStatus.COMPLETED)
            .createdAt(LocalDateTime.now())
            .createdBy("user")
            .build();
    when(nadaConstaRepository.save(any())).thenReturn(nadaConsta);
    when(usuarioService.save(any(Usuario.class)))
        .thenReturn(usuarioFactory.criarUsuarioResponseDto(usuario));

    NadaConstaResponseDto dto = service.solicitarNadaConsta(documento);

    if (scenarioName.equals("Null ModelMapper Result")) {
      assertNull(dto);
    } else {
      assertNotNull(dto);
    }

    if (eventBehavior.equals("shouldPublishEvent")) {
      verify(eventPublisher).publishEvent(any(NadaConstaEmitidoEvent.class));
    } else {
      verify(eventPublisher, never()).publishEvent(any(NadaConstaEmitidoEvent.class));
    }

    verify(usuarioService).save(any(Usuario.class));
  }

  @Test
  void shouldSendDeclarationAndDeactivateUserWhenNoPendingLoans() {
    Usuario usuario = usuarioFactory.criarUsuarioBasico("123456", "Aluno Teste");
    usuario.setId(1L);

    mockFactory.configurarUsuarioServicePorDocumento(usuarioService, "123456", usuario);
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
    when(usuarioService.save(any(Usuario.class)))
        .thenReturn(usuarioFactory.criarUsuarioResponseDto(usuario));
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
    emprestimo.setEmprestimoItem(Set.of(emprestimoItem));
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
    // O nome do item é "Notebook" conforme simulado
    assertEquals("Notebook", itens.getFirst().get("itemNome"));
  }

  @Test
  void shouldThrowExceptionWhenUserNotFound() {
    when(usuarioService.findByDocumento("999999")).thenReturn(null);
    assertThrows(RuntimeException.class, () -> service.solicitarNadaConsta("999999"));
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
    when(nadaConstaRepository.findById(10L)).thenReturn(Optional.of(nadaConsta));
    when(nadaConstaRepository.save(any())).thenReturn(nadaConsta);
    when(usuarioService.save(any(Usuario.class)))
        .thenReturn(usuarioFactory.criarUsuarioResponseDto(usuario));
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
