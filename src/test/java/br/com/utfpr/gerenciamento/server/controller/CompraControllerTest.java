package br.com.utfpr.gerenciamento.server.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.dto.CompraResponseDTO;
import br.com.utfpr.gerenciamento.server.model.Compra;
import br.com.utfpr.gerenciamento.server.model.CompraItem;
import br.com.utfpr.gerenciamento.server.model.Fornecedor;
import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.service.CompraService;
import br.com.utfpr.gerenciamento.server.service.ItemService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

class CompraControllerTest {

  private CompraService compraService;
  private ItemService itemService;
  private CompraController compraController;

  @BeforeEach
  void setup() {
    compraService = Mockito.mock(CompraService.class);
    itemService = Mockito.mock(ItemService.class);
    compraController = new CompraController(compraService, itemService);
  }

  @Test
  void testGetService_DeveRetornarCompraService() {
    // When
    var result = compraController.getService();

    // Then
    assertThat(result).isEqualTo(compraService);
  }

  @Test
  void testPreSave_ComIdNull_NaoDeveFazerNada() {
    // Given
    Compra compra = criarCompra(null);

    // When
    compraController.preSave(compra);

    // Then
    verify(compraService, never()).findOne(anyLong());
    verify(itemService, never()).diminuiSaldoItem(anyLong(), any(), anyBoolean());
  }

  @Test
  void testPreSave_ComId_DeveDiminuirSaldoAntigo() {
    // Given
    Long compraId = 1L;
    Compra compra = criarCompra(compraId);
    Compra compraAntiga = criarCompraAntiga(compraId);

    CompraResponseDTO compraResponseDTO = new CompraResponseDTO();
    when(compraService.findOne(compraId)).thenReturn(compraResponseDTO);
    when(compraService.toEntity(compraResponseDTO)).thenReturn(compraAntiga);

    // When
    compraController.preSave(compra);

    // Then
    verify(compraService).findOne(compraId);
    verify(compraService).toEntity(compraResponseDTO);
    verify(itemService).diminuiSaldoItem(10L, new BigDecimal("5"), false);
    verify(itemService).diminuiSaldoItem(20L, new BigDecimal("3"), false);
  }

  @Test
  void testPreSave_ComIdEUmItem_DeveDiminuirSaldoDoItem() {
    // Given
    Long compraId = 1L;
    Item item = criarItem(10L);
    CompraItem compraItem = criarCompraItem(1L, item, new BigDecimal("5"));

    Compra compra = criarCompra(compraId);
    compra.setCompraItem(Collections.singletonList(compraItem));

    Compra compraAntiga = criarCompra(compraId);
    compraAntiga.setCompraItem(Collections.singletonList(compraItem));

    CompraResponseDTO compraResponseDTO = new CompraResponseDTO();
    when(compraService.findOne(compraId)).thenReturn(compraResponseDTO);
    when(compraService.toEntity(compraResponseDTO)).thenReturn(compraAntiga);

    // When
    compraController.preSave(compra);

    // Then
    verify(itemService).diminuiSaldoItem(10L, new BigDecimal("5"), false);
  }

  @Test
  void testPostSave_DeveAumentarSaldoDosItens() {
    // Given
    Item item1 = criarItem(10L);
    Item item2 = criarItem(20L);

    CompraItem compraItem1 = criarCompraItem(1L, item1, new BigDecimal("10"));
    CompraItem compraItem2 = criarCompraItem(2L, item2, new BigDecimal("5"));

    Compra compra = criarCompra(1L);
    compra.setCompraItem(Arrays.asList(compraItem1, compraItem2));

    // When
    compraController.postSave(compra);

    // Then
    verify(itemService).aumentaSaldoItem(10L, new BigDecimal("10"));
    verify(itemService).aumentaSaldoItem(20L, new BigDecimal("5"));
  }

  @Test
  void testPostSave_ComUmItem_DeveAumentarSaldo() {
    // Given
    Item item = criarItem(10L);
    CompraItem compraItem = criarCompraItem(1L, item, new BigDecimal("15"));

    Compra compra = criarCompra(1L);
    compra.setCompraItem(Collections.singletonList(compraItem));

    // When
    compraController.postSave(compra);

    // Then
    verify(itemService).aumentaSaldoItem(10L, new BigDecimal("15"));
  }

  @Test
  void testPostSave_ComListaVazia_NaoDeveFazerNada() {
    // Given
    Compra compra = criarCompra(1L);
    compra.setCompraItem(Collections.emptyList());

    // When
    compraController.postSave(compra);

    // Then
    verify(itemService, never()).aumentaSaldoItem(anyLong(), any());
  }

  @Test
  void testPostDelete_DeveDiminuirSaldoDosItens() {
    // Given
    Item item1 = criarItem(10L);
    Item item2 = criarItem(20L);

    CompraItem compraItem1 = criarCompraItem(1L, item1, new BigDecimal("8"));
    CompraItem compraItem2 = criarCompraItem(2L, item2, new BigDecimal("12"));

    Compra compra = criarCompra(1L);
    compra.setCompraItem(Arrays.asList(compraItem1, compraItem2));

    // When
    compraController.postDelete(compra);

    // Then
    verify(itemService).diminuiSaldoItem(10L, new BigDecimal("8"), true);
    verify(itemService).diminuiSaldoItem(20L, new BigDecimal("12"), true);
  }

  @Test
  void testPostDelete_ComUmItem_DeveDiminuirSaldoComFlagTrue() {
    // Given
    Item item = criarItem(30L);
    CompraItem compraItem = criarCompraItem(1L, item, new BigDecimal("7"));

    Compra compra = criarCompra(1L);
    compra.setCompraItem(Collections.singletonList(compraItem));

    // When
    compraController.postDelete(compra);

    // Then
    verify(itemService).diminuiSaldoItem(30L, new BigDecimal("7"), true);
  }

  @Test
  void testPostDelete_ComListaVazia_NaoDeveFazerNada() {
    // Given
    Compra compra = criarCompra(1L);
    compra.setCompraItem(Collections.emptyList());

    // When
    compraController.postDelete(compra);

    // Then
    verify(itemService, never()).diminuiSaldoItem(anyLong(), any(), anyBoolean());
  }

  @Test
  void testFluxoCompleto_NovaCompra_DeveApenasAumentarSaldo() {
    // Given - nova compra (sem ID)
    Item item = criarItem(10L);
    CompraItem compraItem = criarCompraItem(1L, item, new BigDecimal("10"));

    Compra novaCompra = criarCompra(null);
    novaCompra.setCompraItem(Collections.singletonList(compraItem));

    // When
    compraController.preSave(novaCompra);
    compraController.postSave(novaCompra);

    // Then
    verify(itemService, never()).diminuiSaldoItem(anyLong(), any(), anyBoolean());
    verify(itemService).aumentaSaldoItem(10L, new BigDecimal("10"));
  }

  @Test
  void testFluxoCompleto_EdicaoCompra_DeveDiminuirAntigoEAumentarNovo() {
    // Given - compra existente sendo editada
    Long compraId = 1L;
    Item item = criarItem(10L);

    // Compra antiga tinha quantidade 5
    CompraItem compraItemAntigo = criarCompraItem(1L, item, new BigDecimal("5"));
    Compra compraAntiga = criarCompra(compraId);
    compraAntiga.setCompraItem(Collections.singletonList(compraItemAntigo));

    // Nova compra tem quantidade 8
    CompraItem compraItemNovo = criarCompraItem(1L, item, new BigDecimal("8"));
    Compra compraNova = criarCompra(compraId);
    compraNova.setCompraItem(Collections.singletonList(compraItemNovo));

    CompraResponseDTO compraResponseDTO = new CompraResponseDTO();
    when(compraService.findOne(compraId)).thenReturn(compraResponseDTO);
    when(compraService.toEntity(compraResponseDTO)).thenReturn(compraAntiga);

    // When
    compraController.preSave(compraNova);
    compraController.postSave(compraNova);

    // Then
    InOrder inOrder = inOrder(itemService);
    inOrder.verify(itemService).diminuiSaldoItem(10L, new BigDecimal("5"), false);
    inOrder.verify(itemService).aumentaSaldoItem(10L, new BigDecimal("8"));
  }

  @Test
  void testFluxoCompleto_ExclusaoCompra_DeveApenasRemoverSaldo() {
    // Given - compra sendo excluída
    Item item1 = criarItem(10L);
    Item item2 = criarItem(20L);

    CompraItem compraItem1 = criarCompraItem(1L, item1, new BigDecimal("5"));
    CompraItem compraItem2 = criarCompraItem(2L, item2, new BigDecimal("3"));

    Compra compra = criarCompra(1L);
    compra.setCompraItem(Arrays.asList(compraItem1, compraItem2));

    // When
    compraController.postDelete(compra);

    // Then
    verify(itemService).diminuiSaldoItem(10L, new BigDecimal("5"), true);
    verify(itemService).diminuiSaldoItem(20L, new BigDecimal("3"), true);
    verify(itemService, never()).aumentaSaldoItem(anyLong(), any());
  }

  @Test
  void testPreSave_ComVariosItens_DeveDiminuirSaldoDeTodos() {
    // Given
    Long compraId = 1L;
    Item item1 = criarItem(10L);
    Item item2 = criarItem(20L);
    Item item3 = criarItem(30L);

    CompraItem compraItem1 = criarCompraItem(1L, item1, new BigDecimal("2"));
    CompraItem compraItem2 = criarCompraItem(2L, item2, new BigDecimal("4"));
    CompraItem compraItem3 = criarCompraItem(3L, item3, new BigDecimal("6"));

    Compra compraAntiga = criarCompra(compraId);
    compraAntiga.setCompraItem(Arrays.asList(compraItem1, compraItem2, compraItem3));

    Compra compraNova = criarCompra(compraId);

    CompraResponseDTO compraResponseDTO = new CompraResponseDTO();
    when(compraService.findOne(compraId)).thenReturn(compraResponseDTO);
    when(compraService.toEntity(compraResponseDTO)).thenReturn(compraAntiga);

    // When
    compraController.preSave(compraNova);

    // Then
    verify(itemService).diminuiSaldoItem(10L, new BigDecimal("2"), false);
    verify(itemService).diminuiSaldoItem(20L, new BigDecimal("4"), false);
    verify(itemService).diminuiSaldoItem(30L, new BigDecimal("6"), false);
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

  private Compra criarCompraAntiga(Long compraId) {
    Item item1 = criarItem(10L);
    Item item2 = criarItem(20L);

    CompraItem compraItem1 = criarCompraItem(1L, item1, new BigDecimal("5"));
    CompraItem compraItem2 = criarCompraItem(2L, item2, new BigDecimal("3"));

    Compra compra = criarCompra(compraId);
    compra.setCompraItem(Arrays.asList(compraItem1, compraItem2));
    return compra;
  }

  private CompraItem criarCompraItem(Long id, Item item, BigDecimal quantidade) {
    CompraItem compraItem = new CompraItem();
    compraItem.setId(id);
    compraItem.setItem(item);
    compraItem.setQtde(quantidade);
    compraItem.setValor(new BigDecimal("100.00"));
    return compraItem;
  }

  private Item criarItem(Long id) {
    Item item = new Item();
    item.setId(id);
    item.setSaldo(new BigDecimal("100"));
    return item;
  }

  private Fornecedor criarFornecedor(Long id) {
    Fornecedor fornecedor = new Fornecedor();
    fornecedor.setId(id);
    fornecedor.setNomeFantasia("Fornecedor Teste");
    return fornecedor;
  }
}
