package br.com.utfpr.gerenciamento.server.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto;
import br.com.utfpr.gerenciamento.server.service.PermissaoService;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

  @Mock private UsuarioService usuarioService;

  @Mock private PermissaoService permissaoService;

  @InjectMocks private UsuarioController usuarioController;

  private UsuarioResponseDto usuarioResponseDto;
  private Page<UsuarioResponseDto> usuarioPage;

  @BeforeEach
  void setUp() {
    usuarioResponseDto = new UsuarioResponseDto();
    usuarioResponseDto.setId(1L);
    usuarioResponseDto.setNome("João Silva");
    usuarioResponseDto.setEmail("joao@test.com");

    usuarioPage =
        new PageImpl<>(Collections.singletonList(usuarioResponseDto), PageRequest.of(0, 10), 1);
  }

  @Test
  void complete_DeveRetornarPaginaComUsuarios() {
    // Given
    String query = "João";
    int page = 0;
    int size = 10;

    when(usuarioService.usuarioComplete(eq(query), any(Pageable.class))).thenReturn(usuarioPage);

    // When
    Page<UsuarioResponseDto> resultado = usuarioController.complete(query, page, size);

    // Then
    assertNotNull(resultado);
    assertEquals(1, resultado.getTotalElements());
    assertEquals(1, resultado.getContent().size());
    assertEquals("João Silva", resultado.getContent().get(0).getNome());

    verify(usuarioService).usuarioComplete(eq(query), any(Pageable.class));
  }

  @Test
  void complete_DeveUsarValoresPadraoQuandoNaoInformados() {
    // Given
    String query = "";
    int pageDefault = 0;
    int sizeDefault = 10;

    when(usuarioService.usuarioComplete(eq(query), any(Pageable.class))).thenReturn(usuarioPage);

    // When
    Page<UsuarioResponseDto> resultado =
        usuarioController.complete(query, pageDefault, sizeDefault);

    // Then
    assertNotNull(resultado);
    verify(usuarioService).usuarioComplete(eq(query), any(Pageable.class));
  }

  @Test
  void completeByUserOrDocOrNome_DeveRetornarPaginaComUsuarios() {
    // Given
    String query = "123456789";
    int page = 0;
    int size = 10;

    when(usuarioService.usuarioCompleteByUserAndDocAndNome(eq(query), any(Pageable.class)))
        .thenReturn(usuarioPage);

    // When
    Page<UsuarioResponseDto> resultado =
        usuarioController.completeByUserOrDocOrNome(query, page, size);

    // Then
    assertNotNull(resultado);
    assertEquals(1, resultado.getTotalElements());
    verify(usuarioService).usuarioCompleteByUserAndDocAndNome(eq(query), any(Pageable.class));
  }

  @Test
  void completeByUserOrDocOrNome_DeveFuncionarComQueryVazia() {
    // Given
    String query = "";
    int page = 0;
    int size = 10;

    when(usuarioService.usuarioCompleteByUserAndDocAndNome(eq(query), any(Pageable.class)))
        .thenReturn(usuarioPage);

    // When
    Page<UsuarioResponseDto> resultado =
        usuarioController.completeByUserOrDocOrNome(query, page, size);

    // Then
    assertNotNull(resultado);
    verify(usuarioService).usuarioCompleteByUserAndDocAndNome(eq(query), any(Pageable.class));
  }

  @Test
  void completeUserLabs_DeveRetornarPaginaComUsuariosDeLaboratorio() {
    // Given
    String query = "João";
    int page = 0;
    int size = 10;

    when(usuarioService.usuarioCompleteLab(eq(query), any(Pageable.class))).thenReturn(usuarioPage);

    // When
    Page<UsuarioResponseDto> resultado = usuarioController.completeUserLabs(query, page, size);

    // Then
    assertNotNull(resultado);
    assertEquals(1, resultado.getTotalElements());
    verify(usuarioService).usuarioCompleteLab(eq(query), any(Pageable.class));
  }

  @Test
  void completeUserLabs_DeveRetornarPaginaVaziaQuandoNaoHaResultados() {
    // Given
    String query = "NaoExiste";
    int page = 0;
    int size = 10;
    Page<UsuarioResponseDto> paginaVazia =
        new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), 0);

    when(usuarioService.usuarioCompleteLab(eq(query), any(Pageable.class))).thenReturn(paginaVazia);

    // When
    Page<UsuarioResponseDto> resultado = usuarioController.completeUserLabs(query, page, size);

    // Then
    assertNotNull(resultado);
    assertEquals(0, resultado.getTotalElements());
    assertTrue(resultado.getContent().isEmpty());
    verify(usuarioService).usuarioCompleteLab(eq(query), any(Pageable.class));
  }

  @Test
  void complete_DeveRespeitarTamanhoDaPaginaSolicitado() {
    // Given
    String query = "test";
    int page = 0;
    int sizeCustomizado = 5;

    Page<UsuarioResponseDto> paginaCustomizada =
        new PageImpl<>(
            Collections.singletonList(usuarioResponseDto),
            PageRequest.of(page, sizeCustomizado),
            1);

    when(usuarioService.usuarioComplete(eq(query), any(Pageable.class)))
        .thenReturn(paginaCustomizada);

    // When
    Page<UsuarioResponseDto> resultado = usuarioController.complete(query, page, sizeCustomizado);

    // Then
    assertNotNull(resultado);
    assertEquals(sizeCustomizado, resultado.getSize());
    verify(usuarioService).usuarioComplete(eq(query), any(Pageable.class));
  }

  @Test
  void complete_DevePermitirNavegacaoEntrePaginas() {
    // Given
    String query = "test";
    int page = 1; // Segunda página
    int size = 10;

    when(usuarioService.usuarioComplete(eq(query), any(Pageable.class))).thenReturn(usuarioPage);

    // When
    Page<UsuarioResponseDto> resultado = usuarioController.complete(query, page, size);

    // Then
    assertNotNull(resultado);
    verify(usuarioService).usuarioComplete(eq(query), argThat(p -> p.getPageNumber() == page));
  }
}
