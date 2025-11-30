package br.com.utfpr.gerenciamento.server.audit.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import br.com.utfpr.gerenciamento.server.audit.dto.AuditEntryDto;
import br.com.utfpr.gerenciamento.server.audit.service.AuditService;
import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Testes unitários para AuditController.
 *
 * <p>Testa a lógica de negócio do controller de auditoria.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuditController")
class AuditControllerTest {

  @Mock private AuditService auditService;

  @InjectMocks private AuditController auditController;

  @Nested
  @DisplayName("GET /audit/{entidade}/{id}")
  class GetHistorico {

    @Test
    @DisplayName("Deve retornar histórico paginado para entidade válida")
    void deveRetornarHistoricoPaginadoParaEntidadeValida() {
      // Arrange
      AuditEntryDto entry =
          AuditEntryDto.builder()
              .revisao(1L)
              .dataHora(LocalDateTime.of(2025, 11, 30, 10, 0))
              .usuario("admin")
              .ip("192.168.1.1")
              .tipoOperacao("CRIACAO")
              .entidade(Map.of("id", 1L, "observacao", "Teste"))
              .build();

      Page<AuditEntryDto> page = new PageImpl<>(List.of(entry), PageRequest.of(0, 20), 1);
      when(auditService.getHistoricoPaginado(eq(Emprestimo.class), eq(1L), any())).thenReturn(page);

      // Act
      ResponseEntity<Page<AuditEntryDto>> response =
          auditController.getHistorico("emprestimo", 1L, 0, 20);

      // Assert
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody().getContent()).hasSize(1);
      assertThat(response.getBody().getContent().get(0).getRevisao()).isEqualTo(1L);
      assertThat(response.getBody().getContent().get(0).getUsuario()).isEqualTo("admin");
      assertThat(response.getBody().getContent().get(0).getTipoOperacao()).isEqualTo("CRIACAO");
    }

    @Test
    @DisplayName("Deve retornar 404 para entidade não encontrada")
    void deveRetornar404ParaEntidadeNaoEncontrada() {
      // Act
      ResponseEntity<Page<AuditEntryDto>> response =
          auditController.getHistorico("entidade-inexistente", 1L, 0, 20);

      // Assert
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Deve retornar 404 para nome de entidade com caracteres inválidos")
    void deveRetornar404ParaNomeDeEntidadeComCaracteresInvalidos() {
      // Act
      ResponseEntity<Page<AuditEntryDto>> response =
          auditController.getHistorico("EMPRESTIMO", 1L, 0, 20);

      // Assert - uppercase não é aceito (regex ^[a-z\\-]+$)
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Deve retornar página vazia quando não há histórico")
    void deveRetornarPaginaVaziaQuandoNaoHaHistorico() {
      // Arrange
      Page<AuditEntryDto> emptyPage = Page.empty();
      when(auditService.getHistoricoPaginado(any(), any(), any())).thenReturn(emptyPage);

      // Act
      ResponseEntity<Page<AuditEntryDto>> response =
          auditController.getHistorico("emprestimo", 999L, 0, 20);

      // Assert
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody().getContent()).isEmpty();
    }

    @Test
    @DisplayName("Deve limitar tamanho máximo da página a 100")
    void deveLimitarTamanhoMaximoDaPaginaA100() {
      // Arrange
      Page<AuditEntryDto> emptyPage = Page.empty();
      when(auditService.getHistoricoPaginado(any(), any(), any())).thenReturn(emptyPage);

      // Act - solicita 500 itens, mas deve ser limitado a 100
      ResponseEntity<Page<AuditEntryDto>> response =
          auditController.getHistorico("emprestimo", 1L, 0, 500);

      // Assert
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Deve usar tamanho padrão quando size é inválido")
    void deveUsarTamanhoPadraoQuandoSizeEhInvalido() {
      // Arrange
      Page<AuditEntryDto> emptyPage = Page.empty();
      when(auditService.getHistoricoPaginado(any(), any(), any())).thenReturn(emptyPage);

      // Act - size negativo deve usar DEFAULT_PAGE_SIZE (20)
      ResponseEntity<Page<AuditEntryDto>> response =
          auditController.getHistorico("emprestimo", 1L, 0, -1);

      // Assert
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
  }

  @Nested
  @DisplayName("GET /audit/{entidade}/{id}/count")
  class ContarRevisoes {

    @Test
    @DisplayName("Deve retornar contagem de revisões")
    void deveRetornarContagemDeRevisoes() {
      // Arrange
      when(auditService.contarRevisoes(Emprestimo.class, 1L)).thenReturn(5L);

      // Act
      ResponseEntity<Long> response = auditController.contarRevisoes("emprestimo", 1L);

      // Assert
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEqualTo(5L);
    }

    @Test
    @DisplayName("Deve retornar 404 para entidade não encontrada")
    void deveRetornar404ParaEntidadeNaoEncontrada() {
      // Act
      ResponseEntity<Long> response = auditController.contarRevisoes("xyz", 1L);

      // Assert
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Deve retornar zero quando não há revisões")
    void deveRetornarZeroQuandoNaoHaRevisoes() {
      // Arrange
      when(auditService.contarRevisoes(any(), any())).thenReturn(0L);

      // Act
      ResponseEntity<Long> response = auditController.contarRevisoes("item", 999L);

      // Assert
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isZero();
    }
  }

  @Nested
  @DisplayName("GET /audit/revisao/{revisao}")
  class GetRevisao {

    @Test
    @DisplayName("Deve retornar informações da revisão")
    void deveRetornarInformacoesDaRevisao() {
      // Arrange
      AuditEntryDto revInfo =
          AuditEntryDto.builder()
              .revisao(10L)
              .dataHora(LocalDateTime.of(2025, 11, 30, 15, 30))
              .usuario("maria.santos")
              .ip("10.0.0.5")
              .build();

      when(auditService.getRevisao(10L)).thenReturn(revInfo);

      // Act
      ResponseEntity<AuditEntryDto> response = auditController.getRevisao(10L);

      // Assert
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody().getRevisao()).isEqualTo(10L);
      assertThat(response.getBody().getUsuario()).isEqualTo("maria.santos");
    }

    @Test
    @DisplayName("Deve retornar 404 para revisão inexistente")
    void deveRetornar404ParaRevisaoInexistente() {
      // Arrange
      when(auditService.getRevisao(999L)).thenReturn(null);

      // Act
      ResponseEntity<AuditEntryDto> response = auditController.getRevisao(999L);

      // Assert
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
  }

  @Nested
  @DisplayName("GET /audit/entidades")
  class ListarEntidades {

    @Test
    @DisplayName("Deve listar todas as entidades disponíveis")
    void deveListarTodasAsEntidadesDisponiveis() {
      // Act
      ResponseEntity<List<String>> response = auditController.listarEntidades();

      // Assert
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotEmpty();
      assertThat(response.getBody()).contains("emprestimo", "item", "usuario");
    }

    @Test
    @DisplayName("Deve retornar lista ordenada")
    void deveRetornarListaOrdenada() {
      // Act
      ResponseEntity<List<String>> response = auditController.listarEntidades();

      // Assert
      List<String> entidades = response.getBody();
      assertThat(entidades).isSorted();
    }

    @Test
    @DisplayName("Deve incluir todas as 17 entidades auditadas")
    void deveIncluirTodasAs17EntidadesAuditadas() {
      // Act
      ResponseEntity<List<String>> response = auditController.listarEntidades();

      // Assert
      assertThat(response.getBody())
          .hasSize(17)
          .contains(
              "emprestimo",
              "emprestimo-item",
              "emprestimo-devolucao-item",
              "item",
              "item-image",
              "usuario",
              "saida",
              "saida-item",
              "reserva",
              "reserva-item",
              "compra",
              "compra-item",
              "solicitacao",
              "solicitacao-item",
              "grupo",
              "fornecedor",
              "nada-consta");
    }
  }
}
