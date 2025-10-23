package br.com.utfpr.gerenciamento.server.mapper;

import static org.junit.jupiter.api.Assertions.*;

import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.model.EmprestimoDevolucaoItem;
import br.com.utfpr.gerenciamento.server.model.EmprestimoItem;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EmprestimoTemplateMapperTest {

  private EmprestimoTemplateMapper mapper;
  private Emprestimo emprestimo;
  private List<EmprestimoItem> emprestimoItems;
  private List<EmprestimoDevolucaoItem> devolucaoItems;

  @BeforeEach
  void setUp() {
    mapper = new EmprestimoTemplateMapper();

    // Configurar usuário do empréstimo
    Usuario usuarioEmprestimo = new Usuario();
    usuarioEmprestimo.setNome("João Silva");

    // Configurar usuário responsável
    Usuario usuarioResponsavel = new Usuario();
    usuarioResponsavel.setNome("Maria Santos");

    // Configurar listas de itens
    emprestimoItems = Collections.emptyList();
    devolucaoItems = Collections.emptyList();

    // Configurar empréstimo
    emprestimo = new Emprestimo();
    emprestimo.setUsuarioEmprestimo(usuarioEmprestimo);
    emprestimo.setUsuarioResponsavel(usuarioResponsavel);
    emprestimo.setDataEmprestimo(LocalDate.of(2025, 10, 15));
    emprestimo.setPrazoDevolucao(LocalDate.of(2025, 10, 20));
    emprestimo.setEmprestimoItem(emprestimoItems);
    emprestimo.setEmprestimoDevolucaoItem(devolucaoItems);
  }

  @Test
  void deveMapearEmprestimoSemDevolucaoParaTemplateData() {
    // When
    Map<String, Object> result = mapper.toTemplateData(emprestimo);

    // Then
    assertNotNull(result);
    assertEquals("João Silva", result.get("usuarioEmprestimo"));
    assertEquals("Maria Santos", result.get("usuarioResponsavel"));
    assertEquals("15/10/2025", result.get("dtEmprestimo"));
    assertEquals("20/10/2025", result.get("dtPrazoDevolucao"));
    assertNull(result.get("dtDevolucao"));
    assertEquals(emprestimoItems, result.get("emprestimoItem"));
    assertEquals(devolucaoItems, result.get("emprestimoDevolucaoItem"));
  }

  @Test
  void deveMapearEmprestimoComDevolucaoParaTemplateData() {
    // Given
    emprestimo.setDataDevolucao(LocalDate.of(2025, 10, 18));

    // When
    Map<String, Object> result = mapper.toTemplateData(emprestimo);

    // Then
    assertNotNull(result);
    assertEquals("18/10/2025", result.get("dtDevolucao"));
  }

  @Test
  void deveIncluirTodasChavesNecessariasNoTemplate() {
    // When
    Map<String, Object> result = mapper.toTemplateData(emprestimo);

    // Then
    assertTrue(result.containsKey("usuarioEmprestimo"));
    assertTrue(result.containsKey("usuarioResponsavel"));
    assertTrue(result.containsKey("dtEmprestimo"));
    assertTrue(result.containsKey("dtPrazoDevolucao"));
    assertTrue(result.containsKey("dtDevolucao"));
    assertTrue(result.containsKey("emprestimoItem"));
    assertTrue(result.containsKey("emprestimoDevolucaoItem"));
  }

  @Test
  void deveFormatarDatasNoFormatoBrasileiro() {
    // Given
    emprestimo.setDataEmprestimo(LocalDate.of(2025, 1, 5)); // Mês e dia com 1 dígito
    emprestimo.setPrazoDevolucao(LocalDate.of(2025, 12, 25)); // Mês e dia com 2 dígitos

    // When
    Map<String, Object> result = mapper.toTemplateData(emprestimo);

    // Then
    assertEquals("05/01/2025", result.get("dtEmprestimo"));
    assertEquals("25/12/2025", result.get("dtPrazoDevolucao"));
  }
}
