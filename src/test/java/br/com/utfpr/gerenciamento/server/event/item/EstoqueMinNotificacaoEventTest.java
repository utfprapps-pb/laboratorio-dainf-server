package br.com.utfpr.gerenciamento.server.event.item;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EstoqueMinNotificacaoEventTest {
  @Test
  void testConstructorAndGetters() {
    EstoqueMinNotificacaoEvent event = new EstoqueMinNotificacaoEvent(this, "admin@email.com");
    assertEquals("admin@email.com", event.getRecipient());
    assertEquals("Notificação: Itens que atingiram o estoque mínimo", event.getSubject());
    assertEquals("templateNotificacaoEstoqueMinimo.html", event.getTemplateName());
  }
}
