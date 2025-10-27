package br.com.utfpr.gerenciamento.server.event.emprestimo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EmprestimoPrazoAlteradoEventTest {
  @Test
  void testConstructorAndGetters() {
    EmprestimoPrazoAlteradoEvent event =
        new EmprestimoPrazoAlteradoEvent(this, 12L, "user@email.com");
    assertEquals(12L, event.getEmprestimoId());
    assertEquals("user@email.com", event.getRecipient());
    assertEquals("Alteração do prazo de devolução", event.getSubject());
    assertEquals("templateAlteracaoPrazoDevolucao.html", event.getTemplateName());
  }
}
