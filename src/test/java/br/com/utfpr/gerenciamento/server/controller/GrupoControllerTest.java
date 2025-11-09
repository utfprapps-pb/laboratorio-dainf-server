package br.com.utfpr.gerenciamento.server.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.dto.GrupoResponseDto;
import br.com.utfpr.gerenciamento.server.dto.ItemResponseDto;
import br.com.utfpr.gerenciamento.server.model.Grupo;
import br.com.utfpr.gerenciamento.server.service.GrupoService;
import br.com.utfpr.gerenciamento.server.service.ItemService;
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

@ExtendWith(MockitoExtension.class)
class GrupoControllerTest {

  @Mock private GrupoService grupoService;

  @Mock private ItemService itemService;

  @InjectMocks private GrupoController grupoController;

  private Grupo grupo;
  private GrupoResponseDto grupoResponseDto;
  private ItemResponseDto itemResponseDto;
  private List<GrupoResponseDto> gruposDto;
  private List<ItemResponseDto> itensDto;

  @BeforeEach
  void setUp() {
    // Configurar Grupo
    grupo = new Grupo();
    grupo.setId(1L);
    grupo.setDescricao("Descrição do grupo teste");

    // Configurar GrupoResponseDto
    grupoResponseDto = new GrupoResponseDto();
    grupoResponseDto.setId(1L);
    grupoResponseDto.setDescricao("Descrição do grupo teste");

    // Configurar ItemResponseDto
    itemResponseDto = new ItemResponseDto();
    itemResponseDto.setId(1L);
    itemResponseDto.setNome("Item Teste");
    itemResponseDto.setDescricao("Descrição do item teste");

    // Configurar listas
    gruposDto = Arrays.asList(grupoResponseDto, createGrupoResponseDto(2L, "Outro Grupo"));

    itensDto = Arrays.asList(itemResponseDto, createItemResponseDto(2L, "Outro Item"));
  }

  // Métodos helper para criação de objetos
  private GrupoResponseDto createGrupoResponseDto(Long id, String nome) {
    GrupoResponseDto dto = new GrupoResponseDto();
    dto.setId(id);
    dto.setDescricao("Descrição " + nome);
    return dto;
  }

  private ItemResponseDto createItemResponseDto(Long id, String nome) {
    ItemResponseDto dto = new ItemResponseDto();
    dto.setId(id);
    dto.setDescricao("Descrição " + nome);
    return dto;
  }

  @Test
  void testGetService() {
    // When
    var service = grupoController.getService();

    // Then
    assertNotNull(service);
    assertEquals(grupoService, service);
  }

  @Test
  void testComplete() {
    // Given
    String query = "teste";
    when(grupoService.completeGrupo(query)).thenReturn(gruposDto);

    // When
    List<GrupoResponseDto> result = grupoController.complete(query);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(gruposDto, result);
    verify(grupoService).completeGrupo(query);
  }

  @Test
  void testComplete_WithEmptyQuery() {
    // Given
    String query = "";
    when(grupoService.completeGrupo(query)).thenReturn(gruposDto);

    // When
    List<GrupoResponseDto> result = grupoController.complete(query);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    verify(grupoService).completeGrupo(query);
  }

  @Test
  void testComplete_WithNullQuery() {
    // Given
    String query = null;
    when(grupoService.completeGrupo(query)).thenReturn(gruposDto);

    // When
    List<GrupoResponseDto> result = grupoController.complete(query);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    verify(grupoService).completeGrupo(query);
  }

  @Test
  void testFindItensVinculados() {
    // Given
    Long idGrupo = 1L;
    when(itemService.findByGrupo(idGrupo)).thenReturn(itensDto);

    // When
    List<ItemResponseDto> result = grupoController.findItensVinculado(idGrupo);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(itensDto, result);
    verify(itemService).findByGrupo(idGrupo);
  }

  @Test
  void testFindItensVinculados_WithInvalidId() {
    // Given
    Long idGrupo = 999L;
    when(itemService.findByGrupo(idGrupo)).thenReturn(List.of());

    // When
    List<ItemResponseDto> result = grupoController.findItensVinculado(idGrupo);

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(itemService).findByGrupo(idGrupo);
  }

  @Test
  void testFindAll() {
    // Given
    org.springframework.data.domain.Sort sort =
        org.springframework.data.domain.Sort.by(
            org.springframework.data.domain.Sort.Direction.ASC, "id");
    when(grupoService.findAll(sort)).thenReturn(gruposDto);

    // When
    List<GrupoResponseDto> result = grupoController.findAll();

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    verify(grupoService).findAll(sort);
  }

  @Test
  void testSave() {
    // Given
    when(grupoService.save(grupo)).thenReturn(grupoResponseDto);

    // When
    GrupoResponseDto result = grupoController.save(grupo);

    // Then
    assertNotNull(result);
    assertEquals(grupoResponseDto, result);
    verify(grupoService).save(grupo);
  }

  @Test
  void testFindOne() {
    // Given
    Long id = 1L;
    when(grupoService.findOne(id)).thenReturn(grupoResponseDto);

    // When
    GrupoResponseDto result = grupoController.findone(id);

    // Then
    assertNotNull(result);
    assertEquals(grupoResponseDto, result);
    verify(grupoService).findOne(id);
  }

  @Test
  void testDelete() {
    // Given
    Long id = 1L;
    when(grupoService.findOne(id)).thenReturn(grupoResponseDto);
    when(grupoService.toEntity(grupoResponseDto)).thenReturn(grupo);
    doNothing().when(grupoService).delete(id);

    // When
    grupoController.delete(id);

    // Then
    verify(grupoService).findOne(id);
    verify(grupoService).toEntity(grupoResponseDto);
    verify(grupoService).delete(id);
  }

  @Test
  void testExists() {
    // Given
    Long id = 1L;
    when(grupoService.exists(id)).thenReturn(true);

    // When
    boolean result = grupoController.exists(id);

    // Then
    assertTrue(result);
    verify(grupoService).exists(id);
  }

  @Test
  void testCount() {
    // Given
    when(grupoService.count()).thenReturn(5L);

    // When
    long result = grupoController.count();

    // Then
    assertEquals(5L, result);
    verify(grupoService).count();
  }

  @Test
  void testFindAllPaged() {
    // Given
    int page = 0;
    int size = 10;
    String filter = "teste";
    String order = "nome";
    Boolean asc = true;


    Page<GrupoResponseDto> pageResult = new PageImpl<>(gruposDto);

    // Use a mock Specification<Grupo>
    org.springframework.data.jpa.domain.Specification<Grupo> specificationMock =
        mock(org.springframework.data.jpa.domain.Specification.class);
    when(grupoService.filterByAllFields(filter)).thenReturn(specificationMock);
    when(grupoService.findAllSpecification(any(), any())).thenReturn(pageResult);

    // When
    Page<GrupoResponseDto> result = grupoController.findAllPaged(page, size, filter, order, asc);

    // Then
    assertNotNull(result);
    assertEquals(2, result.getContent().size());
    verify(grupoService).filterByAllFields(filter);
    // The controller creates a PageRequest with sorting if 'order' and 'asc' are provided
    PageRequest sortedPageRequest =
        PageRequest.of(
            page,
            size,
            asc
                ? org.springframework.data.domain.Sort.Direction.ASC
                : org.springframework.data.domain.Sort.Direction.DESC,
            order);
    verify(grupoService).findAllSpecification(specificationMock, sortedPageRequest);
  }

  @Test
  void testFindAllPaged_WithoutFilter() {
    // Given
    int page = 0;
    int size = 10;

    Page<GrupoResponseDto> pageResult = new PageImpl<>(gruposDto);
    when(grupoService.findAll(any(PageRequest.class))).thenReturn(pageResult);

    // When
    Page<GrupoResponseDto> result = grupoController.findAllPaged(page, size, null, null, null);

    // Then
    assertNotNull(result);
    assertEquals(2, result.getContent().size());
    verify(grupoService).findAll(any(PageRequest.class));
  }

  @Test
  void testFindAllPaged_WithEmptyFilter() {
    // Given
    int page = 0;
    int size = 10;
    String filter = "";

    Page<GrupoResponseDto> pageResult = new PageImpl<>(gruposDto);
    when(grupoService.findAll(any(PageRequest.class))).thenReturn(pageResult);

    // When
    Page<GrupoResponseDto> result = grupoController.findAllPaged(page, size, filter, null, null);

    // Then
    assertNotNull(result);
    assertEquals(2, result.getContent().size());
    verify(grupoService).findAll(any(PageRequest.class));
  }

  @Test
  void testFindAllPaged_WithNullParameters() {
    // Given
    int page = 0;
    int size = 10;
    PageRequest pageRequest = PageRequest.of(page, size);
    Page<GrupoResponseDto> pageResult = new PageImpl<>(gruposDto);

    when(grupoService.findAll(pageRequest)).thenReturn(pageResult);

    // When
    Page<GrupoResponseDto> result = grupoController.findAllPaged(page, size, null, null, null);

    // Then
    assertNotNull(result);
    assertEquals(2, result.getSize());
    verify(grupoService).findAll(pageRequest);
  }

  @Test
  void testFindAllPaged_WithInvalidPage() {
    // Given
    int page = -1;
    int size = 10;
    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          grupoController.findAllPaged(page, size, null, null, null);
        });
  }

  @Test
  void testPreSave() {
    // Given
    Grupo object = new Grupo();

    // When & Then - Não deve lançar exceção
    assertDoesNotThrow(() -> grupoController.preSave(object));
  }

  @Test
  void testPostSave() {
    // Given
    Grupo object = new Grupo();

    // When & Then - Não deve lançar exceção
    assertDoesNotThrow(() -> grupoController.postSave(object));
  }

  @Test
  void testPostDelete() {
    // Given
    Grupo object = new Grupo();

    // When & Then - Não deve lançar exceção
    assertDoesNotThrow(() -> grupoController.postDelete(object));
  }

  @Test
  void testConstructorInjection() {
    // When
    GrupoController controller = new GrupoController(grupoService, itemService);

    // Then
    assertNotNull(controller);
    // Verificar se os serviços foram injetados corretamente
    assertEquals(grupoService, controller.getService());
  }
}
