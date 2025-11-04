package br.com.utfpr.gerenciamento.server.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.dto.EmprestimoResponseDto;
import br.com.utfpr.gerenciamento.server.enumeration.StatusDevolucao;
import br.com.utfpr.gerenciamento.server.enumeration.TipoItem;
import br.com.utfpr.gerenciamento.server.exception.EntityNotFoundException;
import br.com.utfpr.gerenciamento.server.model.*;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class EmprestimoServiceImplTest {
  @Mock private EmprestimoRepository emprestimoRepository;
  @Mock private UsuarioService usuarioService;
  @Mock private ApplicationEventPublisher eventPublisher;
  @Mock private ModelMapper modelMapper;
  @Mock private ItemService itemService;
  @Mock private SaidaService saidaService;
  @Mock private UsuarioRepository usuarioRepository;
  @InjectMocks private EmprestimoServiceImpl service;

  private Emprestimo emp;
  private Usuario usuarioResponsavel;

  @BeforeEach
  void setUp() {
    Usuario usuarioEmprestimo = new Usuario();
    usuarioEmprestimo.setEmail("mail@test.com");
    usuarioResponsavel = new Usuario();
    usuarioResponsavel.setNome("Responsavel Teste");
    emp = new Emprestimo();
    emp.setUsuarioEmprestimo(usuarioEmprestimo);
    emp.setUsuarioResponsavel(usuarioResponsavel);
    emp.setPrazoDevolucao(LocalDate.now().plusDays(5));
    emp.setDataEmprestimo(LocalDate.now());
    // Fix for self-injection (cast to EmprestimoService if needed)
    try {
      Field selfField = EmprestimoServiceImpl.class.getDeclaredField("self");
      selfField.setAccessible(true);
      selfField.set(service, (EmprestimoService) service);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @AfterEach
  void tearDown() {}

  @Test
  void testFindAllByDataEmprestimoBetween() {
    LocalDate ini = LocalDate.now();
    LocalDate fim = ini.plusDays(1);
    List<Emprestimo> expected = Collections.singletonList(new Emprestimo());
    when(emprestimoRepository.findAllByDataEmprestimoBetween(ini, fim)).thenReturn(expected);
    List<Emprestimo> result = service.findAllByDataEmprestimoBetween(ini, fim);
    assertEquals(expected, result);
  }

  @Test
  void testCountByDataEmprestimo() {
    LocalDate ini = LocalDate.now();
    LocalDate fim = ini.plusDays(1);
    List<DashboardEmprestimoDia> expected = Collections.emptyList();
    when(emprestimoRepository.countByDataEmprestimo(ini, fim)).thenReturn(expected);
    List<DashboardEmprestimoDia> result = service.countByDataEmprestimo(ini, fim);
    assertEquals(expected, result);
  }

  @Test
  void testFindItensMaisEmprestados() {
    LocalDate ini = LocalDate.now();
    LocalDate fim = ini.plusDays(1);
    List<DashboardItensEmprestados> expected = Collections.emptyList();
    when(emprestimoRepository.findItensMaisEmprestados(ini, fim)).thenReturn(expected);
    List<DashboardItensEmprestados> result = service.findItensMaisEmprestados(ini, fim);
    assertEquals(expected, result);
  }

  @Test
  void testCreateEmprestimoItemDevolucao() {
    br.com.utfpr.gerenciamento.server.model.Item itemModel =
        new br.com.utfpr.gerenciamento.server.model.Item();
    itemModel.setTipoItem(TipoItem.C);
    Emprestimo emprestimo = new Emprestimo();
    EmprestimoItem item = new EmprestimoItem();
    item.setItem(itemModel);
    item.setQtde(BigDecimal.valueOf(2));
    item.setEmprestimo(emprestimo);
    List<EmprestimoDevolucaoItem> result =
        service.createEmprestimoItemDevolucao(Collections.singletonList(item));
    assertEquals(1, result.size());
    assertEquals(
        StatusDevolucao.P, result.getFirst().getStatusDevolucao()); // Use getFirst() for clarity
  }

  @Test
  void testFilter() {
    EmprestimoFilter filter = new EmprestimoFilter();
    List<Emprestimo> expected = Collections.emptyList();
    when(emprestimoRepository.findAll(any(Specification.class), any(Sort.class)))
        .thenReturn(expected);
    List<Emprestimo> result = service.filter(filter);
    assertEquals(expected, result);
  }

  @Test
  void testFindAllUsuarioEmprestimo() {
    Usuario usuario = new Usuario();
    List<Emprestimo> expected = Collections.emptyList();
    when(usuarioService.findByUsername(anyString())).thenReturn(usuario);
    when(emprestimoRepository.findAllByUsuarioEmprestimo(usuario)).thenReturn(expected);
    List<Emprestimo> result = service.findAllUsuarioEmprestimo("user");
    assertEquals(expected, result);
  }

  @Test
  void testFindAllEmprestimosAbertos() {
    List<Emprestimo> expected = Collections.emptyList();
    when(emprestimoRepository.findAllByDataDevolucaoIsNullOrderById()).thenReturn(expected);
    List<Emprestimo> result = service.findAllEmprestimosAbertos();
    assertEquals(expected, result);
  }

  @Test
  void testFindAllEmprestimosAbertosByUsuario() {
    Usuario usuario = new Usuario();
    List<Emprestimo> expected = Collections.emptyList();
    when(usuarioService.findByUsername(anyString())).thenReturn(usuario);
    when(emprestimoRepository.findAllByUsuarioEmprestimoAndDataDevolucaoIsNull(usuario))
        .thenReturn(expected);
    List<Emprestimo> result = service.findAllEmprestimosAbertosByUsuario("user");
    assertEquals(expected, result);
  }

  @Test
  void testChangePrazoDevolucao() {
    emp.setUsuarioResponsavel(usuarioResponsavel);
    assertNotNull(emp.getUsuarioResponsavel());
    // Mock correto: repositório findById ao invés de service.findOne
    when(emprestimoRepository.findById(anyLong())).thenReturn(java.util.Optional.of(emp));
    when(emprestimoRepository.save(any(Emprestimo.class))).thenReturn(emp);
    service.changePrazoDevolucao(1L, LocalDate.now());
    verify(eventPublisher).publishEvent(any());
  }

  @Test
  void testSendEmailConfirmacaoEmprestimoWithDevolucaoItem() {
    emp.setUsuarioResponsavel(usuarioResponsavel);
    emp.setEmprestimoDevolucaoItem(Collections.singletonList(new EmprestimoDevolucaoItem()));
    assertNotNull(emp.getUsuarioResponsavel());
    service.sendEmailConfirmacaoEmprestimo(emp);
    verify(eventPublisher).publishEvent(any());
  }

  @Test
  void testSendEmailConfirmacaoEmprestimoWithoutDevolucaoItem() {
    emp.setUsuarioResponsavel(usuarioResponsavel);
    emp.setEmprestimoDevolucaoItem(Collections.emptyList());
    assertNotNull(emp.getUsuarioResponsavel());
    service.sendEmailConfirmacaoEmprestimo(emp);
    verify(eventPublisher).publishEvent(any());
  }

  @Test
  void testSendEmailConfirmacaoDevolucao() {
    emp.setUsuarioResponsavel(usuarioResponsavel);
    emp.setDataDevolucao(LocalDate.now());
    assertNotNull(emp.getUsuarioResponsavel());
    service.sendEmailConfirmacaoDevolucao(emp);
    verify(eventPublisher).publishEvent(any());
  }

  @Test
  void testSendEmailPrazoDevolucaoProximoWithEmprestimos() {
    emp.setUsuarioResponsavel(usuarioResponsavel);
    emp.setPrazoDevolucao(LocalDate.now().plusDays(1));
    assertNotNull(emp.getUsuarioResponsavel());
    List<Emprestimo> emprestimos = Collections.singletonList(emp);
    when(emprestimoRepository.findByDataDevolucaoIsNullAndPrazoDevolucaoEquals(
            any(LocalDate.class)))
        .thenReturn(emprestimos);
    service.sendEmailPrazoDevolucaoProximo();
    verify(eventPublisher).publishEvent(any());
  }

  @Test
  void testSendEmailPrazoDevolucaoProximoWithoutEmprestimos() {
    when(emprestimoRepository.findByDataDevolucaoIsNullAndPrazoDevolucaoEquals(
            any(LocalDate.class)))
        .thenReturn(Collections.emptyList());
    service.sendEmailPrazoDevolucaoProximo();
    // No event should be published
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void testConvertToDto() {
    Emprestimo emprestimo = new Emprestimo();
    EmprestimoResponseDto dto = new EmprestimoResponseDto();
    when(modelMapper.map(emprestimo, EmprestimoResponseDto.class)).thenReturn(dto);
    EmprestimoResponseDto result = service.convertToDto(emprestimo);
    assertEquals(dto, result);
  }

  @Test
  void testSaveThrowsExceptionWhenUsuarioEmprestimoIsNull() {
    Emprestimo emprestimo = new Emprestimo();
    assertThrows(IllegalArgumentException.class, () -> service.save(emprestimo));
  }

  @Test
  void testSaveThrowsExceptionWhenUsuarioEmprestimoIdIsNull() {
    Emprestimo emprestimo = new Emprestimo();
    Usuario usuario = new Usuario();
    emprestimo.setUsuarioEmprestimo(usuario);
    assertThrows(IllegalArgumentException.class, () -> service.save(emprestimo));
  }

  @Test
  void testSaveThrowsEntityNotFoundExceptionWhenUsuarioEmprestimoNotFound() {
    Emprestimo emprestimo = new Emprestimo();
    Usuario usuarioEmprestimo = new Usuario();
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
    Emprestimo emprestimo = new Emprestimo();
    doNothing().when(emprestimoRepository).delete(emprestimo);
    service.delete(emprestimo);
    verify(emprestimoRepository).delete(emprestimo);
  }

  @Test
  void testProcessEmprestimoCallsFinalizeEmprestimoAndConvertToDto() {
    Emprestimo emprestimo = new Emprestimo();
    EmprestimoResponseDto dto = new EmprestimoResponseDto();
    Usuario usuarioEmprestimo = new Usuario();
    usuarioEmprestimo.setId(1L);
    usuarioEmprestimo.setEmail("mail@test.com");
    emprestimo.setUsuarioEmprestimo(usuarioEmprestimo);
    Usuario usuarioResponsavel = new Usuario();
    usuarioResponsavel.setId(2L);
    usuarioResponsavel.setEmail("responsavel@test.com");
    emprestimo.setUsuarioResponsavel(usuarioResponsavel);
    when(usuarioRepository.findById(anyLong()))
        .thenAnswer(
            invocation -> {
              Long id = invocation.getArgument(0);
              if (id == 1L) return Optional.of(usuarioEmprestimo);
              if (id == 2L) return Optional.of(usuarioResponsavel);
              return Optional.empty();
            });
    when(usuarioService.findByUsername(eq("testuser"))).thenReturn(usuarioResponsavel);
    doNothing().when(eventPublisher).publishEvent(any());
    when(emprestimoRepository.save(any(Emprestimo.class))).thenReturn(emprestimo);
    when(modelMapper.map(any(Emprestimo.class), eq(EmprestimoResponseDto.class))).thenReturn(dto);
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
    Page<Emprestimo> result = service.findAllSpecification(spec, pageable);
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
    Emprestimo emprestimo = new Emprestimo();
    // Inicializa o usuário do empréstimo para evitar NullPointerException
    Usuario usuarioEmprestimo = new Usuario();
    usuarioEmprestimo.setEmail("mail@test.com");
    emprestimo.setUsuarioEmprestimo(usuarioEmprestimo);
    service.finalizeEmprestimo(emprestimo);
    // No exception means success
  }

  @Test
  void testCleanupAfterDeleteHandlesNulls() {
    Emprestimo emprestimo = new Emprestimo();
    service.cleanupAfterDelete(emprestimo);
    // No exception means success
  }
}
