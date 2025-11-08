package br.com.utfpr.gerenciamento.server.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.utfpr.gerenciamento.server.dto.ItemResponseDto;
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
    // Given
    Item item = new Item();
    item.setId(1L);
    item.setTipoItem(TipoItem.P);
    item.setSaldo(new BigDecimal("10"));
    item.setQuantidadeEmprestada(new BigDecimal("3"));
    item.setDisponivelEmprestimoCalculado(new BigDecimal("7"));

    ItemResponseDto itemDto = new ItemResponseDto();
    itemDto.setId(1L);
    itemDto.setTipoItem(TipoItem.P);
    itemDto.setSaldo(new BigDecimal("10"));
    itemDto.setQuantidadeEmprestada(new BigDecimal("3"));
    itemDto.setDisponivelEmprestimoCalculado(new BigDecimal("7"));

    when(itemService.findOneWithDisponibilidade(1L)).thenReturn(item);
    when(itemService.toDto(item)).thenReturn(itemDto);

    // When
    ItemResponseDto result = itemController.findone(1L);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getDisponivelEmprestimoCalculado()).isEqualByComparingTo("7");
  }

  @Test
  void testFindAll_DeveRetornarListaDoService() {
    // Given
    Item item1 = new Item();
    item1.setId(1L);
    item1.setTipoItem(TipoItem.P);
    item1.setSaldo(new BigDecimal("20"));

    Item item2 = new Item();
    item2.setId(2L);
    item2.setTipoItem(TipoItem.C);
    item2.setSaldo(new BigDecimal("10"));

    ItemResponseDto itemDto1 = new ItemResponseDto();
    itemDto1.setId(1L);
    itemDto1.setTipoItem(TipoItem.P);
    itemDto1.setSaldo(new BigDecimal("20"));

    ItemResponseDto itemDto2 = new ItemResponseDto();
    itemDto2.setId(2L);
    itemDto2.setTipoItem(TipoItem.C);
    itemDto2.setSaldo(new BigDecimal("10"));

    List<ItemResponseDto> dtos = Arrays.asList(itemDto1, itemDto2);
    when(itemService.findAll(Sort.by("id"))).thenReturn(dtos);

    // When
    List<ItemResponseDto> result = itemController.findAll();

    // Then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getId()).isEqualTo(1L);
    assertThat(result.get(1).getId()).isEqualTo(2L);
    assertThat(result.get(0).getTipoItem()).isEqualTo(TipoItem.P);
    assertThat(result.get(1).getTipoItem()).isEqualTo(TipoItem.C);
  }

  @Test
  void testFindAllPaged_DeveRetornarPaginaDoService() {
    // Given
    ItemResponseDto itemDto = new ItemResponseDto();
    itemDto.setId(1L);
    itemDto.setTipoItem(TipoItem.P);
    itemDto.setSaldo(new BigDecimal("8"));

    Page<ItemResponseDto> page = new PageImpl<>(List.of(itemDto));

    when(itemService.findAll(any(PageRequest.class))).thenReturn(page);

    // When
    Page<ItemResponseDto> result = itemController.findAllPaged(0, 10, null, null, null);

    // Then
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
    assertThat(result.getContent().get(0).getTipoItem()).isEqualTo(TipoItem.P);
  }

  @Test
  void testFindAllPaged_ComFiltro_DeveRetornarPaginaFiltrada() {
    // Given
    ItemResponseDto itemDto = new ItemResponseDto();
    itemDto.setId(1L);
    itemDto.setTipoItem(TipoItem.P);
    itemDto.setSaldo(new BigDecimal("8"));

    Page<ItemResponseDto> page = new PageImpl<>(List.of(itemDto));

    when(itemService.findAllSpecification(any(), any(PageRequest.class))).thenReturn(page);

    // When
    Page<ItemResponseDto> result = itemController.findAllPaged(0, 10, "filtro", "nome", true);

    // Then
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
  }

  @Test
  void testComplete_DeveRetornarListaCompleta() {
    // Given
    ItemResponseDto itemDto1 = new ItemResponseDto();
    itemDto1.setId(1L);
    itemDto1.setTipoItem(TipoItem.P);

    ItemResponseDto itemDto2 = new ItemResponseDto();
    itemDto2.setId(2L);
    itemDto2.setTipoItem(TipoItem.C);

    List<ItemResponseDto> dtos = Arrays.asList(itemDto1, itemDto2);

    when(itemService.itemComplete("query", true)).thenReturn(dtos);

    // When
    List<ItemResponseDto> result = itemController.complete("query", true);

    // Then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getId()).isEqualTo(1L);
    assertThat(result.get(1).getId()).isEqualTo(2L);
  }

  @Test
  void testSave_DeveChamarPreSaveEPostSave() {
    // Given
    Item item = new Item();
    item.setId(1L);
    item.setTipoItem(TipoItem.P);

    ItemResponseDto itemDto = new ItemResponseDto();
    itemDto.setId(1L);
    itemDto.setTipoItem(TipoItem.P);

    when(itemService.save(item)).thenReturn(itemDto);

    // When
    ItemResponseDto result = itemController.save(item);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    Mockito.verify(itemService).save(item);
  }

  @Test
  void testDelete_DeveChamarPostDelete() {
    // Given
    Long itemId = 1L;
    Item item = new Item();
    item.setId(itemId);

    ItemResponseDto itemDto = new ItemResponseDto();
    itemDto.setId(itemId);

    when(itemService.findOne(itemId)).thenReturn(itemDto);
    when(itemService.toEntity(itemDto)).thenReturn(item);

    // When
    itemController.delete(itemId);

    // Then - Não deve lançar exceção e deve chamar o serviço
    Mockito.verify(itemService).findOne(itemId);
    Mockito.verify(itemService).toEntity(itemDto);
    Mockito.verify(itemService).delete(itemId);
  }

  @Test
  void testExists_DeveRetornarTrueQuandoItemExiste() {
    // Given
    Long itemId = 1L;
    when(itemService.exists(itemId)).thenReturn(true);

    // When
    boolean result = itemController.exists(itemId);

    // Then
    assertThat(result).isTrue();
  }

  @Test
  void testCount_DeveRetornarQuantidadeDeItens() {
    // Given
    when(itemService.count()).thenReturn(5L);

    // When
    long result = itemController.count();

    // Then
    assertThat(result).isEqualTo(5L);
  }
}
