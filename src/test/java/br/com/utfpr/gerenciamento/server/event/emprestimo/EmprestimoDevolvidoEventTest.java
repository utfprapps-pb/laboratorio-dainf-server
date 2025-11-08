package br.com.utfpr.gerenciamento.server.event.emprestimo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EmprestimoDevolvidoEventTest {
  @Test
  void testConstructorAndGetters() {
    EmprestimoDevolvidoEvent event = new EmprestimoDevolvidoEvent(this, 11L, "user@email.com");
    assertEquals(11L, event.getEmprestimoId());
    assertEquals("user@email.com", event.getRecipient());
    assertEquals("Confirmação de Devolução do Empréstimo", event.getSubject());
    assertEquals("templateDevolucaoEmprestimo.html", event.getTemplateName());
  }
}
