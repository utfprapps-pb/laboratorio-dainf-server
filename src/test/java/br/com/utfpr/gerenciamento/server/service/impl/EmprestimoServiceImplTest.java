package br.com.utfpr.gerenciamento.server.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.dto.EmprestimoResponseDto;
import br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto;
import br.com.utfpr.gerenciamento.server.enumeration.StatusDevolucao;
import br.com.utfpr.gerenciamento.server.enumeration.TipoItem;
import br.com.utfpr.gerenciamento.server.exception.EntityNotFoundException;
import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.model.EmprestimoDevolucaoItem;
import br.com.utfpr.gerenciamento.server.model.EmprestimoItem;
import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardEmprestimoDia;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensEmprestados;
import br.com.utfpr.gerenciamento.server.model.filter.EmprestimoFilter;
import br.com.utfpr.gerenciamento.server.repository.EmprestimoRepository;
import br.com.utfpr.gerenciamento.server.repository.UsuarioRepository;
import br.com.utfpr.gerenciamento.server.service.EmprestimoService;
import br.com.utfpr.gerenciamento.server.service.ItemService;
import br.com.utfpr.gerenciamento.server.service.SaidaService;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import br.com.utfpr.gerenciamento.server.util.SecurityUtils;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class EmprestimoServiceImplTest {

  @Mock private EmprestimoRepository emprestimoRepository;

  @Mock private UsuarioService usuarioService;

  @Mock private ItemService itemService;

  @Mock private SaidaService saidaService;

  @Mock private ApplicationEventPublisher eventPublisher;

  @Mock private ModelMapper modelMapper;
  @Mock private UsuarioRepository usuarioRepository;
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
    emprestimo = new Emprestimo();
    emprestimo.setUsuarioEmprestimo(usuarioEmprestimo);
    emprestimo.setUsuarioResponsavel(usuarioResponsavel);
    emprestimo.setPrazoDevolucao(LocalDate.now().plusDays(5));
    emprestimo.setDataEmprestimo(LocalDate.now());
    // Fix for self-injection (cast to EmprestimoService if needed)
    try {
      Field selfField = EmprestimoServiceImpl.class.getDeclaredField("self");
      selfField.setAccessible(true);
      selfField.set(service, (EmprestimoService) service);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
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
    assertEquals(StatusDevolucao.P, result.getFirst().getStatusDevolucao());
    assertEquals(itemModel, result.getFirst().getItem());
    assertEquals(BigDecimal.valueOf(2), result.getFirst().getQtde());
    assertEquals(emprestimo, result.getFirst().getEmprestimo());
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
    emprestimo = new Emprestimo();
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

    novoEmprestimo.setEmprestimoItem(Collections.singleton(emprestimoItem));

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

  @Test
  void testSaveThrowsExceptionWhenUsuarioEmprestimoIsNull() {
    emprestimo = new Emprestimo();
    assertThrows(IllegalArgumentException.class, () -> service.save(emprestimo));
  }

  @Test
  void testSaveThrowsExceptionWhenUsuarioEmprestimoIdIsNull() {
    emprestimo = new Emprestimo();
    Usuario usuario = new Usuario();
    emprestimo.setUsuarioEmprestimo(usuario);
    assertThrows(IllegalArgumentException.class, () -> service.save(emprestimo));
  }

  @Test
  void testSaveThrowsEntityNotFoundExceptionWhenUsuarioEmprestimoNotFound() {
    emprestimo = new Emprestimo();
    usuarioEmprestimo = new Usuario();
    usuarioEmprestimo.setId(1L);
    emprestimo.setUsuarioEmprestimo(usuarioEmprestimo);
    when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());
    assertThrows(EntityNotFoundException.class, () -> service.save(emprestimo));
  }

  @Test
  void testDeleteByIdCallsSuperDelete() {
    doNothing().when(emprestimoRepository).deleteById(anyLong());
    service.delete(1L);
    verify(emprestimoRepository).deleteById(1L);
  }

  @Test
  void testDeleteByEntityCallsSuperDelete() {
    emprestimo = new Emprestimo();
    doNothing().when(emprestimoRepository).delete(emprestimo);
    service.delete(emprestimo);
    verify(emprestimoRepository).delete(emprestimo);
  }

  @Test
  void testProcessEmprestimoCallsFinalizeEmprestimoAndConvertToDto() {
    emprestimo = new Emprestimo();
    emprestimo.setEmprestimoItem(new HashSet<>());
    emprestimo.setEmprestimoDevolucaoItem(new ArrayList<>());
    EmprestimoResponseDto dto = new EmprestimoResponseDto();
    usuarioEmprestimo = new Usuario();
    usuarioEmprestimo.setId(1L);
    usuarioEmprestimo.setEmail("mail@test.com");
    emprestimo.setUsuarioEmprestimo(usuarioEmprestimo);
    usuarioResponsavel = new Usuario();
    usuarioResponsavel.setId(2L);
    usuarioResponsavel.setEmail("responsavel@test.com");
    emprestimo.setUsuarioResponsavel(usuarioResponsavel);
    doNothing().when(eventPublisher).publishEvent(any());
    when(emprestimoRepository.save(any(Emprestimo.class)))
        .thenAnswer(
            invocation -> {
              Emprestimo saved = invocation.getArgument(0);
              saved.setId(1L);
              return saved;
            });
    when(modelMapper.map(any(Emprestimo.class), eq(EmprestimoResponseDto.class))).thenReturn(dto);
    when(modelMapper.map(any(EmprestimoResponseDto.class), eq(Emprestimo.class)))
        .thenAnswer(
            invocation -> {
              EmprestimoResponseDto responseDto = invocation.getArgument(0);
              Emprestimo entity = new Emprestimo();
              entity.setId(responseDto.getId());
              entity.setEmprestimoItem(new HashSet<>());
              entity.setEmprestimoDevolucaoItem(new ArrayList<>());
              entity.setUsuarioEmprestimo(usuarioEmprestimo);
              entity.setUsuarioResponsavel(usuarioResponsavel);
              return entity;
            });
    try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
      mockedSecurity.when(SecurityUtils::getAuthenticatedUsername).thenReturn("testuser");
      EmprestimoResponseDto result = service.processEmprestimo(emprestimo, null);
      assertEquals(dto, result);
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  void testFindAllPagedWithTextFilterCallsFindAllSpecification() {
    Pageable pageable = PageRequest.of(0, 10);
    Specification<Emprestimo> spec = (Specification<Emprestimo>) mock(Specification.class);
    when(emprestimoRepository.findAll(any(Specification.class), eq(pageable)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));
    // Testa apenas o resultado final esperado
    Page<EmprestimoResponseDto> result = service.findAllSpecification(spec, pageable);
    assertNotNull(result);
  }

  @Test
  void testPrepareEmprestimoHandlesNulls() {
    // Caso 1: Emprestimo novo, sem itens
    Emprestimo emprestimoNovo = new Emprestimo();
    service.prepareEmprestimo(emprestimoNovo);
    assertNotNull(emprestimoNovo.getEmprestimoDevolucaoItem());
    assertEquals(0, emprestimoNovo.getEmprestimoDevolucaoItem().size());

    // Caso 2: Emprestimo com itens
    Emprestimo emprestimoComItens = new Emprestimo();
    Item itemModel = new Item();
    itemModel.setId(10L);
    itemModel.setTipoItem(TipoItem.C);
    EmprestimoItem empItem = new EmprestimoItem();
    empItem.setItem(itemModel);
    empItem.setQtde(java.math.BigDecimal.ONE);
    java.util.Set<EmprestimoItem> itensSet = new HashSet<>();
    itensSet.add(empItem);
    emprestimoComItens.setEmprestimoItem(itensSet);
    when(itemService.saldoItemIsValid(any(), any())).thenReturn(true);
    service.prepareEmprestimo(emprestimoComItens);
    assertNotNull(emprestimoComItens.getEmprestimoDevolucaoItem());
    assertFalse(emprestimoComItens.getEmprestimoDevolucaoItem().isEmpty());
  }

  @Test
  void testFinalizeEmprestimoHandlesNulls() {
    emprestimo = new Emprestimo();
    // Inicializa o usuário do empréstimo para evitar NullPointerException
    usuarioEmprestimo = new Usuario();
    usuarioEmprestimo.setEmail("mail@test.com");
    emprestimo.setUsuarioEmprestimo(usuarioEmprestimo);

    // Test that the method completes without throwing an exception
    assertDoesNotThrow(() -> service.finalizeEmprestimo(emprestimo));

    // Verify that the event publisher was called (email should be sent)
    verify(eventPublisher).publishEvent(any());
  }

  @Test
  void testFinalizeEmprestimo_ApenasItensConsumiveisDiminuemSaldo() {
    // Given - Empréstimo com itens consumíveis e permanentes
    emprestimo = new Emprestimo();
    usuarioEmprestimo = new Usuario();
    usuarioEmprestimo.setEmail("mail@test.com");
    emprestimo.setUsuarioEmprestimo(usuarioEmprestimo);

    // Item Consumível (C)
    Item itemConsumivel = new Item();
    itemConsumivel.setId(1L);
    itemConsumivel.setTipoItem(TipoItem.C);

    EmprestimoItem emprestimoItemConsumivel = new EmprestimoItem();
    emprestimoItemConsumivel.setItem(itemConsumivel);
    emprestimoItemConsumivel.setQtde(BigDecimal.valueOf(5));
    emprestimoItemConsumivel.setEmprestimo(emprestimo);

    // Item Permanente (P)
    Item itemPermanente = new Item();
    itemPermanente.setId(2L);
    itemPermanente.setTipoItem(TipoItem.P);

    EmprestimoItem emprestimoItemPermanente = new EmprestimoItem();
    emprestimoItemPermanente.setItem(itemPermanente);
    emprestimoItemPermanente.setQtde(BigDecimal.valueOf(3));
    emprestimoItemPermanente.setEmprestimo(emprestimo);

    // Adiciona ambos os itens ao empréstimo
    java.util.Set<EmprestimoItem> itens = new HashSet<>();
    itens.add(emprestimoItemConsumivel);
    itens.add(emprestimoItemPermanente);
    emprestimo.setEmprestimoItem(itens);

    // When
    service.finalizeEmprestimo(emprestimo);

    // Then - Apenas o item consumível deve ter saldo diminuído
    verify(itemService).diminuiSaldoItem(1L, BigDecimal.valueOf(5), true);
    verify(itemService, never()).diminuiSaldoItem(eq(2L), any(), anyBoolean());
    verify(eventPublisher).publishEvent(any());
  }

  @Test
  void testFinalizeEmprestimo_SomenteItensPermanentes_NaoDiminuiSaldo() {
    // Given - Empréstimo apenas com itens permanentes
    emprestimo = new Emprestimo();
    usuarioEmprestimo = new Usuario();
    usuarioEmprestimo.setEmail("mail@test.com");
    emprestimo.setUsuarioEmprestimo(usuarioEmprestimo);

    Item itemPermanente = new Item();
    itemPermanente.setId(1L);
    itemPermanente.setTipoItem(TipoItem.P);

    EmprestimoItem emprestimoItem = new EmprestimoItem();
    emprestimoItem.setItem(itemPermanente);
    emprestimoItem.setQtde(BigDecimal.valueOf(2));
    emprestimoItem.setEmprestimo(emprestimo);

    emprestimo.setEmprestimoItem(Collections.singleton(emprestimoItem));

    // When
    service.finalizeEmprestimo(emprestimo);

    // Then - Nenhum item deve ter saldo diminuído
    verify(itemService, never()).diminuiSaldoItem(any(), any(), anyBoolean());
    verify(eventPublisher).publishEvent(any());
  }

  @Test
  void testCleanupAfterDeleteHandlesNulls() {
    emprestimo = new Emprestimo();
    emprestimo.setId(1L);

    // Test that the method completes without throwing an exception
    assertDoesNotThrow(() -> service.cleanupAfterDelete(emprestimo));

    // Verify that the saidaService.deleteSaidaByEmprestimo was called (this is what
    // cleanupAfterDelete does)
    verify(saidaService).deleteSaidaByEmprestimo(1L);
  }

  @Test
  void testFindAllByItemId() {
    // Given
    Long itemId = 1L;
    List<Emprestimo> emprestimos = Collections.singletonList(emprestimo);

    when(emprestimoRepository.findAllByItemId(itemId)).thenReturn(emprestimos);
    doReturn(emprestimoDto).when(service).toDto(emprestimo);

    // When
    List<EmprestimoResponseDto> result = service.findAllByItemId(itemId);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    verify(emprestimoRepository).findAllByItemId(itemId);
    verify(service).toDto(emprestimo);
  }
}
