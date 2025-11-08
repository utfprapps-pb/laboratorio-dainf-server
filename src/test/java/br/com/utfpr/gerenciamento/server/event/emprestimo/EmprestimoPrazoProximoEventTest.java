package br.com.utfpr.gerenciamento.server.event.emprestimo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EmprestimoPrazoProximoEventTest {
  @Test
  void testConstructorAndGetters() {
    EmprestimoPrazoProximoEvent event =
        new EmprestimoPrazoProximoEvent(this, 13L, "user@email.com");
    assertEquals(13L, event.getEmprestimoId());
    assertEquals("user@email.com", event.getRecipient());
    assertEquals("Empréstimo próximo da data de devolução", event.getSubject());
    assertEquals("templateProximoPrazoDevolucaoEmprestimo.html", event.getTemplateName());
  }
}
