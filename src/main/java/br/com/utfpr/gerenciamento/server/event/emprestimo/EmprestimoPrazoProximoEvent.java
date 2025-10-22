package br.com.utfpr.gerenciamento.server.event.emprestimo;

import br.com.utfpr.gerenciamento.server.event.email.EmailEvent;
import lombok.Getter;

/**
 * Evento publicado quando o prazo de devolução de um empréstimo está próximo (3 dias).
 *
 * <p>Dispara envio de email notificando o usuário sobre a proximidade do prazo.
 *
 * @author Rodrigo Izidoro
 * @since 2025-10-22
 */
@Getter
public class EmprestimoPrazoProximoEvent extends EmailEvent {

  private final Long emprestimoId;

  /**
   * Cria evento de prazo próximo.
   *
   * @param source Service que publicou o evento
   * @param emprestimoId ID do empréstimo com prazo próximo
   * @param recipient Email do usuário do empréstimo
   */
  public EmprestimoPrazoProximoEvent(Object source, Long emprestimoId, String recipient) {
    super(
        source,
        recipient,
        "Empréstimo próximo da data de devolução",
        "templateProximoPrazoDevolucaoEmprestimo");
    this.emprestimoId = emprestimoId;
  }
}
