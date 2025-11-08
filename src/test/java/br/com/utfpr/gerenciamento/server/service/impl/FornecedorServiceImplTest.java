package br.com.utfpr.gerenciamento.server.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.dto.FornecedorResponseDto;
import br.com.utfpr.gerenciamento.server.model.Cidade;
import br.com.utfpr.gerenciamento.server.model.Estado;
import br.com.utfpr.gerenciamento.server.model.Fornecedor;
import br.com.utfpr.gerenciamento.server.repository.FornecedorRepository;
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
class FornecedorServiceImplTest {

  @Mock private FornecedorRepository fornecedorRepository;
  @Mock private ModelMapper modelMapper;

  @InjectMocks private FornecedorServiceImpl fornecedorService;

  private Fornecedor fornecedor;
  private FornecedorResponseDto fornecedorResponseDto;

  @BeforeEach
  void setUp() {
    fornecedor = criarFornecedor(1L, "Fornecedor Teste Ltda", "Teste");
    fornecedorResponseDto = criarFornecedorResponseDto(1L, "Fornecedor Teste Ltda", "Teste");
  }

  @Test
  void testGetRepository_DeveRetornarFornecedorRepository() {
    // When
    var result = fornecedorService.getRepository();

    // Then
    assertThat(result).isEqualTo(fornecedorRepository);
  }

  @Test
  void testToDto_DeveConverterFornecedorParaDTO() {
    // Given
    when(modelMapper.map(fornecedor, FornecedorResponseDto.class))
        .thenReturn(fornecedorResponseDto);

    // When
    FornecedorResponseDto result = fornecedorService.toDto(fornecedor);

    // Then
    assertNotNull(result);
    assertThat(result).isEqualTo(fornecedorResponseDto);
    verify(modelMapper).map(fornecedor, FornecedorResponseDto.class);
  }

  @Test
  void testToDto_ComFornecedorCompleto_DeveConverterCorretamente() {
    // Given
    fornecedor.setEmail("teste@example.com");
    fornecedor.setTelefone("46999999999");
    fornecedor.setObservacao("Observação teste");

    FornecedorResponseDto dtoEsperado =
        criarFornecedorResponseDto(1L, "Fornecedor Teste Ltda", "Teste");

    when(modelMapper.map(fornecedor, FornecedorResponseDto.class)).thenReturn(dtoEsperado);

    // When
    FornecedorResponseDto result = fornecedorService.toDto(fornecedor);

    // Then
    assertNotNull(result);
    verify(modelMapper).map(fornecedor, FornecedorResponseDto.class);
  }

  @Test
  void testToDto_ComFornecedorNulo_DeveRetornarNull() {
    // Given
    when(modelMapper.map(null, FornecedorResponseDto.class)).thenReturn(null);

    // When
    FornecedorResponseDto result = fornecedorService.toDto(null);

    // Then
    assertNull(result);
  }

  @Test
  void testToEntity_DeveConverterDTOParaFornecedor() {
    // Given
    when(modelMapper.map(fornecedorResponseDto, Fornecedor.class)).thenReturn(fornecedor);

    // When
    Fornecedor result = fornecedorService.toEntity(fornecedorResponseDto);

    // Then
    assertNotNull(result);
    assertThat(result).isEqualTo(fornecedor);
    verify(modelMapper).map(fornecedorResponseDto, Fornecedor.class);
  }

  @Test
  void testToEntity_ComDTONull_DeveRetornarNull() {
    // Given
    when(modelMapper.map(null, Fornecedor.class)).thenReturn(null);

    // When
    Fornecedor result = fornecedorService.toEntity(null);

    // Then
    assertNull(result);
  }

  @Test
  void testCompleteFornecedor_ComQueryVazia_DeveRetornarTodos() {
    // Given
    String query = "";

    Fornecedor f1 = criarFornecedor(1L, "Fornecedor A", "A");
    Fornecedor f2 = criarFornecedor(2L, "Fornecedor B", "B");

    List<Fornecedor> fornecedores = Arrays.asList(f1, f2);
    when(fornecedorRepository.findAll()).thenReturn(fornecedores);

    // When
    List<Fornecedor> result = fornecedorService.completeFornecedor(query);

    // Then
    assertNotNull(result);
    assertThat(result).hasSize(2);
    verify(fornecedorRepository).findAll();
    verify(fornecedorRepository, never()).findByNomeFantasiaLikeIgnoreCase(anyString());
  }

  @Test
  void testCompleteFornecedor_ComQuery_DeveBuscarPorNomeFantasia() {
    // Given
    String query = "Teste";

    Fornecedor fornecedor = criarFornecedor(1L, "Fornecedor Teste Ltda", "Teste");

    when(fornecedorRepository.findByNomeFantasiaLikeIgnoreCase("%Teste%"))
        .thenReturn(Collections.singletonList(fornecedor));

    // When
    List<Fornecedor> result = fornecedorService.completeFornecedor(query);

    // Then
    assertNotNull(result);
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getNomeFantasia()).isEqualTo("Teste");
    verify(fornecedorRepository).findByNomeFantasiaLikeIgnoreCase("%Teste%");
    verify(fornecedorRepository, never()).findAll();
  }

  @Test
  void testCompleteFornecedor_SemResultados_DeveRetornarListaVazia() {
    // Given
    String query = "Inexistente";

    when(fornecedorRepository.findByNomeFantasiaLikeIgnoreCase("%Inexistente%"))
        .thenReturn(Collections.emptyList());

    // When
    List<Fornecedor> result = fornecedorService.completeFornecedor(query);

    // Then
    assertNotNull(result);
    assertThat(result).isEmpty();
    verify(fornecedorRepository).findByNomeFantasiaLikeIgnoreCase("%Inexistente%");
  }

  @Test
  void testCompleteFornecedor_ComVariosResultados_DeveRetornarTodos() {
    // Given
    String query = "ABC";

    Fornecedor f1 = criarFornecedor(1L, "ABC Materiais", "ABC");
    Fornecedor f2 = criarFornecedor(2L, "ABC Distribuidora", "ABC Dist");
    Fornecedor f3 = criarFornecedor(3L, "ABC Equipamentos", "ABC Equip");

    List<Fornecedor> fornecedores = Arrays.asList(f1, f2, f3);
    when(fornecedorRepository.findByNomeFantasiaLikeIgnoreCase("%ABC%"))
        .thenReturn(fornecedores);

    // When
    List<Fornecedor> result = fornecedorService.completeFornecedor(query);

    // Then
    assertNotNull(result);
    assertThat(result).hasSize(3);
    verify(fornecedorRepository).findByNomeFantasiaLikeIgnoreCase("%ABC%");
  }

  @Test
  void testCompleteFornecedor_DeveConstruirQueryCorretamente() {
    // Given
    String query = "Test";

    when(fornecedorRepository.findByNomeFantasiaLikeIgnoreCase("%Test%"))
        .thenReturn(Collections.emptyList());

    // When
    fornecedorService.completeFornecedor(query);

    // Then
    verify(fornecedorRepository).findByNomeFantasiaLikeIgnoreCase("%Test%");
  }

  @Test
  void testCompleteFornecedor_CaseSensitive_DeveUsarIgnoreCase() {
    // Given
    String query = "TESTE";

    Fornecedor fornecedor = criarFornecedor(1L, "Fornecedor teste", "teste");

    when(fornecedorRepository.findByNomeFantasiaLikeIgnoreCase("%TESTE%"))
        .thenReturn(Collections.singletonList(fornecedor));

    // When
    List<Fornecedor> result = fornecedorService.completeFornecedor(query);

    // Then
    assertNotNull(result);
    assertThat(result).hasSize(1);
    verify(fornecedorRepository).findByNomeFantasiaLikeIgnoreCase("%TESTE%");
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
