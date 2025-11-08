package br.com.utfpr.gerenciamento.server.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.dto.FornecedorResponseDto;
import br.com.utfpr.gerenciamento.server.exception.EntityNotFoundException;
import br.com.utfpr.gerenciamento.server.model.Fornecedor;
import br.com.utfpr.gerenciamento.server.repository.FornecedorRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;

@ExtendWith(MockitoExtension.class)
class FornecedorServiceImplTest {

  @Mock private FornecedorRepository fornecedorRepository;

  @Mock private ModelMapper modelMapper;

  @InjectMocks private FornecedorServiceImpl fornecedorService;

  private Fornecedor fornecedor;
  private FornecedorResponseDto fornecedorResponseDto;
  private List<Fornecedor> fornecedores;
  private List<FornecedorResponseDto> fornecedoresDto;

  @BeforeEach
  void setUp() {
    // Criar primeiro fornecedor
    fornecedor = new Fornecedor();
    fornecedor.setId(1L);
    fornecedor.setRazaoSocial("Razão Social Teste LTDA");
    fornecedor.setNomeFantasia("Fornecedor Teste LTDA");
    fornecedor.setCnpj("12345678000195");
    fornecedor.setIe("1234567890");

    // Criar segundo fornecedor
    Fornecedor outroFornecedor = new Fornecedor();
    outroFornecedor.setId(2L);
    outroFornecedor.setRazaoSocial("Outra Razão Social LTDA");
    outroFornecedor.setNomeFantasia("Outro Fornecedor");
    outroFornecedor.setCnpj("98765432000110");
    outroFornecedor.setIe("0987654321");

    fornecedores = Arrays.asList(fornecedor, outroFornecedor);

    // Criar primeiro DTO
    fornecedorResponseDto = new FornecedorResponseDto();
    fornecedorResponseDto.setId(1L);
    fornecedorResponseDto.setRazaoSocial("Razão Social Teste LTDA");
    fornecedorResponseDto.setNomeFantasia("Fornecedor Teste LTDA");
    fornecedorResponseDto.setCnpj("12345678000195");
    fornecedorResponseDto.setIe("1234567890");

    // Criar segundo DTO
    FornecedorResponseDto outroFornecedorDto = new FornecedorResponseDto();
    outroFornecedorDto.setId(2L);
    outroFornecedorDto.setRazaoSocial("Outra Razão Social LTDA");
    outroFornecedorDto.setNomeFantasia("Outro Fornecedor");
    outroFornecedorDto.setCnpj("98765432000110");
    outroFornecedorDto.setIe("0987654321");

    fornecedoresDto = Arrays.asList(fornecedorResponseDto, outroFornecedorDto);
  }

  // Método helper para criar FornecedorResponseDto simplificado
  private FornecedorResponseDto createFornecedorResponseDto(Long id, String nomeFantasia) {
    FornecedorResponseDto dto = new FornecedorResponseDto();
    dto.setId(id);
    dto.setNomeFantasia(nomeFantasia);
    dto.setRazaoSocial("Razão Social " + nomeFantasia);
    dto.setCnpj("12345678000195");
    dto.setIe("1234567890");
    return dto;
  }

  // Método helper para criar Fornecedor simplificado
  private Fornecedor createFornecedor(Long id, String nomeFantasia) {
    fornecedor = new Fornecedor();
    fornecedor.setId(id);
    fornecedor.setNomeFantasia(nomeFantasia);
    fornecedor.setRazaoSocial("Razão Social " + nomeFantasia);
    fornecedor.setCnpj("12345678000195");
    fornecedor.setIe("1234567890");
    return fornecedor;
  }

  @Test
  void testGetRepository() {
    // When
    var repository = fornecedorService.getRepository();

    // Then
    assertNotNull(repository);
    assertEquals(fornecedorRepository, repository);
  }

  @Test
  void testToDto() {
    // Given
    when(modelMapper.map(fornecedor, FornecedorResponseDto.class))
        .thenReturn(fornecedorResponseDto);

    // When
    FornecedorResponseDto result = fornecedorService.toDto(fornecedor);

    // Then
    assertNotNull(result);
    assertEquals(fornecedorResponseDto, result);
    verify(modelMapper).map(fornecedor, FornecedorResponseDto.class);
  }

  @Test
  void testToEntity() {
    // Given
    when(modelMapper.map(fornecedorResponseDto, Fornecedor.class)).thenReturn(fornecedor);

    // When
    Fornecedor result = fornecedorService.toEntity(fornecedorResponseDto);

    // Then
    assertNotNull(result);
    assertEquals(fornecedor, result);
    verify(modelMapper).map(fornecedorResponseDto, Fornecedor.class);
  }

  @Test
  void testCompleteFornecedor_WithQuery() {
    // Given
    String query = "teste";
    when(fornecedorRepository.findByNomeFantasiaLikeIgnoreCase("%" + query + "%"))
        .thenReturn(fornecedores);

    // When
    List<Fornecedor> result = fornecedorService.completeFornecedor(query);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    verify(fornecedorRepository).findByNomeFantasiaLikeIgnoreCase("%" + query + "%");
    verify(fornecedorRepository, never()).findAll();
  }

  @Test
  void testCompleteFornecedor_WithEmptyQuery() {
    // Given
    String query = "";
    when(fornecedorRepository.findAll()).thenReturn(fornecedores);

    // When
    List<Fornecedor> result = fornecedorService.completeFornecedor(query);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    verify(fornecedorRepository).findAll();
    verify(fornecedorRepository, never()).findByNomeFantasiaLikeIgnoreCase(anyString());
  }

  @Test
  void testFindAll() {
    // Given
    when(fornecedorRepository.findAll()).thenReturn(fornecedores);
    when(modelMapper.map(any(Fornecedor.class), eq(FornecedorResponseDto.class)))
        .thenReturn(fornecedorResponseDto)
        .thenReturn(fornecedoresDto.get(1));

    // When
    List<FornecedorResponseDto> result = fornecedorService.findAll();

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    verify(fornecedorRepository).findAll();
    verify(modelMapper, times(2)).map(any(Fornecedor.class), eq(FornecedorResponseDto.class));
  }

  @Test
  void testFindAllWithSort() {
    // Given
    Sort sort = Sort.by("nomeFantasia").ascending();
    when(fornecedorRepository.findAll(sort)).thenReturn(fornecedores);
    when(modelMapper.map(any(Fornecedor.class), eq(FornecedorResponseDto.class)))
        .thenReturn(fornecedorResponseDto)
        .thenReturn(fornecedoresDto.get(1));

    // When
    List<FornecedorResponseDto> result = fornecedorService.findAll(sort);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    verify(fornecedorRepository).findAll(sort);
    verify(modelMapper, times(2)).map(any(Fornecedor.class), eq(FornecedorResponseDto.class));
  }

  @Test
  void testFindAllWithPageable() {
    // Given
    Pageable pageable = PageRequest.of(0, 10);
    Page<Fornecedor> page = new PageImpl<>(fornecedores, pageable, fornecedores.size());
    when(fornecedorRepository.findAll(pageable)).thenReturn(page);
    when(modelMapper.map(any(Fornecedor.class), eq(FornecedorResponseDto.class)))
        .thenReturn(fornecedorResponseDto)
        .thenReturn(fornecedoresDto.get(1));

    // When
    Page<FornecedorResponseDto> result = fornecedorService.findAll(pageable);

    // Then
    assertNotNull(result);
    assertEquals(2, result.getContent().size());
    assertEquals(2, result.getTotalElements());
    verify(fornecedorRepository).findAll(pageable);
    verify(modelMapper, times(2)).map(any(Fornecedor.class), eq(FornecedorResponseDto.class));
  }

  @Test
  void testSave() {
    // Given
    when(fornecedorRepository.save(fornecedor)).thenReturn(fornecedor);
    when(modelMapper.map(fornecedor, FornecedorResponseDto.class))
        .thenReturn(fornecedorResponseDto);

    // When
    FornecedorResponseDto result = fornecedorService.save(fornecedor);

    // Then
    assertNotNull(result);
    assertEquals(fornecedorResponseDto, result);
    verify(fornecedorRepository).save(fornecedor);
    verify(modelMapper).map(fornecedor, FornecedorResponseDto.class);
  }

  @Test
  void testSaveAndFlush() {
    // Given
    when(fornecedorRepository.saveAndFlush(fornecedor)).thenReturn(fornecedor);
    when(modelMapper.map(fornecedor, FornecedorResponseDto.class))
        .thenReturn(fornecedorResponseDto);

    // When
    FornecedorResponseDto result = fornecedorService.saveAndFlush(fornecedor);

    // Then
    assertNotNull(result);
    assertEquals(fornecedorResponseDto, result);
    verify(fornecedorRepository).saveAndFlush(fornecedor);
    verify(modelMapper).map(fornecedor, FornecedorResponseDto.class);
  }

  @Test
  void testFindOne() {
    // Given
    Long id = 1L;
    when(fornecedorRepository.findById(id)).thenReturn(Optional.of(fornecedor));
    when(modelMapper.map(fornecedor, FornecedorResponseDto.class))
        .thenReturn(fornecedorResponseDto);

    // When
    FornecedorResponseDto result = fornecedorService.findOne(id);

    // Then
    assertNotNull(result);
    assertEquals(fornecedorResponseDto, result);
    verify(fornecedorRepository).findById(id);
    verify(modelMapper).map(fornecedor, FornecedorResponseDto.class);
  }

  @Test
  void testFindOne_NotFound() {
    // Given
    Long id = 999L;
    when(fornecedorRepository.findById(id)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(EntityNotFoundException.class, () -> fornecedorService.findOne(id));
    verify(fornecedorRepository).findById(id);
    verify(modelMapper, never()).map(any(), eq(FornecedorResponseDto.class));
  }

  @Test
  void testExists() {
    // Given
    Long id = 1L;
    when(fornecedorRepository.existsById(id)).thenReturn(true);

    // When
    boolean result = fornecedorService.exists(id);

    // Then
    assertTrue(result);
    verify(fornecedorRepository).existsById(id);
  }

  @Test
  void testCount() {
    // Given
    when(fornecedorRepository.count()).thenReturn(10L);

    // When
    long result = fornecedorService.count();

    // Then
    assertEquals(10L, result);
    verify(fornecedorRepository).count();
  }

  @Test
  void testDeleteById() {
    // Given
    Long id = 1L;
    doNothing().when(fornecedorRepository).deleteById(id);

    // When
    fornecedorService.delete(id);

    // Then
    verify(fornecedorRepository).deleteById(id);
  }

  @Test
  void testDeleteByEntity() {
    // Given
    doNothing().when(fornecedorRepository).delete(fornecedor);

    // When
    fornecedorService.delete(fornecedor);

    // Then
    verify(fornecedorRepository).delete(fornecedor);
  }

  @Test
  void testFlush() {
    // Given
    doNothing().when(fornecedorRepository).flush();

    // When
    fornecedorService.flush();

    // Then
    verify(fornecedorRepository).flush();
  }

  @Test
  void testSaveAll() {
    // Given
    List<Fornecedor> entities =
        Arrays.asList(createFornecedor(1L, "Fornecedor 1"), createFornecedor(2L, "Fornecedor 2"));

    when(fornecedorRepository.saveAll(entities)).thenReturn(entities);
    when(modelMapper.map(any(Fornecedor.class), eq(FornecedorResponseDto.class)))
        .thenReturn(createFornecedorResponseDto(1L, "Fornecedor 1"))
        .thenReturn(createFornecedorResponseDto(2L, "Fornecedor 2"));

    // When
    Iterable<FornecedorResponseDto> result = fornecedorService.save(entities);

    // Then
    assertNotNull(result);
    verify(fornecedorRepository).saveAll(entities);
    verify(modelMapper, times(2)).map(any(Fornecedor.class), eq(FornecedorResponseDto.class));
  }

  @Test
  void testFindAllById() {
    // Given
    List<Long> ids = Arrays.asList(1L, 2L);
    when(fornecedorRepository.findAllById(ids)).thenReturn(fornecedores);
    when(modelMapper.map(any(Fornecedor.class), eq(FornecedorResponseDto.class)))
        .thenReturn(fornecedorResponseDto)
        .thenReturn(fornecedoresDto.get(1));

    // When
    List<FornecedorResponseDto> result = fornecedorService.findAllById(ids);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    verify(fornecedorRepository).findAllById(ids);
    verify(modelMapper, times(2)).map(any(Fornecedor.class), eq(FornecedorResponseDto.class));
  }

  @Test
  void testDeleteAll() {
    // Given
    doNothing().when(fornecedorRepository).deleteAll();

    // When
    fornecedorService.deleteAll();

    // Then
    verify(fornecedorRepository).deleteAll();
  }

  @Test
  void testDeleteIterable() {
    // Given
    List<Fornecedor> entities =
        Arrays.asList(createFornecedor(1L, "Fornecedor 1"), createFornecedor(2L, "Fornecedor 2"));
    doNothing().when(fornecedorRepository).deleteAll(entities);

    // When
    fornecedorService.delete(entities);

    // Then
    verify(fornecedorRepository).deleteAll(entities);
  }
}
