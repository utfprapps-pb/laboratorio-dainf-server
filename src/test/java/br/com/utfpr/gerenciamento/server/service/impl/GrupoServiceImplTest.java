package br.com.utfpr.gerenciamento.server.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.dto.GrupoResponseDto;
import br.com.utfpr.gerenciamento.server.exception.EntityNotFoundException;
import br.com.utfpr.gerenciamento.server.model.Grupo;
import br.com.utfpr.gerenciamento.server.repository.GrupoRepository;
import java.util.Arrays;
import java.util.Collections;
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
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class GrupoServiceImplTest {

  @Mock private GrupoRepository grupoRepository;

  @Mock private ModelMapper modelMapper;

  @InjectMocks private GrupoServiceImpl service;

  private Grupo grupo;
  private GrupoResponseDto grupoDto;
  private List<Grupo> grupos;
  private List<GrupoResponseDto> gruposDto;

  @BeforeEach
  void setUp() {
    grupo = new Grupo();
    grupo.setId(1L);
    grupo.setDescricao("Grupo Teste");

    grupoDto = new GrupoResponseDto();
    grupoDto.setId(1L);
    grupoDto.setDescricao("Grupo Teste");

    // Criar lista de grupos
    Grupo grupo2 = new Grupo();
    grupo2.setId(2L);
    grupo2.setDescricao("Outro Grupo");

    grupos = Arrays.asList(grupo, grupo2);

    // Criar lista de DTOs
    GrupoResponseDto grupoDto2 = new GrupoResponseDto();
    grupoDto2.setId(2L);
    grupoDto2.setDescricao("Outro Grupo");

    gruposDto = Arrays.asList(grupoDto, grupoDto2);
  }

  @Test
  void testGetRepository() {
    // When
    var repository = service.getRepository();

    // Then
    assertNotNull(repository);
    assertEquals(grupoRepository, repository);
  }

  @Test
  void testToDto() {
    // Given
    when(modelMapper.map(grupo, GrupoResponseDto.class)).thenReturn(grupoDto);

    // When
    GrupoResponseDto result = service.toDto(grupo);

    // Then
    assertEquals(grupoDto, result);
    verify(modelMapper).map(grupo, GrupoResponseDto.class);
  }

  @Test
  void testToEntity() {
    // Given
    when(modelMapper.map(grupoDto, Grupo.class)).thenReturn(grupo);

    // When
    Grupo result = service.toEntity(grupoDto);

    // Then
    assertEquals(grupo, result);
    verify(modelMapper).map(grupoDto, Grupo.class);
  }

  @Test
  void testCompleteGrupo_NullQuery() {
    // Given
    when(grupoRepository.findAll()).thenReturn(grupos);
    when(modelMapper.map(grupo, GrupoResponseDto.class)).thenReturn(grupoDto);
    when(modelMapper.map(grupos.get(1), GrupoResponseDto.class)).thenReturn(gruposDto.get(1));

    // When
    List<GrupoResponseDto> result = service.completeGrupo(null);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    verify(grupoRepository).findAll();
    verify(modelMapper, times(2)).map(any(Grupo.class), eq(GrupoResponseDto.class));
  }

  @Test
  void testCompleteGrupo_BlankQuery() {
    // Given
    when(grupoRepository.findAll()).thenReturn(grupos);
    when(modelMapper.map(grupo, GrupoResponseDto.class)).thenReturn(grupoDto);
    when(modelMapper.map(grupos.get(1), GrupoResponseDto.class)).thenReturn(gruposDto.get(1));

    // When
    List<GrupoResponseDto> result = service.completeGrupo("");

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    verify(grupoRepository).findAll();
    verify(modelMapper, times(2)).map(any(Grupo.class), eq(GrupoResponseDto.class));
  }

  @Test
  void testCompleteGrupo_ValidQuery() {
    // Given
    String query = "teste";
    when(grupoRepository.findByDescricaoLikeIgnoreCase("%teste%"))
        .thenReturn(Collections.singletonList(grupo));
    when(modelMapper.map(grupo, GrupoResponseDto.class)).thenReturn(grupoDto);

    // When
    List<GrupoResponseDto> result = service.completeGrupo(query);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(grupoDto, result.getFirst());
    verify(grupoRepository).findByDescricaoLikeIgnoreCase("%teste%");
    verify(modelMapper).map(grupo, GrupoResponseDto.class);
  }

  @Test
  void testCompleteGrupo_QueryWithSpaces() {
    // Given
    String query = "  teste  ";
    when(grupoRepository.findByDescricaoLikeIgnoreCase("%teste%"))
        .thenReturn(Collections.singletonList(grupo));
    when(modelMapper.map(grupo, GrupoResponseDto.class)).thenReturn(grupoDto);

    // When
    List<GrupoResponseDto> result = service.completeGrupo(query);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    verify(grupoRepository).findByDescricaoLikeIgnoreCase("%teste%");
    verify(modelMapper).map(grupo, GrupoResponseDto.class);
  }

  @Test
  void testFindAll() {
    // Given
    when(grupoRepository.findAll()).thenReturn(grupos);
    when(modelMapper.map(grupo, GrupoResponseDto.class)).thenReturn(grupoDto);
    when(modelMapper.map(grupos.get(1), GrupoResponseDto.class)).thenReturn(gruposDto.get(1));

    // When
    List<GrupoResponseDto> result = service.findAll();

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    verify(grupoRepository).findAll();
    verify(modelMapper, times(2)).map(any(Grupo.class), eq(GrupoResponseDto.class));
  }

  @Test
  void testFindAllWithSort() {
    // Given
    Sort sort = Sort.by("descricao").ascending();
    when(grupoRepository.findAll(sort)).thenReturn(grupos);
    when(modelMapper.map(grupo, GrupoResponseDto.class)).thenReturn(grupoDto);
    when(modelMapper.map(grupos.get(1), GrupoResponseDto.class)).thenReturn(gruposDto.get(1));

    // When
    List<GrupoResponseDto> result = service.findAll(sort);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    verify(grupoRepository).findAll(sort);
    verify(modelMapper, times(2)).map(any(Grupo.class), eq(GrupoResponseDto.class));
  }

  @Test
  void testFindAllWithPageable() {
    // Given
    Pageable pageable = PageRequest.of(0, 10);
    Page<Grupo> page = new PageImpl<>(grupos, pageable, grupos.size());
    when(grupoRepository.findAll(pageable)).thenReturn(page);
    when(modelMapper.map(grupo, GrupoResponseDto.class)).thenReturn(grupoDto);
    when(modelMapper.map(grupos.get(1), GrupoResponseDto.class)).thenReturn(gruposDto.get(1));

    // When
    Page<GrupoResponseDto> result = service.findAll(pageable);

    // Then
    assertNotNull(result);
    assertEquals(2, result.getContent().size());
    assertEquals(2, result.getTotalElements());
    verify(grupoRepository).findAll(pageable);
    verify(modelMapper, times(2)).map(any(Grupo.class), eq(GrupoResponseDto.class));
  }

  @Test
  void testSave() {
    // Given
    when(grupoRepository.save(grupo)).thenReturn(grupo);
    when(modelMapper.map(grupo, GrupoResponseDto.class)).thenReturn(grupoDto);

    // When
    GrupoResponseDto result = service.save(grupo);

    // Then
    assertNotNull(result);
    assertEquals(grupoDto, result);
    verify(grupoRepository).save(grupo);
    verify(modelMapper).map(grupo, GrupoResponseDto.class);
  }

  @Test
  void testSaveAndFlush() {
    // Given
    when(grupoRepository.saveAndFlush(grupo)).thenReturn(grupo);
    when(modelMapper.map(grupo, GrupoResponseDto.class)).thenReturn(grupoDto);

    // When
    GrupoResponseDto result = service.saveAndFlush(grupo);

    // Then
    assertNotNull(result);
    assertEquals(grupoDto, result);
    verify(grupoRepository).saveAndFlush(grupo);
    verify(modelMapper).map(grupo, GrupoResponseDto.class);
  }

  @Test
  void testFindOne() {
    // Given
    Long id = 1L;
    when(grupoRepository.findById(id)).thenReturn(Optional.of(grupo));
    when(modelMapper.map(grupo, GrupoResponseDto.class)).thenReturn(grupoDto);

    // When
    GrupoResponseDto result = service.findOne(id);

    // Then
    assertNotNull(result);
    assertEquals(grupoDto, result);
    verify(grupoRepository).findById(id);
    verify(modelMapper).map(grupo, GrupoResponseDto.class);
  }

  @Test
  void testFindOne_NotFound() {
    // Given
    Long id = 999L;
    when(grupoRepository.findById(id)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(
        EntityNotFoundException.class,
        () ->
          service.findOne(id));
    verify(grupoRepository).findById(id);
    verify(modelMapper, never()).map(any(), eq(GrupoResponseDto.class));
  }

  @Test
  void testExists() {
    // Given
    Long id = 1L;
    when(grupoRepository.existsById(id)).thenReturn(true);

    // When
    boolean result = service.exists(id);

    // Then
    assertTrue(result);
    verify(grupoRepository).existsById(id);
  }

  @Test
  void testCount() {
    // Given
    when(grupoRepository.count()).thenReturn(5L);

    // When
    long result = service.count();

    // Then
    assertEquals(5L, result);
    verify(grupoRepository).count();
  }

  @Test
  void testDeleteById() {
    // Given
    Long id = 1L;
    doNothing().when(grupoRepository).deleteById(id);

    // When
    service.delete(id);

    // Then
    verify(grupoRepository).deleteById(id);
  }

  @Test
  void testDeleteByEntity() {
    // Given
    doNothing().when(grupoRepository).delete(grupo);

    // When
    service.delete(grupo);

    // Then
    verify(grupoRepository).delete(grupo);
  }

  @Test
  void testFlush() {
    // Given
    doNothing().when(grupoRepository).flush();

    // When
    service.flush();

    // Then
    verify(grupoRepository).flush();
  }

  @Test
  void testSaveAll() {
    // Given
    List<Grupo> entities = Arrays.asList(grupo, new Grupo());
    when(grupoRepository.saveAll(entities)).thenReturn(entities);
    when(modelMapper.map(grupo, GrupoResponseDto.class)).thenReturn(grupoDto);
    when(modelMapper.map(entities.get(1), GrupoResponseDto.class))
        .thenReturn(new GrupoResponseDto());

    // When
    Iterable<GrupoResponseDto> result = service.save(entities);

    // Then
    assertNotNull(result);
    verify(grupoRepository).saveAll(entities);
    verify(modelMapper, times(2)).map(any(Grupo.class), eq(GrupoResponseDto.class));
  }

  @Test
  void testFindAllById() {
    // Given
    List<Long> ids = Arrays.asList(1L, 2L);
    when(grupoRepository.findAllById(ids)).thenReturn(grupos);
    when(modelMapper.map(grupo, GrupoResponseDto.class)).thenReturn(grupoDto);
    when(modelMapper.map(grupos.get(1), GrupoResponseDto.class)).thenReturn(gruposDto.get(1));

    // When
    List<GrupoResponseDto> result = service.findAllById(ids);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    verify(grupoRepository).findAllById(ids);
    verify(modelMapper, times(2)).map(any(Grupo.class), eq(GrupoResponseDto.class));
  }

  @Test
  void testDeleteAll() {
    // Given
    doNothing().when(grupoRepository).deleteAll();

    // When
    service.deleteAll();

    // Then
    verify(grupoRepository).deleteAll();
  }

  @Test
  void testDeleteIterable() {
    // Given
    List<Grupo> entities = Arrays.asList(grupo, new Grupo());
    doNothing().when(grupoRepository).deleteAll(entities);

    // When
    service.delete(entities);

    // Then
    verify(grupoRepository).deleteAll(entities);
  }

  @Test
  void testFilterByAllFields() {
    // Given
    String filter = "teste";
    Pageable pageable = PageRequest.of(0, 10);
    Page<Grupo> page = new PageImpl<>(Collections.singletonList(grupo), pageable, 1);

    when(grupoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
    when(modelMapper.map(grupo, GrupoResponseDto.class)).thenReturn(grupoDto);

    // When
    Page<GrupoResponseDto> result =
        service.findAllSpecification(service.filterByAllFields(filter), pageable);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getContent().size());
    verify(grupoRepository).findAll(any(Specification.class), eq(pageable));
    verify(modelMapper).map(grupo, GrupoResponseDto.class);
  }

  @Test
  void testFilterByAllFields_EmptyFilter() {
    // Given
    String filter = "";
    Pageable pageable = PageRequest.of(0, 10);
    Page<Grupo> page = new PageImpl<>(grupos, pageable, grupos.size());

    when(grupoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
    when(modelMapper.map(grupo, GrupoResponseDto.class)).thenReturn(grupoDto);
    when(modelMapper.map(grupos.get(1), GrupoResponseDto.class)).thenReturn(gruposDto.get(1));

    // When
    Page<GrupoResponseDto> result =
        service.findAllSpecification(service.filterByAllFields(filter), pageable);

    // Then
    assertNotNull(result);
    assertEquals(2, result.getContent().size());
    verify(grupoRepository).findAll(any(Specification.class), eq(pageable));
    verify(modelMapper, times(2)).map(any(Grupo.class), eq(GrupoResponseDto.class));
  }

  @Test
  void testConstructor() {
    // When
    GrupoServiceImpl grupoService = new GrupoServiceImpl(grupoRepository, modelMapper);

    // Then
    assertNotNull(grupoService);
    // Verificar se as dependências foram injetadas corretamente
    assertEquals(grupoRepository, grupoService.getRepository());
  }
}
