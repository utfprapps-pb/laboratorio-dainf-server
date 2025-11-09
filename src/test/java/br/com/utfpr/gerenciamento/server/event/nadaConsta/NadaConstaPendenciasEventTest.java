package br.com.utfpr.gerenciamento.server.event.nadaConsta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NadaConstaPendenciasEventTest {
  @Test
  void testConstructorAndGetters() {
    Map<String, Object> templateData = new HashMap<>();
    NadaConstaPendenciasEvent event =
        new NadaConstaPendenciasEvent(this, "user@email.com", templateData);
    assertEquals("user@email.com", event.getRecipient());
    assertEquals("Pendências de Empréstimos", event.getSubject());
    assertEquals("pendencias-emprestimos.html", event.getTemplateName());
    assertSame(templateData, event.getTemplateData());
  }
}
