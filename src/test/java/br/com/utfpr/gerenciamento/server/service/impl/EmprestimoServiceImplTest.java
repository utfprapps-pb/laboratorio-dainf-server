package br.com.utfpr.gerenciamento.server.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.dto.EmprestimoResponseDto;
import br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto;
import br.com.utfpr.gerenciamento.server.enumeration.StatusDevolucao;
import br.com.utfpr.gerenciamento.server.enumeration.TipoItem;
import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.model.EmprestimoDevolucaoItem;
import br.com.utfpr.gerenciamento.server.model.EmprestimoItem;
import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardEmprestimoDia;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensEmprestados;
import br.com.utfpr.gerenciamento.server.model.filter.EmprestimoFilter;
import br.com.utfpr.gerenciamento.server.repository.EmprestimoRepository;
import br.com.utfpr.gerenciamento.server.service.ItemService;
import br.com.utfpr.gerenciamento.server.service.ReservaService;
import br.com.utfpr.gerenciamento.server.service.SaidaService;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class EmprestimoServiceImplTest {

  @Mock private EmprestimoRepository emprestimoRepository;

  @Mock private UsuarioService usuarioService;

  @Mock private ItemService itemService;

  @Mock private SaidaService saidaService;

  @Mock private ReservaService reservaService;

  @Mock private ApplicationEventPublisher eventPublisher;

  @Mock private ModelMapper modelMapper;

  @Spy @InjectMocks private EmprestimoServiceImpl service;

  private Emprestimo emprestimo;
  private EmprestimoResponseDto emprestimoDto;
  private Usuario usuarioEmprestimo;
  private Usuario usuarioResponsavel;
  private UsuarioResponseDto usuarioDto;

  @BeforeEach
  void setUp() {
    // Setup Usuários
    usuarioEmprestimo = new Usuario();
    usuarioEmprestimo.setId(1L);
    usuarioEmprestimo.setEmail("usuario@test.com");
    usuarioEmprestimo.setNome("Usuário Teste");

    usuarioResponsavel = new Usuario();
    usuarioResponsavel.setId(2L);
    usuarioResponsavel.setNome("Responsavel Teste");
    usuarioResponsavel.setEmail("responsavel@test.com");

    usuarioDto = new UsuarioResponseDto();
    usuarioDto.setId(1L);
    usuarioDto.setEmail("usuario@test.com");
    usuarioDto.setNome("Usuário Teste");

    // Setup Empréstimo
    emprestimo = new Emprestimo();
    emprestimo.setId(1L);
    emprestimo.setUsuarioEmprestimo(usuarioEmprestimo);
    emprestimo.setUsuarioResponsavel(usuarioResponsavel);
    emprestimo.setPrazoDevolucao(LocalDate.now().plusDays(5));
    emprestimo.setDataEmprestimo(LocalDate.now());

    // Setup DTO
    emprestimoDto = new EmprestimoResponseDto();
    emprestimoDto.setId(1L);
    emprestimoDto.setPrazoDevolucao(LocalDate.now().plusDays(5));
    emprestimoDto.setDataEmprestimo(LocalDate.now());
    emprestimoDto.setUsuarioEmprestimo(usuarioDto);
  }

  @Test
  void testFindAllByDataEmprestimoBetween() {
    // Given
    LocalDate ini = LocalDate.now();
    LocalDate fim = ini.plusDays(1);
    List<Emprestimo> emprestimos = Collections.singletonList(emprestimo);

    when(emprestimoRepository.findAllByDataEmprestimoBetween(ini, fim)).thenReturn(emprestimos);
    doReturn(emprestimoDto).when(service).toDto(emprestimo);

    // When
    List<EmprestimoResponseDto> result = service.findAllByDataEmprestimoBetween(ini, fim);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    verify(emprestimoRepository).findAllByDataEmprestimoBetween(ini, fim);
    verify(service).toDto(emprestimo);
  }

  @Test
  void testCountByDataEmprestimo() {
    // Given
    LocalDate ini = LocalDate.now();
    LocalDate fim = ini.plusDays(1);
    List<DashboardEmprestimoDia> expected = Collections.emptyList();

    when(emprestimoRepository.countByDataEmprestimo(ini, fim)).thenReturn(expected);

    // When
    List<DashboardEmprestimoDia> result = service.countByDataEmprestimo(ini, fim);

    // Then
    assertNotNull(result);
    assertEquals(expected, result);
    verify(emprestimoRepository).countByDataEmprestimo(ini, fim);
  }

  @Test
  void testFindItensMaisEmprestados() {
    // Given
    LocalDate ini = LocalDate.now();
    LocalDate fim = ini.plusDays(1);
    List<DashboardItensEmprestados> expected = Collections.emptyList();

    when(emprestimoRepository.findItensMaisEmprestados(ini, fim)).thenReturn(expected);

    // When
    List<DashboardItensEmprestados> result = service.findItensMaisEmprestados(ini, fim);

    // Then
    assertNotNull(result);
    assertEquals(expected, result);
    verify(emprestimoRepository).findItensMaisEmprestados(ini, fim);
  }

  @Test
  void testCreateEmprestimoItemDevolucao_ComItemConsumivel() {
    // Given
    Item itemModel = new Item();
    itemModel.setId(1L);
    itemModel.setTipoItem(TipoItem.C); // Consumível

    EmprestimoItem item = new EmprestimoItem();
    item.setItem(itemModel);
    item.setQtde(BigDecimal.valueOf(2));
    item.setEmprestimo(emprestimo);

    List<EmprestimoItem> itens = Collections.singletonList(item);

    // When
    List<EmprestimoDevolucaoItem> result = service.createEmprestimoItemDevolucao(itens);

    // Then
    assertEquals(1, result.size());
    assertEquals(StatusDevolucao.P, result.get(0).getStatusDevolucao());
    assertEquals(itemModel, result.get(0).getItem());
    assertEquals(BigDecimal.valueOf(2), result.get(0).getQtde());
    assertEquals(emprestimo, result.get(0).getEmprestimo());
  }

  @Test
  void testCreateEmprestimoItemDevolucao_ComItemPermanente() {
    // Given
    Item itemModel = new Item();
    itemModel.setId(1L);
    itemModel.setTipoItem(TipoItem.P); // Permanente - não deve criar devolução

    EmprestimoItem item = new EmprestimoItem();
    item.setItem(itemModel);
    item.setQtde(BigDecimal.valueOf(2));
    item.setEmprestimo(emprestimo);

    List<EmprestimoItem> itens = Collections.singletonList(item);

    // When
    List<EmprestimoDevolucaoItem> result = service.createEmprestimoItemDevolucao(itens);

    // Then
    assertTrue(result.isEmpty());
  }

  @Test
  void testCreateEmprestimoItemDevolucao_ComListaNull() {
    // When
    List<EmprestimoDevolucaoItem> result = service.createEmprestimoItemDevolucao(null);

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void testFilter() {
    // Given
    EmprestimoFilter filter = new EmprestimoFilter();
    List<Emprestimo> emprestimos = Collections.singletonList(emprestimo);

    when(emprestimoRepository.findAll(any(Specification.class), any(Sort.class)))
        .thenReturn(emprestimos);
    doReturn(emprestimoDto).when(service).toDto(emprestimo);

    // When
    List<EmprestimoResponseDto> result = service.filter(filter);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    verify(emprestimoRepository).findAll(any(Specification.class), any(Sort.class));
    verify(service).toDto(emprestimo);
  }

  @Test
  void testFindAllUsuarioEmprestimo() {
    // Given
    String username = "usuario@test.com";
    List<Emprestimo> emprestimos = Collections.singletonList(emprestimo);

    when(usuarioService.findByUsername(username)).thenReturn(usuarioDto);
    when(usuarioService.toEntity(usuarioDto)).thenReturn(usuarioEmprestimo);
    when(emprestimoRepository.findAllByUsuarioEmprestimo(usuarioEmprestimo))
        .thenReturn(emprestimos);
    doReturn(emprestimoDto).when(service).toDto(emprestimo);

    // When
    List<EmprestimoResponseDto> result = service.findAllUsuarioEmprestimo(username);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    verify(usuarioService).findByUsername(username);
    verify(emprestimoRepository).findAllByUsuarioEmprestimo(usuarioEmprestimo);
  }

  @Test
  void testFindAllEmprestimosAbertos() {
    // Given
    List<Emprestimo> emprestimos = Collections.singletonList(emprestimo);

    when(emprestimoRepository.findAllByDataDevolucaoIsNullOrderById()).thenReturn(emprestimos);
    doReturn(emprestimoDto).when(service).toDto(emprestimo);

    // When
    List<EmprestimoResponseDto> result = service.findAllEmprestimosAbertos();

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    verify(emprestimoRepository).findAllByDataDevolucaoIsNullOrderById();
  }

  @Test
  void testFindAllEmprestimosAbertosByUsuario() {
    // Given
    String username = "usuario@test.com";
    List<Emprestimo> emprestimos = Collections.singletonList(emprestimo);

    when(usuarioService.findByUsername(username)).thenReturn(usuarioDto);
    when(usuarioService.toEntity(usuarioDto)).thenReturn(usuarioEmprestimo);
    when(emprestimoRepository.findAllByUsuarioEmprestimoAndDataDevolucaoIsNull(usuarioEmprestimo))
        .thenReturn(emprestimos);
    doReturn(emprestimoDto).when(service).toDto(emprestimo);

    // When
    List<EmprestimoResponseDto> result = service.findAllEmprestimosAbertosByUsuario(username);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    verify(usuarioService).findByUsername(username);
    verify(emprestimoRepository)
        .findAllByUsuarioEmprestimoAndDataDevolucaoIsNull(usuarioEmprestimo);
  }

  @Test
  void testChangePrazoDevolucao_FixUsingRepository() {
    // Given
    Long emprestimoId = 1L;
    LocalDate novaData = LocalDate.now().plusDays(10);

    // Ensure repository findById returns the entity so CrudServiceImpl.findOne doesn't throw
    when(emprestimoRepository.findById(emprestimoId)).thenReturn(Optional.of(emprestimo));

    // Spy allows stubbing toDto/toEntity/save
    doReturn(emprestimoDto).when(service).toDto(emprestimo);
    doReturn(emprestimo).when(service).toEntity(emprestimoDto);
    doReturn(emprestimoDto).when(service).save(any(Emprestimo.class));

    // When
    service.changePrazoDevolucao(emprestimoId, novaData);

    // Then
    verify(emprestimoRepository).findById(emprestimoId);
    verify(service).save(any(Emprestimo.class));
    verify(eventPublisher).publishEvent(any());
  }

  @Test
  void testSendEmailConfirmacaoEmprestimo_ComItensDevolucao() {
    // Given
    emprestimo.setEmprestimoDevolucaoItem(Collections.singletonList(new EmprestimoDevolucaoItem()));

    // When
    service.sendEmailConfirmacaoEmprestimo(emprestimo);

    // Then
    verify(eventPublisher).publishEvent(any());
  }

  @Test
  void testSendEmailConfirmacaoEmprestimo_SemItensDevolucao() {
    // Given
    emprestimo.setEmprestimoDevolucaoItem(Collections.emptyList());

    // When
    service.sendEmailConfirmacaoEmprestimo(emprestimo);

    // Then
    verify(eventPublisher).publishEvent(any());
  }

  @Test
  void testSendEmailConfirmacaoEmprestimo_ComListaDevolucaoNull() {
    // Given
    emprestimo.setEmprestimoDevolucaoItem(null);

    // When
    service.sendEmailConfirmacaoEmprestimo(emprestimo);

    // Then
    verify(eventPublisher).publishEvent(any());
  }

  @Test
  void testSendEmailConfirmacaoDevolucao() {
    // When
    service.sendEmailConfirmacaoDevolucao(emprestimo);

    // Then
    verify(eventPublisher).publishEvent(any());
  }

  @Test
  void testSendEmailPrazoDevolucaoProximo_ComEmprestimos() {
    // Given
    List<Emprestimo> emprestimos = Collections.singletonList(emprestimo);

    when(emprestimoRepository.findByDataDevolucaoIsNullAndPrazoDevolucaoEquals(
            any(LocalDate.class)))
        .thenReturn(emprestimos);

    // When
    service.sendEmailPrazoDevolucaoProximo();

    // Then
    verify(emprestimoRepository)
        .findByDataDevolucaoIsNullAndPrazoDevolucaoEquals(any(LocalDate.class));
    verify(eventPublisher, atLeastOnce()).publishEvent(any());
  }

  @Test
  void testSendEmailPrazoDevolucaoProximo_SemEmprestimos() {
    // Given
    when(emprestimoRepository.findByDataDevolucaoIsNullAndPrazoDevolucaoEquals(
            any(LocalDate.class)))
        .thenReturn(Collections.emptyList());

    // When
    service.sendEmailPrazoDevolucaoProximo();

    // Then
    verify(emprestimoRepository)
        .findByDataDevolucaoIsNullAndPrazoDevolucaoEquals(any(LocalDate.class));
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void testToDto() {
    // Given
    Emprestimo emprestimo = new Emprestimo();
    EmprestimoResponseDto expectedDto = new EmprestimoResponseDto();

    when(modelMapper.map(emprestimo, EmprestimoResponseDto.class)).thenReturn(expectedDto);

    // When
    EmprestimoResponseDto result = service.toDto(emprestimo);

    // Then
    assertEquals(expectedDto, result);
    verify(modelMapper).map(emprestimo, EmprestimoResponseDto.class);
  }

  @Test
  void testToEntity() {
    // Given
    EmprestimoResponseDto dto = new EmprestimoResponseDto();
    Emprestimo expectedEmprestimo = new Emprestimo();

    when(modelMapper.map(dto, Emprestimo.class)).thenReturn(expectedEmprestimo);

    // When
    Emprestimo result = service.toEntity(dto);

    // Then
    assertEquals(expectedEmprestimo, result);
    verify(modelMapper).map(dto, Emprestimo.class);
  }

  @Test
  void testPrepareEmprestimo_NovoEmprestimo() {
    // Given - Empréstimo novo (sem ID)
    Emprestimo novoEmprestimo = new Emprestimo();
    novoEmprestimo.setId(null);

    Item item = new Item();
    item.setId(1L);
    item.setTipoItem(TipoItem.P);

    EmprestimoItem emprestimoItem = new EmprestimoItem();
    emprestimoItem.setItem(item);
    emprestimoItem.setQtde(BigDecimal.ONE);

    novoEmprestimo.setEmprestimoItem(Collections.singletonList(emprestimoItem));

    // Mock do itemService para evitar NullPointerException
    when(itemService.getSaldoItem(1L)).thenReturn(BigDecimal.TEN);
    when(itemService.saldoItemIsValid(BigDecimal.TEN, BigDecimal.ONE)).thenReturn(true);

    // When
    service.prepareEmprestimo(novoEmprestimo);

    // Then - Não deve tentar restaurar saldo de empréstimo antigo
    assertNotNull(novoEmprestimo.getEmprestimoDevolucaoItem());
  }

  @Test
  void testCleanupAfterDelete_ComEmprestimoNull() {
    // When
    service.cleanupAfterDelete(null);

    // Then - Não deve lançar exceção
    assertDoesNotThrow(() -> service.cleanupAfterDelete(null));
  }

  @Test
  void testCreateEmprestimoItemDevolucao_ComItemNull() {
    // Given
    EmprestimoItem itemComItemNull = new EmprestimoItem();
    itemComItemNull.setItem(null); // Item é null
    itemComItemNull.setQtde(BigDecimal.ONE);

    List<EmprestimoItem> itens = Collections.singletonList(itemComItemNull);

    // When
    List<EmprestimoDevolucaoItem> result = service.createEmprestimoItemDevolucao(itens);

    // Then - Deve ignorar itens com item null
    assertTrue(result.isEmpty());
  }

  @Test
  void testCreateEmprestimoItemDevolucao_ComEmprestimoItemNull() {
    // Given
    List<EmprestimoItem> itens = Collections.singletonList(null);

    // When
    List<EmprestimoDevolucaoItem> result = service.createEmprestimoItemDevolucao(itens);

    // Then - Deve ignorar itens null
    assertTrue(result.isEmpty());
  }
}
