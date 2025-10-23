package br.com.utfpr.gerenciamento.server.controller;
import br.com.utfpr.gerenciamento.server.dto.ItemResponseDto;
import br.com.utfpr.gerenciamento.server.enumeration.TipoItem;
import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.model.ItemImage;
import br.com.utfpr.gerenciamento.server.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ItemControllerTest {

    private ItemService itemService;
    private ItemController itemController;

    @BeforeEach
    void setUp() {
        itemService = mock(ItemService.class);
        itemController = new ItemController(itemService);
    }

    @Test
    void testFindOne_ComTipoP_DeveCalcularDisponivelEmprestimo() {
        Item item = new Item();
        item.setId(1L);
        item.setSaldo(new BigDecimal("10"));
        item.setTipoItem(TipoItem.P);

        when(itemService.findOne(1L)).thenReturn(item);
        when(itemService.disponivelParaEmprestimo(1L)).thenReturn(new BigDecimal("4"));

        Item result = itemController.findone(1L);

        assertEquals(new BigDecimal("6"), result.getDisponivelEmprestimoCalculado());
        verify(itemService).findOne(1L);
    }

    @Test
    void testFindOne_ComOutroTipo_DeveUsarSaldo() {
        Item item = new Item();
        item.setId(2L);
        item.setSaldo(new BigDecimal("8"));
        item.setTipoItem(TipoItem.C);

        when(itemService.findOne(2L)).thenReturn(item);

        Item result = itemController.findone(2L);
        assertEquals(new BigDecimal("8"), result.getDisponivelEmprestimoCalculado());
    }

    @Test
    void testFindAll_DeveCalcularParaItensTipoP() {
        Item item1 = new Item();
        item1.setId(1L);
        item1.setSaldo(new BigDecimal("10"));
        item1.setTipoItem(TipoItem.P);

        Item item2 = new Item();
        item2.setId(2L);
        item2.setSaldo(new BigDecimal("5"));
        item2.setTipoItem(TipoItem.C);

        when(itemService.findAll(any(Sort.class))).thenReturn(Arrays.asList(item1, item2));
        when(itemService.disponivelParaEmprestimo(1L)).thenReturn(new BigDecimal("3"));

        List<Item> result = itemController.findAll();

        assertEquals(2, result.size());
        assertEquals(new BigDecimal("7"), result.get(0).getDisponivelEmprestimoCalculado());
        assertEquals(new BigDecimal("5"), result.get(1).getDisponivelEmprestimoCalculado());
    }

    @Test
    void testFindAllPaged_SemFiltro_DeveManterPaginacao() {
        Item item = new Item();
        item.setId(1L);
        item.setSaldo(new BigDecimal("12"));
        item.setTipoItem(TipoItem.P);

        Page<Item> page = new PageImpl<>(Collections.singletonList(item));
        when(itemService.findAll(any(PageRequest.class))).thenReturn(page);
        when(itemService.disponivelParaEmprestimo(1L)).thenReturn(new BigDecimal("2"));

        Page<Item> result = itemController.findAllPaged(0, 10, null, null, null);

        assertEquals(1, result.getTotalElements());
        assertEquals(new BigDecimal("10"), result.getContent().get(0).getDisponivelEmprestimoCalculado());
    }

    @Test
    void testFindAllPaged_ComFiltro_DeveUsarSpecification() {
        Item item = new Item();
        item.setId(1L);
        item.setSaldo(new BigDecimal("20"));
        item.setTipoItem(TipoItem.C);

        Page<Item> page = new PageImpl<>(Collections.singletonList(item));
        when(itemService.filterByAllFields(anyString())).thenReturn(mock(Specification.class));
        when(itemService.findAllSpecification(any(), any(PageRequest.class))).thenReturn(page);

        Page<Item> result = itemController.findAllPaged(0, 10, "teste", null, null);

        assertEquals(1, result.getTotalElements());
        assertEquals(new BigDecimal("20"), result.getContent().get(0).getDisponivelEmprestimoCalculado());
    }


    @Test
    void testPostSave_SemImagensNaoCopia() {
        Item item = new Item();
        item.setId(11L);

        itemController.postSave(item);

        verify(itemService, never()).copyImagesItem(anyList(), anyLong());
    }
}