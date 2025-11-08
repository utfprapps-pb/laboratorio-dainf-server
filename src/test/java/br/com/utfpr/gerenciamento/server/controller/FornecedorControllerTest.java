package br.com.utfpr.gerenciamento.server.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.dto.FornecedorResponseDto;
import br.com.utfpr.gerenciamento.server.model.Cidade;
import br.com.utfpr.gerenciamento.server.model.Estado;
import br.com.utfpr.gerenciamento.server.model.Fornecedor;
import br.com.utfpr.gerenciamento.server.service.FornecedorService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class FornecedorControllerTest {

  private FornecedorService fornecedorService;
  private FornecedorController fornecedorController;

  @BeforeEach
  void setup() {
    fornecedorService = Mockito.mock(FornecedorService.class);
    fornecedorController = new FornecedorController(fornecedorService);
  }

  @Test
  void testGetService_DeveRetornarFornecedorService() {
    // When
    var result = fornecedorController.getService();

    // Then
    assertThat(result).isEqualTo(fornecedorService);
  }

  @Test
  void testComplete_DeveRetornarListaDeFornecedores() {
    // Given
    String query = "Fornecedor";

    Fornecedor fornecedor1 = criarFornecedor(1L, "Fornecedor ABC Ltda", "ABC");
    Fornecedor fornecedor2 = criarFornecedor(2L, "Fornecedor XYZ Ltda", "XYZ");

    FornecedorResponseDto dto1 = criarFornecedorResponseDto(1L, "Fornecedor ABC Ltda", "ABC");
    FornecedorResponseDto dto2 = criarFornecedorResponseDto(2L, "Fornecedor XYZ Ltda", "XYZ");

    List<Fornecedor> fornecedores = Arrays.asList(fornecedor1, fornecedor2);

    when(fornecedorService.completeFornecedor(query)).thenReturn(fornecedores);
    when(fornecedorService.toDto(fornecedor1)).thenReturn(dto1);
    when(fornecedorService.toDto(fornecedor2)).thenReturn(dto2);

    // When
    List<FornecedorResponseDto> result = fornecedorController.complete(query);

    // Then
    assertNotNull(result);
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getNomeFantasia()).isEqualTo("ABC");
    assertThat(result.get(1).getNomeFantasia()).isEqualTo("XYZ");
    verify(fornecedorService).completeFornecedor(query);
    verify(fornecedorService).toDto(fornecedor1);
    verify(fornecedorService).toDto(fornecedor2);
  }

  @Test
  void testComplete_ComQueryVazia_DeveRetornarTodos() {
    // Given
    String query = "";

    Fornecedor fornecedor = criarFornecedor(1L, "Fornecedor Teste Ltda", "Teste");
    FornecedorResponseDto dto = criarFornecedorResponseDto(1L, "Fornecedor Teste Ltda", "Teste");

    when(fornecedorService.completeFornecedor(query))
        .thenReturn(Collections.singletonList(fornecedor));
    when(fornecedorService.toDto(fornecedor)).thenReturn(dto);

    // When
    List<FornecedorResponseDto> result = fornecedorController.complete(query);

    // Then
    assertNotNull(result);
    assertThat(result).hasSize(1);
    verify(fornecedorService).completeFornecedor(query);
  }

  @Test
  void testComplete_SemResultados_DeveRetornarListaVazia() {
    // Given
    String query = "inexistente";

    when(fornecedorService.completeFornecedor(query)).thenReturn(Collections.emptyList());

    // When
    List<FornecedorResponseDto> result = fornecedorController.complete(query);

    // Then
    assertNotNull(result);
    assertThat(result).isEmpty();
    verify(fornecedorService).completeFornecedor(query);
  }

  @Test
  void testComplete_DeveConverterTodasAsEntidades() {
    // Given
    String query = "test";

    Fornecedor f1 = criarFornecedor(1L, "Teste 1", "T1");
    Fornecedor f2 = criarFornecedor(2L, "Teste 2", "T2");
    Fornecedor f3 = criarFornecedor(3L, "Teste 3", "T3");

    FornecedorResponseDto dto1 = criarFornecedorResponseDto(1L, "Teste 1", "T1");
    FornecedorResponseDto dto2 = criarFornecedorResponseDto(2L, "Teste 2", "T2");
    FornecedorResponseDto dto3 = criarFornecedorResponseDto(3L, "Teste 3", "T3");

    when(fornecedorService.completeFornecedor(query)).thenReturn(Arrays.asList(f1, f2, f3));
    when(fornecedorService.toDto(f1)).thenReturn(dto1);
    when(fornecedorService.toDto(f2)).thenReturn(dto2);
    when(fornecedorService.toDto(f3)).thenReturn(dto3);

    // When
    List<FornecedorResponseDto> result = fornecedorController.complete(query);

    // Then
    assertThat(result).hasSize(3);
    verify(fornecedorService, times(3)).toDto(any(Fornecedor.class));
  }

  // Métodos auxiliares para criar objetos de teste

  private Fornecedor criarFornecedor(Long id, String razaoSocial, String nomeFantasia) {
    Fornecedor fornecedor = new Fornecedor();
    fornecedor.setId(id);
    fornecedor.setRazaoSocial(razaoSocial);
    fornecedor.setNomeFantasia(nomeFantasia);
    fornecedor.setCnpj("12345678901234");
    fornecedor.setIe("123456789012");
    
    Estado estado = new Estado();
    estado.setId(1L);
    estado.setNome("Paraná");
    estado.setUf("PR");
    
    Cidade cidade = new Cidade();
    cidade.setId(1L);
    cidade.setNome("Pato Branco");
    cidade.setEstado(estado);
    
    fornecedor.setEstado(estado);
    fornecedor.setCidade(cidade);
    
    return fornecedor;
  }

  private FornecedorResponseDto criarFornecedorResponseDto(
      Long id, String razaoSocial, String nomeFantasia) {
    FornecedorResponseDto dto = new FornecedorResponseDto();
    dto.setId(id);
    dto.setRazaoSocial(razaoSocial);
    dto.setNomeFantasia(nomeFantasia);
    dto.setCnpj("12345678901234");
    return dto;
  }
}
