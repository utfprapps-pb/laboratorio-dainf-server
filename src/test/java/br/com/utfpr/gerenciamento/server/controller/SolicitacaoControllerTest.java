package br.com.utfpr.gerenciamento.server.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.dto.SolicitacaoResponseDto;
import br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto;
import br.com.utfpr.gerenciamento.server.service.SolicitacaoService;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SolicitacaoControllerTest {

  private SolicitacaoService solicitacaoService;
  private SolicitacaoController solicitacaoController;

  @BeforeEach
  void setup() {
    solicitacaoService = Mockito.mock(SolicitacaoService.class);
    solicitacaoController = new SolicitacaoController(solicitacaoService);
  }

  @Test
  void testGetService_DeveRetornarSolicitacaoService() {
    // When
    var result = solicitacaoController.getService();

    // Then
    assertThat(result).isEqualTo(solicitacaoService);
  }

  @Test
  void testFindAllByUsername_DeveRetornarListaDeSolicitacoes() {
    // Given
    String username = "joao.silva";
    UsuarioResponseDto usuario = criarUsuarioResponseDto(1L, username);

    SolicitacaoResponseDto sol1 = criarSolicitacaoResponseDto(1L, usuario);
    sol1.setDescricao("Solicitação de notebooks");

    SolicitacaoResponseDto sol2 = criarSolicitacaoResponseDto(2L, usuario);
    sol2.setDescricao("Solicitação de monitores");

    List<SolicitacaoResponseDto> solicitacoes = Arrays.asList(sol1, sol2);

    when(solicitacaoService.findAllByUsername(username)).thenReturn(solicitacoes);

    // When
    List<SolicitacaoResponseDto> result = solicitacaoController.findAllByUsername(username);

    // Then
    assertNotNull(result);
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getDescricao()).isEqualTo("Solicitação de notebooks");
    assertThat(result.get(1).getDescricao()).isEqualTo("Solicitação de monitores");
    assertThat(result.get(0).getUsuario().getUsername()).isEqualTo(username);
    verify(solicitacaoService).findAllByUsername(username);
  }

  @Test
  void testFindAllByUsername_SemSolicitacoes_DeveRetornarListaVazia() {
    // Given
    String username = "maria.santos";

    when(solicitacaoService.findAllByUsername(username)).thenReturn(Collections.emptyList());

    // When
    List<SolicitacaoResponseDto> result = solicitacaoController.findAllByUsername(username);

    // Then
    assertNotNull(result);
    assertThat(result).isEmpty();
    verify(solicitacaoService).findAllByUsername(username);
  }

  @Test
  void testFindAllByUsername_ComUmaSolicitacao_DeveRetornarLista() {
    // Given
    String username = "pedro.costa";
    UsuarioResponseDto usuario = criarUsuarioResponseDto(5L, username);

    SolicitacaoResponseDto solicitacao = criarSolicitacaoResponseDto(10L, usuario);
    solicitacao.setDescricao("Solicitação única de equipamentos");
    solicitacao.setObservacao("Urgente");

    when(solicitacaoService.findAllByUsername(username))
        .thenReturn(Collections.singletonList(solicitacao));

    // When
    List<SolicitacaoResponseDto> result = solicitacaoController.findAllByUsername(username);

    // Then
    assertNotNull(result);
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(10L);
    assertThat(result.get(0).getDescricao()).isEqualTo("Solicitação única de equipamentos");
    assertThat(result.get(0).getObservacao()).isEqualTo("Urgente");
    verify(solicitacaoService).findAllByUsername(username);
  }

  @Test
  void testFindAllByUsername_DeveDelegarParaService() {
    // Given
    String username = "ana.oliveira";
    List<SolicitacaoResponseDto> solicitacoesEsperadas = Collections.emptyList();

    when(solicitacaoService.findAllByUsername(username)).thenReturn(solicitacoesEsperadas);

    // When
    List<SolicitacaoResponseDto> result = solicitacaoController.findAllByUsername(username);

    // Then
    assertThat(result).isEqualTo(solicitacoesEsperadas);
    verify(solicitacaoService, times(1)).findAllByUsername(username);
  }

  @Test
  void testFindAllByUsername_ComVariasSolicitacoes_DeveRetornarTodas() {
    // Given
    String username = "carlos.pereira";
    UsuarioResponseDto usuario = criarUsuarioResponseDto(3L, username);

    SolicitacaoResponseDto sol1 = criarSolicitacaoResponseDto(1L, usuario);
    sol1.setDescricao("Solicitação 1");
    sol1.setDataSolicitacao(LocalDate.of(2025, 1, 15));

    SolicitacaoResponseDto sol2 = criarSolicitacaoResponseDto(2L, usuario);
    sol2.setDescricao("Solicitação 2");
    sol2.setDataSolicitacao(LocalDate.of(2025, 2, 20));

    SolicitacaoResponseDto sol3 = criarSolicitacaoResponseDto(3L, usuario);
    sol3.setDescricao("Solicitação 3");
    sol3.setDataSolicitacao(LocalDate.of(2025, 3, 10));

    List<SolicitacaoResponseDto> solicitacoes = Arrays.asList(sol1, sol2, sol3);

    when(solicitacaoService.findAllByUsername(username)).thenReturn(solicitacoes);

    // When
    List<SolicitacaoResponseDto> result = solicitacaoController.findAllByUsername(username);

    // Then
    assertNotNull(result);
    assertThat(result).hasSize(3);
    assertThat(result.get(0).getDataSolicitacao()).isEqualTo(LocalDate.of(2025, 1, 15));
    assertThat(result.get(1).getDataSolicitacao()).isEqualTo(LocalDate.of(2025, 2, 20));
    assertThat(result.get(2).getDataSolicitacao()).isEqualTo(LocalDate.of(2025, 3, 10));
    verify(solicitacaoService).findAllByUsername(username);
  }

  @Test
  void testFindAllByUsername_ComUsernamesDiferentes_DeveRetornarSolicitacoesCorretas() {
    // Given
    String username1 = "usuario1";
    String username2 = "usuario2";

    UsuarioResponseDto user1 = criarUsuarioResponseDto(1L, username1);
    UsuarioResponseDto user2 = criarUsuarioResponseDto(2L, username2);

    SolicitacaoResponseDto sol1 = criarSolicitacaoResponseDto(1L, user1);
    SolicitacaoResponseDto sol2 = criarSolicitacaoResponseDto(2L, user2);

    when(solicitacaoService.findAllByUsername(username1))
        .thenReturn(Collections.singletonList(sol1));
    when(solicitacaoService.findAllByUsername(username2))
        .thenReturn(Collections.singletonList(sol2));

    // When
    List<SolicitacaoResponseDto> result1 = solicitacaoController.findAllByUsername(username1);
    List<SolicitacaoResponseDto> result2 = solicitacaoController.findAllByUsername(username2);

    // Then
    assertThat(result1).hasSize(1);
    assertThat(result1.get(0).getUsuario().getUsername()).isEqualTo(username1);

    assertThat(result2).hasSize(1);
    assertThat(result2.get(0).getUsuario().getUsername()).isEqualTo(username2);

    verify(solicitacaoService).findAllByUsername(username1);
    verify(solicitacaoService).findAllByUsername(username2);
  }

  @Test
  void testFindAllByUsername_ComSolicitacoesComObservacoes_DeveRetornarCompleto() {
    // Given
    String username = "teste.usuario";
    UsuarioResponseDto usuario = criarUsuarioResponseDto(7L, username);

    SolicitacaoResponseDto sol1 = criarSolicitacaoResponseDto(1L, usuario);
    sol1.setDescricao("Solicitação com observação");
    sol1.setObservacao("Precisa de aprovação urgente");

    SolicitacaoResponseDto sol2 = criarSolicitacaoResponseDto(2L, usuario);
    sol2.setDescricao("Solicitação sem observação");
    sol2.setObservacao(null);

    List<SolicitacaoResponseDto> solicitacoes = Arrays.asList(sol1, sol2);

    when(solicitacaoService.findAllByUsername(username)).thenReturn(solicitacoes);

    // When
    List<SolicitacaoResponseDto> result = solicitacaoController.findAllByUsername(username);

    // Then
    assertNotNull(result);
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getObservacao()).isEqualTo("Precisa de aprovação urgente");
    assertThat(result.get(1).getObservacao()).isNull();
    verify(solicitacaoService).findAllByUsername(username);
  }

  @Test
  void testFindAllByUsername_ChamadaMultiplasVezes_DeveDelegarCadaVez() {
    // Given
    String username = "admin";
    List<SolicitacaoResponseDto> lista1 = Collections.emptyList();
    List<SolicitacaoResponseDto> lista2 = Collections.emptyList();

    when(solicitacaoService.findAllByUsername(username)).thenReturn(lista1).thenReturn(lista2);

    // When
    solicitacaoController.findAllByUsername(username);
    solicitacaoController.findAllByUsername(username);

    // Then
    verify(solicitacaoService, times(2)).findAllByUsername(username);
  }

  @Test
  void testFindAllByUsername_ComUsernameEspecial_DeveTratarCorretamente() {
    // Given
    String username = "user.name-123";
    UsuarioResponseDto usuario = criarUsuarioResponseDto(99L, username);
    SolicitacaoResponseDto sol = criarSolicitacaoResponseDto(1L, usuario);

    when(solicitacaoService.findAllByUsername(username)).thenReturn(Collections.singletonList(sol));

    // When
    List<SolicitacaoResponseDto> result = solicitacaoController.findAllByUsername(username);

    // Then
    assertNotNull(result);
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getUsuario().getUsername()).isEqualTo(username);
    verify(solicitacaoService).findAllByUsername(username);
  }

  @Test
  void testFindAllByUsername_VerificaRetornoNuncaNulo() {
    // Given
    String username = "qualquer.usuario";

    when(solicitacaoService.findAllByUsername(username)).thenReturn(Collections.emptyList());

    // When
    List<SolicitacaoResponseDto> result = solicitacaoController.findAllByUsername(username);

    // Then
    assertNotNull(result);
    verify(solicitacaoService).findAllByUsername(username);
  }

  // Métodos auxiliares para criar objetos de teste

  private SolicitacaoResponseDto criarSolicitacaoResponseDto(Long id, UsuarioResponseDto usuario) {
    SolicitacaoResponseDto dto = new SolicitacaoResponseDto();
    dto.setId(id);
    dto.setDescricao("Descrição padrão");
    dto.setDataSolicitacao(LocalDate.now());
    dto.setUsuario(usuario);
    dto.setSolicitacaoItem(Collections.emptyList());
    return dto;
  }

  private UsuarioResponseDto criarUsuarioResponseDto(Long id, String username) {
    UsuarioResponseDto dto = new UsuarioResponseDto();
    dto.setId(id);
    dto.setUsername(username);
    dto.setNome("Nome " + username);
    return dto;
  }
}
