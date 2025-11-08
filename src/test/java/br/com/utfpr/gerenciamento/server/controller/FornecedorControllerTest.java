package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.dto.FornecedorResponseDto;
import br.com.utfpr.gerenciamento.server.model.Fornecedor;
import br.com.utfpr.gerenciamento.server.service.FornecedorService;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FornecedorControllerTest {

    @Mock
    private FornecedorService fornecedorService;

    @InjectMocks
    private FornecedorController fornecedorController;

    private Fornecedor fornecedor;
    private FornecedorResponseDto fornecedorResponseDto;
    private List<Fornecedor> fornecedores;
    private List<FornecedorResponseDto> fornecedoresDto;

    @BeforeEach
    void setUp() {
        fornecedor = new Fornecedor();
        fornecedor.setId(1L);
        fornecedor.setNomeFantasia("Fornecedor Teste");

        fornecedorResponseDto = new FornecedorResponseDto();
        fornecedorResponseDto.setId(1L);
        fornecedorResponseDto.setNomeFantasia("Fornecedor Teste");

        fornecedores = Arrays.asList(fornecedor, new Fornecedor());
        fornecedoresDto = Arrays.asList(fornecedorResponseDto, new FornecedorResponseDto());
    }

    @Test
    void testGetService() {
        // When
        var service = fornecedorController.getService();

        // Then
        assertNotNull(service);
        assertEquals(fornecedorService, service);
    }

    @Test
    void testComplete() {
        // Given
        String query = "teste";
        when(fornecedorService.completeFornecedor(query)).thenReturn(fornecedores);
        when(fornecedorService.toDto(any(Fornecedor.class)))
                .thenReturn(fornecedorResponseDto)
                .thenReturn(new FornecedorResponseDto());

        // When
        List<FornecedorResponseDto> result = fornecedorController.complete(query);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(fornecedorService).completeFornecedor(query);
        verify(fornecedorService, times(2)).toDto(any(Fornecedor.class));
    }

    @Test
    void testCompleteWithEmptyQuery() {
        // Given
        String query = "";
        when(fornecedorService.completeFornecedor(query)).thenReturn(fornecedores);
        when(fornecedorService.toDto(any(Fornecedor.class)))
                .thenReturn(fornecedorResponseDto)
                .thenReturn(new FornecedorResponseDto());

        // When
        List<FornecedorResponseDto> result = fornecedorController.complete(query);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(fornecedorService).completeFornecedor(query);
    }

    @Test
    void testFindAll() {
        // Given
        when(fornecedorService.findAll(any(Sort.class))).thenReturn(fornecedoresDto);

        // When
        List<FornecedorResponseDto> result = fornecedorController.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(fornecedorService).findAll(Sort.by("id"));
    }

    @Test
    void testSave() {
        // Given
        when(fornecedorService.save(fornecedor)).thenReturn(fornecedorResponseDto);

        // When
        FornecedorResponseDto result = fornecedorController.save(fornecedor);

        // Then
        assertNotNull(result);
        assertEquals(fornecedorResponseDto, result);
        verify(fornecedorService).save(fornecedor);
    }

    @Test
    void testFindOne() {
        // Given
        Long id = 1L;
        when(fornecedorService.findOne(id)).thenReturn(fornecedorResponseDto);

        // When
        FornecedorResponseDto result = fornecedorController.findone(id);

        // Then
        assertNotNull(result);
        assertEquals(fornecedorResponseDto, result);
        verify(fornecedorService).findOne(id);
    }

    @Test
    void testDelete() {
        // Given
        Long id = 1L;
        when(fornecedorService.findOne(id)).thenReturn(fornecedorResponseDto);
        when(fornecedorService.toEntity(fornecedorResponseDto)).thenReturn(fornecedor);
        doNothing().when(fornecedorService).delete(id);

        // When
        fornecedorController.delete(id);

        // Then
        verify(fornecedorService).findOne(id);
        verify(fornecedorService).toEntity(fornecedorResponseDto);
        verify(fornecedorService).delete(id);
    }

    @Test
    void testExists() {
        // Given
        Long id = 1L;
        when(fornecedorService.exists(id)).thenReturn(true);

        // When
        boolean result = fornecedorController.exists(id);

        // Then
        assertTrue(result);
        verify(fornecedorService).exists(id);
    }

    @Test
    void testCount() {
        // Given
        when(fornecedorService.count()).thenReturn(10L);

        // When
        long result = fornecedorController.count();

        // Then
        assertEquals(10L, result);
        verify(fornecedorService).count();
    }

    @Test
    void testFindAllPagedWithFilterAndOrder() {
        // Given
        int page = 0;
        int size = 10;
        String filter = "teste";
        String order = "nome";
        Boolean asc = true;

        Page<FornecedorResponseDto> pageResult = new PageImpl<>(fornecedoresDto);
        when(fornecedorService.filterByAllFields(filter)).thenReturn(mock(Specification.class));
        when(fornecedorService.findAllSpecification(any(Specification.class), any(PageRequest.class)))
                .thenReturn(pageResult);

        // When
        Page<FornecedorResponseDto> result = fornecedorController.findAllPaged(page, size, filter, order, asc);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(fornecedorService).filterByAllFields(filter);
        verify(fornecedorService).findAllSpecification(any(Specification.class), any(PageRequest.class));
    }

    @Test
    void testFindAllPagedWithoutFilter() {
        // Given
        int page = 0;
        int size = 10;

        Page<FornecedorResponseDto> pageResult = new PageImpl<>(fornecedoresDto);
        when(fornecedorService.findAll(any(PageRequest.class))).thenReturn(pageResult);

        // When
        Page<FornecedorResponseDto> result = fornecedorController.findAllPaged(page, size, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(fornecedorService).findAll(any(PageRequest.class));
    }

    @Test
    void testFindAllPagedWithEmptyFilter() {
        // Given
        int page = 0;
        int size = 10;
        String filter = "";

        Page<FornecedorResponseDto> pageResult = new PageImpl<>(fornecedoresDto);
        when(fornecedorService.findAll(any(PageRequest.class))).thenReturn(pageResult);

        // When
        Page<FornecedorResponseDto> result = fornecedorController.findAllPaged(page, size, filter, null, null);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(fornecedorService).findAll(any(PageRequest.class));
    }

    @Test
    void testFindAllPagedWithOrderButNoFilter() {
        // Given
        int page = 0;
        int size = 10;
        String order = "nome";
        Boolean asc = false;

        Page<FornecedorResponseDto> pageResult = new PageImpl<>(fornecedoresDto);
        when(fornecedorService.findAll(any(PageRequest.class))).thenReturn(pageResult);

        // When
        Page<FornecedorResponseDto> result = fornecedorController.findAllPaged(page, size, null, order, asc);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(fornecedorService).findAll(any(PageRequest.class));
    }

    @Test
    void testPreSave() {
        // Given
        Fornecedor object = new Fornecedor();

        // When & Then - Não deve lançar exceção
        assertDoesNotThrow(() -> fornecedorController.preSave(object));
    }

    @Test
    void testPostSave() {
        // Given
        Fornecedor object = new Fornecedor();

        // When & Then - Não deve lançar exceção
        assertDoesNotThrow(() -> fornecedorController.postSave(object));
    }

    @Test
    void testPostDelete() {
        // Given
        Fornecedor object = new Fornecedor();

        // When & Then - Não deve lançar exceção
        assertDoesNotThrow(() -> fornecedorController.postDelete(object));
    }
}
