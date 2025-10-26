package br.com.utfpr.gerenciamento.server.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.utfpr.gerenciamento.server.enumeration.TipoItem;
import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.service.ItemService;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.*;

class ItemControllerTest {

  private ItemService itemService;
  private ItemController itemController;

  @BeforeEach
  void setup() {
    itemService = Mockito.mock(ItemService.class);
    itemController = new ItemController(itemService);
  }

  @Test
  void testFindOne_DeveDelegarParaService() {
    Item item = new Item();
    item.setId(1L);
    item.setTipoItem(TipoItem.P);
    item.setSaldo(new BigDecimal("10"));
    item.setQuantidadeEmprestada(new BigDecimal("3"));
    item.setDisponivelEmprestimoCalculado(new BigDecimal("7"));

    when(itemService.findOneWithDisponibilidade(1L)).thenReturn(item);

    Item result = itemController.findone(1L);

    assertThat(result).isEqualTo(item);
    assertThat(result.getDisponivelEmprestimoCalculado()).isEqualByComparingTo("7");
  }

  @Test
  void testFindAll_DeveRetornarListaDoService() {
    Item item1 = new Item();
    item1.setTipoItem(TipoItem.P);
    item1.setSaldo(new BigDecimal("20"));

    Item item2 = new Item();
    item2.setTipoItem(TipoItem.C);
    item2.setSaldo(new BigDecimal("10"));

    when(itemService.findAll(Sort.by("id"))).thenReturn(Arrays.asList(item1, item2));

    List<Item> result = itemController.findAll();

    assertThat(result).hasSize(2);
    assertThat(result.get(0)).isEqualTo(item1);
    assertThat(result.get(1)).isEqualTo(item2);
  }

  @Test
  void testFindAllPaged_DeveRetornarPaginaDoService() {
    Item item = new Item();
    item.setTipoItem(TipoItem.P);
    item.setSaldo(new BigDecimal("8"));

    Page<Item> page = new PageImpl<>(List.of(item));

    when(itemService.findAll(any(PageRequest.class))).thenReturn(page);

    Page<Item> result = itemController.findAllPaged(0, 10, null, null, null);

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0)).isEqualTo(item);
  }
}
