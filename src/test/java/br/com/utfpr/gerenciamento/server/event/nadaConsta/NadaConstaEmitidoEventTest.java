package br.com.utfpr.gerenciamento.server.event.nadaConsta;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Test;

class NadaConstaEmitidoEventTest {
  @Test
  void testConstructorAndGetters() {
    Map<String, Object> templateData = new HashMap<>();
    NadaConstaEmitidoEvent event = new NadaConstaEmitidoEvent(this, "user@email.com", templateData);
    assertEquals("user@email.com", event.getRecipient());
    assertEquals("Declaração Nada Consta", event.getSubject());
    assertEquals("nada-consta-declaracao.html", event.getTemplateName());
    assertSame(templateData, event.getTemplateData());
  }
}
