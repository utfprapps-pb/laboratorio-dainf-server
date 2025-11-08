package br.com.utfpr.gerenciamento.server.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.dto.SaidaResponseDTO;
import br.com.utfpr.gerenciamento.server.model.*;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensSaidas;
import br.com.utfpr.gerenciamento.server.repository.SaidaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
class SaidaServiceImplTest {

  @Mock private SaidaRepository saidaRepository;
  @Mock private ModelMapper modelMapper;

  @InjectMocks private SaidaServiceImpl saidaService;

  @Captor private ArgumentCaptor<Saida> saidaCaptor;

  private Saida saida;
  private SaidaResponseDTO saidaResponseDTO;

  @BeforeEach
  void setUp() {
    saida = criarSaida(1L);
    saidaResponseDTO = criarSaidaResponseDTO(1L);
  }

  @Test
  void testGetRepository_DeveRetornarSaidaRepository() {
    // When
    var result = saidaService.getRepository();

    // Then
    assertThat(result).isEqualTo(saidaRepository);
  }

  @Test
  void testToDto_DeveConverterSaidaParaDTO() {
    // Given
    when(modelMapper.map(saida, SaidaResponseDTO.class)).thenReturn(saidaResponseDTO);

    // When
    SaidaResponseDTO result = saidaService.toDto(saida);

    // Then
    assertNotNull(result);
    assertThat(result).isEqualTo(saidaResponseDTO);
    verify(modelMapper).map(saida, SaidaResponseDTO.class);
  }

  @Test
  void testToDto_ComSaidaCompleta_DeveConverterCorretamente() {
    // Given
    saida.setObservacao("Observação teste");

    SaidaResponseDTO dtoEsperado = criarSaidaResponseDTO(1L);
    dtoEsperado.setObservacao("Observação teste");

    when(modelMapper.map(saida, SaidaResponseDTO.class)).thenReturn(dtoEsperado);

    // When
    SaidaResponseDTO result = saidaService.toDto(saida);

    // Then
    assertNotNull(result);
    assertThat(result.getObservacao()).isEqualTo("Observação teste");
    verify(modelMapper).map(saida, SaidaResponseDTO.class);
  }

  @Test
  void testToDto_ComSaidaNula_DeveRetornarNull() {
    // Given
    when(modelMapper.map(null, SaidaResponseDTO.class)).thenReturn(null);

    // When
    SaidaResponseDTO result = saidaService.toDto(null);

    // Then
    assertNull(result);
  }

  @Test
  void testToEntity_DeveConverterDTOParaSaida() {
    // Given
    when(modelMapper.map(saidaResponseDTO, Saida.class)).thenReturn(saida);

    // When
    Saida result = saidaService.toEntity(saidaResponseDTO);

    // Then
    assertNotNull(result);
    assertThat(result).isEqualTo(saida);
    verify(modelMapper).map(saidaResponseDTO, Saida.class);
  }

  @Test
  void testToEntity_ComDTONull_DeveRetornarNull() {
    // Given
    when(modelMapper.map(null, Saida.class)).thenReturn(null);

    // When
    Saida result = saidaService.toEntity(null);

    // Then
    assertNull(result);
  }

  @Test
  void testFindItensMaisSaidas_DeveRetornarListaDoDashboard() {
    // Given
    LocalDate dtIni = LocalDate.of(2025, 1, 1);
    LocalDate dtFim = LocalDate.of(2025, 12, 31);

    DashboardItensSaidas item1 = new DashboardItensSaidas(new BigDecimal("50"), "Notebook Dell");
    DashboardItensSaidas item2 = new DashboardItensSaidas(new BigDecimal("30"), "Mouse Logitech");
    DashboardItensSaidas item3 =
        new DashboardItensSaidas(new BigDecimal("25"), "Teclado Mecânico");

    List<DashboardItensSaidas> itensSaidas = Arrays.asList(item1, item2, item3);
    when(saidaRepository.findItensMaisSaidas(dtIni, dtFim)).thenReturn(itensSaidas);

    // When
    List<DashboardItensSaidas> result = saidaService.findItensMaisSaidas(dtIni, dtFim);

    // Then
    assertNotNull(result);
    assertThat(result).hasSize(3);
    assertThat(result.get(0).item()).isEqualTo("Notebook Dell");
    assertThat(result.get(0).qtde()).isEqualByComparingTo(new BigDecimal("50"));
    assertThat(result.get(1).item()).isEqualTo("Mouse Logitech");
    assertThat(result.get(1).qtde()).isEqualByComparingTo(new BigDecimal("30"));
    assertThat(result.get(2).item()).isEqualTo("Teclado Mecânico");
    assertThat(result.get(2).qtde()).isEqualByComparingTo(new BigDecimal("25"));
    verify(saidaRepository).findItensMaisSaidas(dtIni, dtFim);
  }

  @Test
  void testFindItensMaisSaidas_SemResultados_DeveRetornarListaVazia() {
    // Given
    LocalDate dtIni = LocalDate.of(2025, 6, 1);
    LocalDate dtFim = LocalDate.of(2025, 6, 30);

    when(saidaRepository.findItensMaisSaidas(dtIni, dtFim)).thenReturn(Collections.emptyList());

    // When
    List<DashboardItensSaidas> result = saidaService.findItensMaisSaidas(dtIni, dtFim);

    // Then
    assertNotNull(result);
    assertThat(result).isEmpty();
    verify(saidaRepository).findItensMaisSaidas(dtIni, dtFim);
  }

  @Test
  void testCreateSaidaByDevolucaoEmprestimo_DeveCriarSaidaCorretamente() {
    // Given
    Long emprestimoId = 10L;
    Usuario usuario = criarUsuario(1L, "joao.silva");
    Emprestimo emprestimo = criarEmprestimo(emprestimoId, usuario);

    Item item1 = criarItem(1L, "Item 1");
    Item item2 = criarItem(2L, "Item 2");

    EmprestimoDevolucaoItem devItem1 = criarEmprestimoDevolucaoItem(1L, emprestimo, item1, new BigDecimal("5"));
    EmprestimoDevolucaoItem devItem2 = criarEmprestimoDevolucaoItem(2L, emprestimo, item2, new BigDecimal("3"));

    List<EmprestimoDevolucaoItem> devolucaoItens = Arrays.asList(devItem1, devItem2);

    when(saidaRepository.save(any(Saida.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    saidaService.createSaidaByDevolucaoEmprestimo(devolucaoItens);

    // Then
    verify(saidaRepository).save(saidaCaptor.capture());
    Saida saidaCriada = saidaCaptor.getValue();

    assertNotNull(saidaCriada);
    assertThat(saidaCriada.getIdEmprestimo()).isEqualTo(emprestimoId);
    assertThat(saidaCriada.getDataSaida()).isEqualTo(LocalDate.now());
    assertThat(saidaCriada.getObservacao()).contains("Saída originada do empréstimo: " + emprestimoId);
    assertThat(saidaCriada.getUsuarioResponsavel()).isEqualTo(usuario);
    assertThat(saidaCriada.getSaidaItem()).hasSize(2);
    assertThat(saidaCriada.getSaidaItem().get(0).getItem()).isEqualTo(item1);
    assertThat(saidaCriada.getSaidaItem().get(0).getQtde()).isEqualByComparingTo(new BigDecimal("5"));
    assertThat(saidaCriada.getSaidaItem().get(1).getItem()).isEqualTo(item2);
    assertThat(saidaCriada.getSaidaItem().get(1).getQtde()).isEqualByComparingTo(new BigDecimal("3"));
  }

  @Test
  void testCreateSaidaByDevolucaoEmprestimo_ComUmItem_DeveCriarCorretamente() {
    // Given
    Long emprestimoId = 5L;
    Usuario usuario = criarUsuario(2L, "maria.santos");
    Emprestimo emprestimo = criarEmprestimo(emprestimoId, usuario);

    Item item = criarItem(10L, "Monitor LG");

    EmprestimoDevolucaoItem devItem =
        criarEmprestimoDevolucaoItem(1L, emprestimo, item, new BigDecimal("2"));

    List<EmprestimoDevolucaoItem> devolucaoItens = Collections.singletonList(devItem);

    when(saidaRepository.save(any(Saida.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    saidaService.createSaidaByDevolucaoEmprestimo(devolucaoItens);

    // Then
    verify(saidaRepository).save(saidaCaptor.capture());
    Saida saidaCriada = saidaCaptor.getValue();

    assertNotNull(saidaCriada);
    assertThat(saidaCriada.getIdEmprestimo()).isEqualTo(emprestimoId);
    assertThat(saidaCriada.getSaidaItem()).hasSize(1);
    assertThat(saidaCriada.getSaidaItem().get(0).getItem().getId()).isEqualTo(10L);
    assertThat(saidaCriada.getSaidaItem().get(0).getQtde()).isEqualByComparingTo(new BigDecimal("2"));
  }

  @Test
  void testCreateSaidaByDevolucaoEmprestimo_DeveVincularSaidaAosItens() {
    // Given
    Long emprestimoId = 7L;
    Usuario usuario = criarUsuario(3L, "pedro.costa");
    Emprestimo emprestimo = criarEmprestimo(emprestimoId, usuario);

    Item item = criarItem(15L, "Teclado");

    EmprestimoDevolucaoItem devItem =
        criarEmprestimoDevolucaoItem(1L, emprestimo, item, new BigDecimal("1"));

    List<EmprestimoDevolucaoItem> devolucaoItens = Collections.singletonList(devItem);

    when(saidaRepository.save(any(Saida.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    saidaService.createSaidaByDevolucaoEmprestimo(devolucaoItens);

    // Then
    verify(saidaRepository).save(saidaCaptor.capture());
    Saida saidaCriada = saidaCaptor.getValue();

    // Verifica que cada SaidaItem tem referência à Saida
    saidaCriada.getSaidaItem().forEach(saidaItem -> {
      assertThat(saidaItem.getSaida()).isEqualTo(saidaCriada);
    });
  }

  @Test
  void testDeleteSaidaByEmprestimo_ComSaidaExistente_DeveDeletar() {
    // Given
    Long idEmprestimo = 10L;
    Saida saidaExistente = criarSaida(1L);
    saidaExistente.setIdEmprestimo(idEmprestimo);

    when(saidaRepository.findByIdEmprestimo(idEmprestimo)).thenReturn(saidaExistente);

    // When
    saidaService.deleteSaidaByEmprestimo(idEmprestimo);

    // Then
    verify(saidaRepository).findByIdEmprestimo(idEmprestimo);
    verify(saidaRepository).delete(saidaExistente);
  }

  @Test
  void testDeleteSaidaByEmprestimo_SemSaidaExistente_NaoDeveDeletar() {
    // Given
    Long idEmprestimo = 15L;

    when(saidaRepository.findByIdEmprestimo(idEmprestimo)).thenReturn(null);

    // When
    saidaService.deleteSaidaByEmprestimo(idEmprestimo);

    // Then
    verify(saidaRepository).findByIdEmprestimo(idEmprestimo);
    verify(saidaRepository, never()).delete(any(Saida.class));
  }

  @Test
  void testDeleteSaidaByEmprestimo_DeveChamarRepositorioCorretamente() {
    // Given
    Long idEmprestimo = 20L;
    Saida saidaExistente = criarSaida(5L);

    when(saidaRepository.findByIdEmprestimo(idEmprestimo)).thenReturn(saidaExistente);

    // When
    saidaService.deleteSaidaByEmprestimo(idEmprestimo);

    // Then
    var inOrder = inOrder(saidaRepository);
    inOrder.verify(saidaRepository).findByIdEmprestimo(idEmprestimo);
    inOrder.verify(saidaRepository).delete(saidaExistente);
  }

  @Test
  void testFindItensMaisSaidas_ComQuantidadeDecimal_DeveManterPrecisao() {
    // Given
    LocalDate dtIni = LocalDate.of(2025, 2, 1);
    LocalDate dtFim = LocalDate.of(2025, 2, 28);

    DashboardItensSaidas item =
        new DashboardItensSaidas(new BigDecimal("15.75"), "Item Fracionado");

    when(saidaRepository.findItensMaisSaidas(dtIni, dtFim))
        .thenReturn(Collections.singletonList(item));

    // When
    List<DashboardItensSaidas> result = saidaService.findItensMaisSaidas(dtIni, dtFim);

    // Then
    assertNotNull(result);
    assertThat(result).hasSize(1);
    assertThat(result.get(0).qtde()).isEqualByComparingTo(new BigDecimal("15.75"));
    verify(saidaRepository).findItensMaisSaidas(dtIni, dtFim);
  }

  @Test
  void testCreateSaidaByDevolucaoEmprestimo_ComVariosItens_DeveCriarTodosSaidaItens() {
    // Given
    Long emprestimoId = 25L;
    Usuario usuario = criarUsuario(4L, "ana.oliveira");
    Emprestimo emprestimo = criarEmprestimo(emprestimoId, usuario);

    Item item1 = criarItem(1L, "Item A");
    Item item2 = criarItem(2L, "Item B");
    Item item3 = criarItem(3L, "Item C");

    EmprestimoDevolucaoItem dev1 =
        criarEmprestimoDevolucaoItem(1L, emprestimo, item1, new BigDecimal("10"));
    EmprestimoDevolucaoItem dev2 =
        criarEmprestimoDevolucaoItem(2L, emprestimo, item2, new BigDecimal("20"));
    EmprestimoDevolucaoItem dev3 =
        criarEmprestimoDevolucaoItem(3L, emprestimo, item3, new BigDecimal("30"));

    List<EmprestimoDevolucaoItem> devolucaoItens = Arrays.asList(dev1, dev2, dev3);

    when(saidaRepository.save(any(Saida.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    saidaService.createSaidaByDevolucaoEmprestimo(devolucaoItens);

    // Then
    verify(saidaRepository).save(saidaCaptor.capture());
    Saida saidaCriada = saidaCaptor.getValue();

    assertThat(saidaCriada.getSaidaItem()).hasSize(3);
    assertThat(saidaCriada.getSaidaItem().get(0).getQtde()).isEqualByComparingTo(new BigDecimal("10"));
    assertThat(saidaCriada.getSaidaItem().get(1).getQtde()).isEqualByComparingTo(new BigDecimal("20"));
    assertThat(saidaCriada.getSaidaItem().get(2).getQtde()).isEqualByComparingTo(new BigDecimal("30"));
  }

  // Métodos auxiliares para criar objetos de teste

  private Saida criarSaida(Long id) {
    Saida saida = new Saida();
    saida.setId(id);
    saida.setDataSaida(LocalDate.now());
    saida.setSaidaItem(Collections.emptyList());
    return saida;
  }

  private SaidaResponseDTO criarSaidaResponseDTO(Long id) {
    SaidaResponseDTO dto = new SaidaResponseDTO();
    dto.setId(id);
    dto.setDataSaida(LocalDate.now());
    return dto;
  }

  private Item criarItem(Long id, String nome) {
    Item item = new Item();
    item.setId(id);
    item.setNome(nome);
    item.setSaldo(new BigDecimal("100"));
    return item;
  }

  private Usuario criarUsuario(Long id, String username) {
    Usuario usuario = new Usuario();
    usuario.setId(id);
    usuario.setUsername(username);
    usuario.setNome("Nome " + username);
    return usuario;
  }

  private Emprestimo criarEmprestimo(Long id, Usuario usuario) {
    Emprestimo emprestimo = new Emprestimo();
    emprestimo.setId(id);
    emprestimo.setUsuarioResponsavel(usuario);
    emprestimo.setDataEmprestimo(LocalDate.now());
    return emprestimo;
  }

  private EmprestimoDevolucaoItem criarEmprestimoDevolucaoItem(
      Long id, Emprestimo emprestimo, Item item, BigDecimal qtde) {
    EmprestimoDevolucaoItem devItem = new EmprestimoDevolucaoItem();
    devItem.setId(id);
    devItem.setEmprestimo(emprestimo);
    devItem.setItem(item);
    devItem.setQtde(qtde);
    return devItem;
  }
}
