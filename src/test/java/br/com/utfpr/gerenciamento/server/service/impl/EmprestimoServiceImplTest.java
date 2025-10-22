package br.com.utfpr.gerenciamento.server.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.dto.EmprestimoResponseDto;
import br.com.utfpr.gerenciamento.server.enumeration.StatusDevolucao;
import br.com.utfpr.gerenciamento.server.enumeration.TipoItem;
import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.model.EmprestimoDevolucaoItem;
import br.com.utfpr.gerenciamento.server.model.EmprestimoItem;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardEmprestimoDia;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensEmprestados;
import br.com.utfpr.gerenciamento.server.model.filter.EmprestimoFilter;
import br.com.utfpr.gerenciamento.server.repository.EmprestimoRepository;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

class EmprestimoServiceImplTest {
  @Mock private EmprestimoRepository emprestimoRepository;
  @Mock private UsuarioService usuarioService;
  @Mock private ApplicationEventPublisher eventPublisher;
  @Mock private ModelMapper modelMapper;
  @InjectMocks private EmprestimoServiceImpl service;

  private Emprestimo emp;
  private Usuario usuarioResponsavel;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    Usuario usuarioEmprestimo = new Usuario();
    usuarioEmprestimo.setEmail("mail@test.com");
    usuarioResponsavel = new Usuario();
    usuarioResponsavel.setNome("Responsavel Teste");
    emp = new Emprestimo();
    emp.setUsuarioEmprestimo(usuarioEmprestimo);
    emp.setUsuarioResponsavel(usuarioResponsavel);
    emp.setPrazoDevolucao(LocalDate.now().plusDays(5));
    emp.setDataEmprestimo(LocalDate.now());
  }

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
    // Tipagem correta para evitar warning
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
}
