package br.com.utfpr.gerenciamento.server.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.dto.CompraResponseDTO;
import br.com.utfpr.gerenciamento.server.model.Compra;
import br.com.utfpr.gerenciamento.server.model.CompraItem;
import br.com.utfpr.gerenciamento.server.model.Fornecedor;
import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensAdquiridos;
import br.com.utfpr.gerenciamento.server.repository.CompraRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
class CompraServiceImplTest {

  @Mock private CompraRepository compraRepository;
  @Mock private ModelMapper modelMapper;

  @InjectMocks private CompraServiceImpl compraService;

  private Compra compra;
  private CompraResponseDTO compraResponseDTO;

  @BeforeEach
  void setUp() {
    compra = criarCompra(1L);
    compraResponseDTO = criarCompraResponseDTO(1L);
  }

  @Test
  void testGetRepository_DeveRetornarCompraRepository() {
    // When
    var result = compraService.getRepository();

    // Then
    assertThat(result).isEqualTo(compraRepository);
  }

  @Test
  void testToDto_DeveConverterCompraParaDTO() {
    // Given
    when(modelMapper.map(compra, CompraResponseDTO.class)).thenReturn(compraResponseDTO);

    // When
    CompraResponseDTO result = compraService.toDto(compra);

    // Then
    assertNotNull(result);
    assertThat(result).isEqualTo(compraResponseDTO);
    verify(modelMapper).map(compra, CompraResponseDTO.class);
  }

  @Test
  void testToDto_ComCompraComItens_DeveConverterCorretamente() {
    // Given
    Item item1 = criarItem(10L, "Mouse");
    Item item2 = criarItem(20L, "Teclado");
    CompraItem compraItem1 = criarCompraItem(1L, item1, new BigDecimal("5"));
    CompraItem compraItem2 = criarCompraItem(2L, item2, new BigDecimal("3"));

    compra.setCompraItem(Arrays.asList(compraItem1, compraItem2));

    CompraResponseDTO dtoEsperado = criarCompraResponseDTO(1L);
    when(modelMapper.map(compra, CompraResponseDTO.class)).thenReturn(dtoEsperado);

    // When
    CompraResponseDTO result = compraService.toDto(compra);

    // Then
    assertNotNull(result);
    verify(modelMapper).map(compra, CompraResponseDTO.class);
  }

  @Test
  void testToEntity_DeveConverterDTOParaCompra() {
    // Given
    when(modelMapper.map(compraResponseDTO, Compra.class)).thenReturn(compra);

    // When
    Compra result = compraService.toEntity(compraResponseDTO);

    // Then
    assertNotNull(result);
    assertThat(result).isEqualTo(compra);
    verify(modelMapper).map(compraResponseDTO, Compra.class);
  }

  @Test
  void testToEntity_ComDTONull_DeveRetornarNull() {
    // Given
    when(modelMapper.map(null, Compra.class)).thenReturn(null);

    // When
    Compra result = compraService.toEntity(null);

    // Then
    assertNull(result);
  }

  @Test
  void testFindItensMaisAdquiridos_DeveRetornarListaDoDashboard() {
    // Given
    LocalDate dtIni = LocalDate.of(2025, 1, 1);
    LocalDate dtFim = LocalDate.of(2025, 12, 31);

    DashboardItensAdquiridos item1 =
        new DashboardItensAdquiridos(new BigDecimal("50"), "Notebook Dell");
    DashboardItensAdquiridos item2 =
        new DashboardItensAdquiridos(new BigDecimal("30"), "Mouse Logitech");
    DashboardItensAdquiridos item3 =
        new DashboardItensAdquiridos(new BigDecimal("25"), "Teclado Mecânico");

    List<DashboardItensAdquiridos> itensAdquiridos = Arrays.asList(item1, item2, item3);
    when(compraRepository.findItensMaisAdquiridos(dtIni, dtFim)).thenReturn(itensAdquiridos);

    // When
    List<DashboardItensAdquiridos> result = compraService.findItensMaisAdquiridos(dtIni, dtFim);

    // Then
    assertNotNull(result);
    assertThat(result).hasSize(3);
    assertThat(result.get(0).item()).isEqualTo("Notebook Dell");
    assertThat(result.get(0).qtde()).isEqualByComparingTo(new BigDecimal("50"));
    assertThat(result.get(1).item()).isEqualTo("Mouse Logitech");
    assertThat(result.get(1).qtde()).isEqualByComparingTo(new BigDecimal("30"));
    assertThat(result.get(2).item()).isEqualTo("Teclado Mecânico");
    assertThat(result.get(2).qtde()).isEqualByComparingTo(new BigDecimal("25"));
    verify(compraRepository).findItensMaisAdquiridos(dtIni, dtFim);
  }

  @Test
  void testFindItensMaisAdquiridos_SemResultados_DeveRetornarListaVazia() {
    // Given
    LocalDate dtIni = LocalDate.of(2025, 6, 1);
    LocalDate dtFim = LocalDate.of(2025, 6, 30);

    when(compraRepository.findItensMaisAdquiridos(dtIni, dtFim))
        .thenReturn(Collections.emptyList());

    // When
    List<DashboardItensAdquiridos> result = compraService.findItensMaisAdquiridos(dtIni, dtFim);

    // Then
    assertNotNull(result);
    assertThat(result).isEmpty();
    verify(compraRepository).findItensMaisAdquiridos(dtIni, dtFim);
  }

  @Test
  void testFindItensMaisAdquiridos_ComUmItem_DeveRetornarLista() {
    // Given
    LocalDate dtIni = LocalDate.of(2025, 3, 1);
    LocalDate dtFim = LocalDate.of(2025, 3, 31);

    DashboardItensAdquiridos item =
        new DashboardItensAdquiridos(new BigDecimal("10"), "Monitor LG");

    when(compraRepository.findItensMaisAdquiridos(dtIni, dtFim))
        .thenReturn(Collections.singletonList(item));

    // When
    List<DashboardItensAdquiridos> result = compraService.findItensMaisAdquiridos(dtIni, dtFim);

    // Then
    assertNotNull(result);
    assertThat(result).hasSize(1);
    assertThat(result.get(0).item()).isEqualTo("Monitor LG");
    assertThat(result.get(0).qtde()).isEqualByComparingTo(new BigDecimal("10"));
    verify(compraRepository).findItensMaisAdquiridos(dtIni, dtFim);
  }

  @Test
  void testFindItensMaisAdquiridos_ComMesmaData_DeveConsiderarDiaInteiro() {
    // Given
    LocalDate data = LocalDate.of(2025, 5, 15);

    DashboardItensAdquiridos item =
        new DashboardItensAdquiridos(new BigDecimal("5"), "Webcam HD");

    when(compraRepository.findItensMaisAdquiridos(data, data))
        .thenReturn(Collections.singletonList(item));

    // When
    List<DashboardItensAdquiridos> result = compraService.findItensMaisAdquiridos(data, data);

    // Then
    assertNotNull(result);
    assertThat(result).hasSize(1);
    verify(compraRepository).findItensMaisAdquiridos(data, data);
  }

  @Test
  void testFindItensMaisAdquiridos_PeriodoAnual_DeveRetornarTodosItens() {
    // Given
    LocalDate dtIni = LocalDate.of(2024, 1, 1);
    LocalDate dtFim = LocalDate.of(2024, 12, 31);

    DashboardItensAdquiridos item1 =
        new DashboardItensAdquiridos(new BigDecimal("100"), "Cabo HDMI");
    DashboardItensAdquiridos item2 =
        new DashboardItensAdquiridos(new BigDecimal("75"), "Adaptador USB");
    DashboardItensAdquiridos item3 =
        new DashboardItensAdquiridos(new BigDecimal("50"), "Hub USB");
    DashboardItensAdquiridos item4 =
        new DashboardItensAdquiridos(new BigDecimal("25"), "Mouse Pad");

    List<DashboardItensAdquiridos> itensAdquiridos = Arrays.asList(item1, item2, item3, item4);
    when(compraRepository.findItensMaisAdquiridos(dtIni, dtFim)).thenReturn(itensAdquiridos);

    // When
    List<DashboardItensAdquiridos> result = compraService.findItensMaisAdquiridos(dtIni, dtFim);

    // Then
    assertNotNull(result);
    assertThat(result).hasSize(4);
    verify(compraRepository).findItensMaisAdquiridos(dtIni, dtFim);
  }

  @Test
  void testFindItensMaisAdquiridos_QuantidadeDecimal_DeveManterPrecisao() {
    // Given
    LocalDate dtIni = LocalDate.of(2025, 2, 1);
    LocalDate dtFim = LocalDate.of(2025, 2, 28);

    DashboardItensAdquiridos item =
        new DashboardItensAdquiridos(new BigDecimal("15.75"), "Item Fracionado");

    when(compraRepository.findItensMaisAdquiridos(dtIni, dtFim))
        .thenReturn(Collections.singletonList(item));

    // When
    List<DashboardItensAdquiridos> result = compraService.findItensMaisAdquiridos(dtIni, dtFim);

    // Then
    assertNotNull(result);
    assertThat(result).hasSize(1);
    assertThat(result.get(0).qtde()).isEqualByComparingTo(new BigDecimal("15.75"));
    verify(compraRepository).findItensMaisAdquiridos(dtIni, dtFim);
  }

  @Test
  void testToDto_ComCompraNula_DeveRetornarNull() {
    // Given
    when(modelMapper.map(null, CompraResponseDTO.class)).thenReturn(null);

    // When
    CompraResponseDTO result = compraService.toDto(null);

    // Then
    assertNull(result);
  }

  @Test
  void testFindItensMaisAdquiridos_DeveChamarRepositorioComParametrosCorretos() {
    // Given
    LocalDate dtIni = LocalDate.of(2025, 4, 1);
    LocalDate dtFim = LocalDate.of(2025, 4, 30);

    when(compraRepository.findItensMaisAdquiridos(dtIni, dtFim))
        .thenReturn(Collections.emptyList());

    // When
    compraService.findItensMaisAdquiridos(dtIni, dtFim);

    // Then
    verify(compraRepository, times(1)).findItensMaisAdquiridos(eq(dtIni), eq(dtFim));
  }

  // Métodos auxiliares para criar objetos de teste

  private Compra criarCompra(Long id) {
    Compra compra = new Compra();
    compra.setId(id);
    compra.setDataCompra(LocalDate.now());
    compra.setFornecedor(criarFornecedor(1L));
    compra.setCompraItem(Collections.emptyList());
    return compra;
  }

  private CompraResponseDTO criarCompraResponseDTO(Long id) {
    CompraResponseDTO dto = new CompraResponseDTO();
    dto.setId(id);
    dto.setDataCompra(LocalDate.now());
    return dto;
  }

  private CompraItem criarCompraItem(Long id, Item item, BigDecimal quantidade) {
    CompraItem compraItem = new CompraItem();
    compraItem.setId(id);
    compraItem.setItem(item);
    compraItem.setQtde(quantidade);
    compraItem.setValor(new BigDecimal("100.00"));
    return compraItem;
  }

  private Item criarItem(Long id, String nome) {
    Item item = new Item();
    item.setId(id);
    item.setNome(nome);
    item.setSaldo(new BigDecimal("100"));
    return item;
  }

  private Fornecedor criarFornecedor(Long id) {
    Fornecedor fornecedor = new Fornecedor();
    fornecedor.setId(id);
    fornecedor.setRazaoSocial("Fornecedor Teste Ltda");
    fornecedor.setNomeFantasia("Fornecedor Teste");
    fornecedor.setCnpj("12345678901234");
    fornecedor.setIe("123456789012");
    return fornecedor;
  }
}
