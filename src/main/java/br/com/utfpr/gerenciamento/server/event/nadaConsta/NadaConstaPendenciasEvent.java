package br.com.utfpr.gerenciamento.server.event.nadaConsta;

import br.com.utfpr.gerenciamento.server.event.email.EmailEvent;
import java.util.Map;
import lombok.Getter;

/** Evento publicado quando há pendências de empréstimos ao solicitar Nada Consta. */
@Getter
public class NadaConstaPendenciasEvent extends EmailEvent {
  private final Map<String, Object> templateData;

  public NadaConstaPendenciasEvent(
      Object source, String recipient, Map<String, Object> templateData) {
    super(source, recipient, "Pendências de Empréstimos", "pendencias-emprestimos.html");
    this.templateData = templateData;
  }
}
