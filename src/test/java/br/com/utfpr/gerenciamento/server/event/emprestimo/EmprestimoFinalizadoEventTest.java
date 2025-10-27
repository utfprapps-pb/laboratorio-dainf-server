package br.com.utfpr.gerenciamento.server.event.emprestimo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EmprestimoFinalizadoEventTest {
  @Test
  void testConstructorAndGetters() {
    EmprestimoFinalizadoEvent event =
        new EmprestimoFinalizadoEvent(this, 10L, "user@email.com", true);
    assertEquals(10L, event.getEmprestimoId());
    assertTrue(event.isTemItensDevolucao());
    assertEquals("user@email.com", event.getRecipient());
    assertEquals("Confirmação de Empréstimo", event.getSubject());
    assertEquals("templateConfirmacaoEmprestimo.html", event.getTemplateName());
    EmprestimoFinalizadoEvent event2 =
        new EmprestimoFinalizadoEvent(this, 20L, "user@email.com", false);
    assertEquals("templateConfirmacaoFinalizacaoEmprestimo.html", event2.getTemplateName());
  }
}
