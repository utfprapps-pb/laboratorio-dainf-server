package br.com.utfpr.gerenciamento.server.event.nadaConsta;

import br.com.utfpr.gerenciamento.server.event.email.EmailEvent;
import java.util.Map;
import lombok.Getter;

/** Evento publicado quando uma declaração de Nada Consta é emitida. */
@Getter
public class NadaConstaEmitidoEvent extends EmailEvent {
  private final transient Map<String, Object> templateData;
  private final String cc;

  public NadaConstaEmitidoEvent(
      Object source, String recipient, Map<String, Object> templateData, String cc) {
    super(source, recipient, "Declaração Nada Consta", "nada-consta-declaracao.html");
    this.templateData = templateData;
    this.cc = cc;
  }

  // Mantém construtor antigo para compatibilidade
  public NadaConstaEmitidoEvent(Object source, String recipient, Map<String, Object> templateData) {
    this(source, recipient, templateData, null);
  }
}
