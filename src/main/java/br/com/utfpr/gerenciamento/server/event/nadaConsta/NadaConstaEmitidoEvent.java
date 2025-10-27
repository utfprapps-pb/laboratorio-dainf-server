package br.com.utfpr.gerenciamento.server.event.nadaConsta;

import br.com.utfpr.gerenciamento.server.event.email.EmailEvent;
import java.util.Map;
import lombok.Getter;

/** Evento publicado quando uma declaração de Nada Consta é emitida. */
@Getter
public class NadaConstaEmitidoEvent extends EmailEvent {
  private final Map<String, Object> templateData;

  public NadaConstaEmitidoEvent(Object source, String recipient, Map<String, Object> templateData) {
    super(source, recipient, "Declaração Nada Consta", "nada-consta-declaracao.html");
    this.templateData = templateData;
  }
}
