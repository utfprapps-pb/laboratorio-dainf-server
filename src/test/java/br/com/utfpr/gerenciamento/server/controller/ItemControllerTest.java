package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.enumeration.TipoItem;
import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ItemControllerTest {

  private ItemService itemService;
  private ItemController itemController;

  @BeforeEach
  void setup() {
    itemService = Mockito.mock(ItemService.class);
    itemController = new ItemController(itemService);
  }

  @Test
  void testFindOne_ComTipoP_DeveSubtrairEmprestimo() {
    Item item = new Item();
    item.setId(1L);
    item.setTipoItem(TipoItem.P);
    item.setSaldo(new BigDecimal("10"));
    item.setDisponivelEmprestimo(new BigDecimal("3"));

    when(itemService.findOne(1L)).thenReturn(item);

    Item result = itemController.findone(1L);

    // saldo - disponivelEmprestimo = 10 - 3 = 7
    assertThat(result.getDisponivelEmprestimoCalculado()).isEqualByComparingTo("7");
  }

  @Test
  void testFindOne_ComTipoC_DeveRetornarSaldo() {
    Item item = new Item();
    item.setId(2L);
    item.setTipoItem(TipoItem.C);
    item.setSaldo(new BigDecimal("15"));

    when(itemService.findOne(2L)).thenReturn(item);

    Item result = itemController.findone(2L);

    assertThat(result.getDisponivelEmprestimoCalculado()).isEqualByComparingTo("15");
  }

  @Test
  void testFindAll_DeveCalcularParaTodosOsItens() {
    Item item1 = new Item();
    item1.setTipoItem(TipoItem.P);
    item1.setSaldo(new BigDecimal("20"));
    item1.setDisponivelEmprestimo(new BigDecimal("5"));

    Item item2 = new Item();
    item2.setTipoItem(TipoItem.C);
    item2.setSaldo(new BigDecimal("10"));

    when(itemService.findAll(Sort.by("id"))).thenReturn(Arrays.asList(item1, item2));

    List<Item> result = itemController.findAll();

    // item1: 20 - 5 = 15
    assertThat(result.get(0).getDisponivelEmprestimoCalculado()).isEqualByComparingTo("15");
    // item2: saldo = 10
    assertThat(result.get(1).getDisponivelEmprestimoCalculado()).isEqualByComparingTo("10");
  }

  @Test
  void testFindAllPaged_DeveAplicarCalculo() {
    Item item = new Item();
    item.setTipoItem(TipoItem.P);
    item.setSaldo(new BigDecimal("8"));
    item.setDisponivelEmprestimo(new BigDecimal("2"));

    Page<Item> page = new PageImpl<>(List.of(item));

    when(itemService.findAll(any(PageRequest.class))).thenReturn(page);

    Page<Item> result = itemController.findAllPaged(0, 10, null, null, null);

    assertThat(result.getContent().get(0).getDisponivelEmprestimoCalculado())
            .isEqualByComparingTo("6");
  }
}
