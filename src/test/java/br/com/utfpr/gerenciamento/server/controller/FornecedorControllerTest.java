package br.com.utfpr.gerenciamento.server.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.dto.FornecedorResponseDto;
import br.com.utfpr.gerenciamento.server.factory.FornecedorFactory;
import br.com.utfpr.gerenciamento.server.model.Fornecedor;
import br.com.utfpr.gerenciamento.server.service.FornecedorService;
import java.util.List;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class FornecedorControllerTest {

  @Mock private FornecedorService fornecedorService;

  @InjectMocks private FornecedorController fornecedorController;

  private Fornecedor fornecedor;
  private FornecedorResponseDto fornecedorResponseDto;
  private List<Fornecedor> fornecedores;
  private List<FornecedorResponseDto> fornecedoresDto;

  @BeforeEach
  void setUp() {
    fornecedor = FornecedorFactory.createFornecedorPadrao();

    fornecedorResponseDto = FornecedorFactory.createFornecedorResponseDtoPadrao();

    fornecedores = FornecedorFactory.createListaFornecedores(2);
    fornecedoresDto = FornecedorFactory.createListaFornecedoresDto(2);
  }

  @Test
  void testGetService() {

    var service = fornecedorController.getService();


    assertNotNull(service);
    assertEquals(fornecedorService, service);
  }

  @Test
  void testComplete() {

    String query = "teste";
    when(fornecedorService.completeFornecedor(query)).thenReturn(fornecedores);
    when(fornecedorService.toDto(any(Fornecedor.class)))
        .thenReturn(fornecedorResponseDto)
        .thenReturn(FornecedorFactory.createFornecedorResponseDto(2L, "Fornecedor 2"));


    List<FornecedorResponseDto> result = fornecedorController.complete(query);


    assertNotNull(result);
    assertEquals(2, result.size());
    verify(fornecedorService).completeFornecedor(query);
    verify(fornecedorService, times(2)).toDto(any(Fornecedor.class));
  }

  @Test
  void testCompleteWithEmptyQuery() {

    String query = "";
    when(fornecedorService.completeFornecedor(query)).thenReturn(fornecedores);
    when(fornecedorService.toDto(any(Fornecedor.class)))
        .thenReturn(fornecedorResponseDto)
        .thenReturn(FornecedorFactory.createFornecedorResponseDto(2L, "Fornecedor 2"));


    List<FornecedorResponseDto> result = fornecedorController.complete(query);


    assertNotNull(result);
    assertEquals(2, result.size());
    verify(fornecedorService).completeFornecedor(query);
  }

  @Test
  void testFindAll() {

    when(fornecedorService.findAll(any(Sort.class))).thenReturn(fornecedoresDto);


    List<FornecedorResponseDto> result = fornecedorController.findAll();


    assertNotNull(result);
    assertEquals(2, result.size());
    verify(fornecedorService).findAll(Sort.by("id"));
  }

  @Test
  void testSave() {

    when(fornecedorService.save(fornecedor)).thenReturn(fornecedorResponseDto);


    FornecedorResponseDto result = fornecedorController.save(fornecedor);


    assertNotNull(result);
    assertEquals(fornecedorResponseDto, result);
    verify(fornecedorService).save(fornecedor);
  }

  @Test
  void testFindOne() {

    Long id = 1L;
    when(fornecedorService.findOne(id)).thenReturn(fornecedorResponseDto);


    FornecedorResponseDto result = fornecedorController.findone(id);


    assertNotNull(result);
    assertEquals(fornecedorResponseDto, result);
    verify(fornecedorService).findOne(id);
  }

  @Test
  void testDelete() {

    Long id = 1L;
    when(fornecedorService.findOne(id)).thenReturn(fornecedorResponseDto);
    when(fornecedorService.toEntity(fornecedorResponseDto)).thenReturn(fornecedor);
    doNothing().when(fornecedorService).delete(id);


    fornecedorController.delete(id);


    verify(fornecedorService).findOne(id);
    verify(fornecedorService).toEntity(fornecedorResponseDto);
    verify(fornecedorService).delete(id);
  }

  @Test
  void testExists() {

    Long id = 1L;
    when(fornecedorService.exists(id)).thenReturn(true);


    boolean result = fornecedorController.exists(id);


    assertTrue(result);
    verify(fornecedorService).exists(id);
  }

  @Test
  void testCount() {

    when(fornecedorService.count()).thenReturn(10L);


    long result = fornecedorController.count();


    assertEquals(10L, result);
    verify(fornecedorService).count();
  }

  @Test
  void testFindAllPagedWithFilterAndOrder() {

    int page = 0;
    int size = 10;
    String filter = "teste";
    String order = "nome";
    Boolean asc = true;

    Page<FornecedorResponseDto> pageResult = new PageImpl<>(fornecedoresDto);
    when(fornecedorService.filterByAllFields(filter)).thenReturn(mock(Specification.class));
    when(fornecedorService.findAllSpecification(any(Specification.class), any(PageRequest.class)))
        .thenReturn(pageResult);


    Page<FornecedorResponseDto> result =
        fornecedorController.findAllPaged(page, size, filter, order, asc);


    assertNotNull(result);
    assertEquals(2, result.getContent().size());
    verify(fornecedorService).filterByAllFields(filter);
    verify(fornecedorService)
        .findAllSpecification(any(Specification.class), any(PageRequest.class));
  }

  @Test
  void testFindAllPagedWithoutFilter() {

    int page = 0;
    int size = 10;

    Page<FornecedorResponseDto> pageResult = new PageImpl<>(fornecedoresDto);
    when(fornecedorService.findAll(any(PageRequest.class))).thenReturn(pageResult);


    Page<FornecedorResponseDto> result =
        fornecedorController.findAllPaged(page, size, null, null, null);


    assertNotNull(result);
    assertEquals(2, result.getContent().size());
    verify(fornecedorService).findAll(any(PageRequest.class));
  }

  @Test
  void testFindAllPagedWithEmptyFilter() {

    int page = 0;
    int size = 10;
    String filter = "";

    Page<FornecedorResponseDto> pageResult = new PageImpl<>(fornecedoresDto);
    when(fornecedorService.findAll(any(PageRequest.class))).thenReturn(pageResult);


    Page<FornecedorResponseDto> result =
        fornecedorController.findAllPaged(page, size, filter, null, null);


    assertNotNull(result);
    assertEquals(2, result.getContent().size());
    verify(fornecedorService).findAll(any(PageRequest.class));
  }

  @Test
  void testFindAllPagedWithOrderButNoFilter() {

    int page = 0;
    int size = 10;
    String order = "nome";
    Boolean asc = false;

    Page<FornecedorResponseDto> pageResult = new PageImpl<>(fornecedoresDto);
    when(fornecedorService.findAll(any(PageRequest.class))).thenReturn(pageResult);


    Page<FornecedorResponseDto> result =
        fornecedorController.findAllPaged(page, size, null, order, asc);


    assertNotNull(result);
    assertEquals(2, result.getContent().size());
    verify(fornecedorService).findAll(any(PageRequest.class));
  }

  @Test
  void testPreSave() {

    assertDoesNotThrow(() -> fornecedorController.preSave(fornecedor));
  }

  @Test
  void testPostSave() {

    assertDoesNotThrow(() -> fornecedorController.postSave(fornecedor));
  }

  @Test
  void testPostDelete() {

    assertDoesNotThrow(() -> fornecedorController.postDelete(fornecedor));
  }
}
