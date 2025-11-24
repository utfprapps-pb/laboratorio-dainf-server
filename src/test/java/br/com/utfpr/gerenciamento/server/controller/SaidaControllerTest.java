package br.com.utfpr.gerenciamento.server.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.dto.SaidaResponseDTO;
import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.model.Saida;
import br.com.utfpr.gerenciamento.server.model.SaidaItem;
import br.com.utfpr.gerenciamento.server.service.ItemService;
import br.com.utfpr.gerenciamento.server.service.SaidaService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

class SaidaControllerTest {

  private SaidaService saidaService;
  private ItemService itemService;
  private SaidaController saidaController;

  @BeforeEach
  void setup() {
    saidaService = Mockito.mock(SaidaService.class);
    itemService = Mockito.mock(ItemService.class);
    saidaController = new SaidaController(saidaService, itemService);
  }

  @Test
  void testGetService_DeveRetornarSaidaService() {
    // When
    var result = saidaController.getService();

    // Then
    assertThat(result).isEqualTo(saidaService);
  }

  @Test
  void testPreSave_ComIdNull_DeveApenasValidarSaldos() {
    // Given
    Item item1 = criarItem(10L);
    Item item2 = criarItem(20L);

    SaidaItem saidaItem1 = criarSaidaItem(1L, item1, new BigDecimal("5"));
    SaidaItem saidaItem2 = criarSaidaItem(2L, item2, new BigDecimal("3"));

    Saida saida = criarSaida(null);
    saida.setSaidaItem(Arrays.asList(saidaItem1, saidaItem2));

    when(itemService.getSaldoItem(10L)).thenReturn(new BigDecimal("100"));
    when(itemService.getSaldoItem(20L)).thenReturn(new BigDecimal("50"));

    // When
    saidaController.preSave(saida);

    // Then
    verify(saidaService, never()).findOne(any());
    verify(itemService).getSaldoItem(10L);
    verify(itemService).getSaldoItem(20L);
    verify(itemService).saldoItemIsValid(new BigDecimal("100"), new BigDecimal("5"));
    verify(itemService).saldoItemIsValid(new BigDecimal("50"), new BigDecimal("3"));
    verify(itemService, never()).aumentaSaldoItem(any(), any());
  }

  @Test
  void testPreSave_ComId_DeveRetornarSaldoAntigoEValidarNovo() {
    // Given
    Long saidaId = 1L;

    Item item1 = criarItem(10L);
    Item item2 = criarItem(20L);

    // Saída antiga
    SaidaItem saidaItemAntigo1 = criarSaidaItem(1L, item1, new BigDecimal("3"));
    SaidaItem saidaItemAntigo2 = criarSaidaItem(2L, item2, new BigDecimal("2"));
    Saida saidaAntiga = criarSaida(saidaId);
    saidaAntiga.setSaidaItem(Arrays.asList(saidaItemAntigo1, saidaItemAntigo2));

    // Nova saída
    SaidaItem saidaItemNovo1 = criarSaidaItem(1L, item1, new BigDecimal("5"));
    SaidaItem saidaItemNovo2 = criarSaidaItem(2L, item2, new BigDecimal("4"));
    Saida saidaNova = criarSaida(saidaId);
    saidaNova.setSaidaItem(Arrays.asList(saidaItemNovo1, saidaItemNovo2));

    SaidaResponseDTO saidaResponseDTO = new SaidaResponseDTO();
    when(saidaService.findOne(saidaId)).thenReturn(saidaResponseDTO);
    when(saidaService.toEntity(saidaResponseDTO)).thenReturn(saidaAntiga);
    when(itemService.getSaldoItem(10L)).thenReturn(new BigDecimal("100"));
    when(itemService.getSaldoItem(20L)).thenReturn(new BigDecimal("50"));

    // When
    saidaController.preSave(saidaNova);

    // Then
    verify(saidaService).findOne(saidaId);
    verify(saidaService).toEntity(saidaResponseDTO);
    verify(itemService).aumentaSaldoItem(10L, new BigDecimal("3"));
    verify(itemService).aumentaSaldoItem(20L, new BigDecimal("2"));
    verify(itemService).saldoItemIsValid(new BigDecimal("100"), new BigDecimal("5"));
    verify(itemService).saldoItemIsValid(new BigDecimal("50"), new BigDecimal("4"));
  }

  @Test
  void testPreSave_ComItemNull_NaoDeveValidarSaldo() {
    // Given
    SaidaItem saidaItem1 = criarSaidaItem(1L, null, new BigDecimal("5"));

    Saida saida = criarSaida(null);
    saida.setSaidaItem(Collections.singletonList(saidaItem1));

    // When
    saidaController.preSave(saida);

    // Then
    verify(itemService, never()).getSaldoItem(any());
    verify(itemService, never()).saldoItemIsValid(any(), any());
  }

  @Test
  void testPostSave_DeveDiminuirSaldoDosItens() {
    // Given
    Item item1 = criarItem(10L);
    Item item2 = criarItem(20L);

    SaidaItem saidaItem1 = criarSaidaItem(1L, item1, new BigDecimal("8"));
    SaidaItem saidaItem2 = criarSaidaItem(2L, item2, new BigDecimal("12"));

    Saida saida = criarSaida(1L);
    saida.setSaidaItem(Arrays.asList(saidaItem1, saidaItem2));

    // When
    saidaController.postSave(saida);

    // Then
    verify(itemService).diminuiSaldoItem(10L, new BigDecimal("8"), true);
    verify(itemService).diminuiSaldoItem(20L, new BigDecimal("12"), true);
  }

  @Test
  void testPostSave_ComUmItem_DeveDiminuirSaldoComFlagTrue() {
    // Given
    Item item = criarItem(30L);
    SaidaItem saidaItem = criarSaidaItem(1L, item, new BigDecimal("7"));

    Saida saida = criarSaida(1L);
    saida.setSaidaItem(Collections.singletonList(saidaItem));

    // When
    saidaController.postSave(saida);

    // Then
    verify(itemService).diminuiSaldoItem(30L, new BigDecimal("7"), true);
  }

  @Test
  void testPostSave_ComListaVazia_NaoDeveFazerNada() {
    // Given
    Saida saida = criarSaida(1L);
    saida.setSaidaItem(Collections.emptyList());

    // When
    saidaController.postSave(saida);

    // Then
    verify(itemService, never()).diminuiSaldoItem(any(), any(), anyBoolean());
  }

  @Test
  void testPostDelete_DeveAumentarSaldoDosItens() {
    // Given
    Item item1 = criarItem(10L);
    Item item2 = criarItem(20L);

    SaidaItem saidaItem1 = criarSaidaItem(1L, item1, new BigDecimal("5"));
    SaidaItem saidaItem2 = criarSaidaItem(2L, item2, new BigDecimal("3"));

    Saida saida = criarSaida(1L);
    saida.setSaidaItem(Arrays.asList(saidaItem1, saidaItem2));

    // When
    saidaController.postDelete(saida);

    // Then
    verify(itemService).aumentaSaldoItem(10L, new BigDecimal("5"));
    verify(itemService).aumentaSaldoItem(20L, new BigDecimal("3"));
  }

  @Test
  void testPostDelete_ComUmItem_DeveAumentarSaldo() {
    // Given
    Item item = criarItem(40L);
    SaidaItem saidaItem = criarSaidaItem(1L, item, new BigDecimal("15"));

    Saida saida = criarSaida(1L);
    saida.setSaidaItem(Collections.singletonList(saidaItem));

    // When
    saidaController.postDelete(saida);

    // Then
    verify(itemService).aumentaSaldoItem(40L, new BigDecimal("15"));
  }

  @Test
  void testPostDelete_ComListaVazia_NaoDeveFazerNada() {
    // Given
    Saida saida = criarSaida(1L);
    saida.setSaidaItem(Collections.emptyList());

    // When
    saidaController.postDelete(saida);

    // Then
    verify(itemService, never()).aumentaSaldoItem(any(), any());
  }

  @Test
  void testFluxoCompleto_NovaSaida_DeveValidarEDiminuirSaldo() {
    // Given - nova saída (sem ID)
    Item item = criarItem(10L);
    SaidaItem saidaItem = criarSaidaItem(1L, item, new BigDecimal("10"));

    Saida novaSaida = criarSaida(null);
    novaSaida.setSaidaItem(Collections.singletonList(saidaItem));

    when(itemService.getSaldoItem(10L)).thenReturn(new BigDecimal("100"));

    // When
    saidaController.preSave(novaSaida);
    saidaController.postSave(novaSaida);

    // Then
    verify(itemService).getSaldoItem(10L);
    verify(itemService).saldoItemIsValid(new BigDecimal("100"), new BigDecimal("10"));
    verify(itemService).diminuiSaldoItem(10L, new BigDecimal("10"), true);
    verify(itemService, never()).aumentaSaldoItem(any(), any());
  }

  @Test
  void testFluxoCompleto_EdicaoSaida_DeveAumentarAntigoDiminuirNovo() {
    // Given - saída existente sendo editada
    Long saidaId = 1L;
    Item item = criarItem(10L);

    // Saída antiga tinha quantidade 5
    SaidaItem saidaItemAntigo = criarSaidaItem(1L, item, new BigDecimal("5"));
    Saida saidaAntiga = criarSaida(saidaId);
    saidaAntiga.setSaidaItem(Collections.singletonList(saidaItemAntigo));

    // Nova saída tem quantidade 8
    SaidaItem saidaItemNovo = criarSaidaItem(1L, item, new BigDecimal("8"));
    Saida saidaNova = criarSaida(saidaId);
    saidaNova.setSaidaItem(Collections.singletonList(saidaItemNovo));

    SaidaResponseDTO saidaResponseDTO = new SaidaResponseDTO();
    when(saidaService.findOne(saidaId)).thenReturn(saidaResponseDTO);
    when(saidaService.toEntity(saidaResponseDTO)).thenReturn(saidaAntiga);
    when(itemService.getSaldoItem(10L)).thenReturn(new BigDecimal("100"));

    // When
    saidaController.preSave(saidaNova);
    saidaController.postSave(saidaNova);

    // Then
    InOrder inOrder = inOrder(itemService);
    inOrder.verify(itemService).aumentaSaldoItem(10L, new BigDecimal("5"));
    inOrder.verify(itemService).getSaldoItem(10L);
    inOrder.verify(itemService).saldoItemIsValid(new BigDecimal("100"), new BigDecimal("8"));
    inOrder.verify(itemService).diminuiSaldoItem(10L, new BigDecimal("8"), true);
  }

  @Test
  void testFluxoCompleto_ExclusaoSaida_DeveApenasRetornarSaldo() {
    // Given - saída sendo excluída
    Item item1 = criarItem(10L);
    Item item2 = criarItem(20L);

    SaidaItem saidaItem1 = criarSaidaItem(1L, item1, new BigDecimal("5"));
    SaidaItem saidaItem2 = criarSaidaItem(2L, item2, new BigDecimal("3"));

    Saida saida = criarSaida(1L);
    saida.setSaidaItem(Arrays.asList(saidaItem1, saidaItem2));

    // When
    saidaController.postDelete(saida);

    // Then
    verify(itemService).aumentaSaldoItem(10L, new BigDecimal("5"));
    verify(itemService).aumentaSaldoItem(20L, new BigDecimal("3"));
    verify(itemService, never()).diminuiSaldoItem(any(), any(), anyBoolean());
  }

  @Test
  void testPreSave_ComVariosItens_DeveValidarTodos() {
    // Given
    Item item1 = criarItem(10L);
    Item item2 = criarItem(20L);
    Item item3 = criarItem(30L);

    SaidaItem saidaItem1 = criarSaidaItem(1L, item1, new BigDecimal("2"));
    SaidaItem saidaItem2 = criarSaidaItem(2L, item2, new BigDecimal("4"));
    SaidaItem saidaItem3 = criarSaidaItem(3L, item3, new BigDecimal("6"));

    Saida saida = criarSaida(null);
    saida.setSaidaItem(Arrays.asList(saidaItem1, saidaItem2, saidaItem3));

    when(itemService.getSaldoItem(10L)).thenReturn(new BigDecimal("100"));
    when(itemService.getSaldoItem(20L)).thenReturn(new BigDecimal("200"));
    when(itemService.getSaldoItem(30L)).thenReturn(new BigDecimal("300"));

    // When
    saidaController.preSave(saida);

    // Then
    verify(itemService).saldoItemIsValid(new BigDecimal("100"), new BigDecimal("2"));
    verify(itemService).saldoItemIsValid(new BigDecimal("200"), new BigDecimal("4"));
    verify(itemService).saldoItemIsValid(new BigDecimal("300"), new BigDecimal("6"));
  }

  @Test
  void testPreSave_EdicaoComItensNulos_DeveIgnorarItensNulos() {
    // Given
    Long saidaId = 1L;
    Item item1 = criarItem(10L);

    SaidaItem saidaItemAntigo = criarSaidaItem(1L, item1, new BigDecimal("5"));
    Saida saidaAntiga = criarSaida(saidaId);
    saidaAntiga.setSaidaItem(Collections.singletonList(saidaItemAntigo));

    SaidaItem saidaItemNovo1 = criarSaidaItem(1L, item1, new BigDecimal("3"));
    SaidaItem saidaItemNovo2 = criarSaidaItem(2L, null, new BigDecimal("2"));

    Saida saidaNova = criarSaida(saidaId);
    saidaNova.setSaidaItem(Arrays.asList(saidaItemNovo1, saidaItemNovo2));

    SaidaResponseDTO saidaResponseDTO = new SaidaResponseDTO();
    when(saidaService.findOne(saidaId)).thenReturn(saidaResponseDTO);
    when(saidaService.toEntity(saidaResponseDTO)).thenReturn(saidaAntiga);
    when(itemService.getSaldoItem(10L)).thenReturn(new BigDecimal("100"));

    // When
    saidaController.preSave(saidaNova);

    // Then
    verify(itemService).aumentaSaldoItem(10L, new BigDecimal("5"));
    verify(itemService).getSaldoItem(10L);
    verify(itemService).saldoItemIsValid(new BigDecimal("100"), new BigDecimal("3"));
    verify(itemService, times(1)).getSaldoItem(any()); // Apenas 1 vez para item1
  }

  // Métodos auxiliares para criar objetos de teste

  private Saida criarSaida(Long id) {
    Saida saida = new Saida();
    saida.setId(id);
    saida.setDataSaida(LocalDate.now());
    saida.setSaidaItem(Collections.emptyList());
    return saida;
  }

  private SaidaItem criarSaidaItem(Long id, Item item, BigDecimal quantidade) {
    SaidaItem saidaItem = new SaidaItem();
    saidaItem.setId(id);
    saidaItem.setItem(item);
    saidaItem.setQtde(quantidade);
    return saidaItem;
  }

  private Item criarItem(Long id) {
    Item item = new Item();
    item.setId(id);
    item.setSaldo(new BigDecimal("100"));
    return item;
  }
}
