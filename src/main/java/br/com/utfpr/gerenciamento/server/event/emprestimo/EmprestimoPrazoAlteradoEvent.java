package br.com.utfpr.gerenciamento.server.event.emprestimo;

import br.com.utfpr.gerenciamento.server.event.email.EmailEvent;
import lombok.Getter;

/**
 * Evento publicado quando o prazo de devolução de um empréstimo é alterado.
 *
 * <p>Dispara envio de email notificando o usuário sobre a nova data de devolução.
 *
 * @author Rodrigo Izidoro
 * @since 2025-10-22
 */
@Getter
public class EmprestimoPrazoAlteradoEvent extends EmailEvent {

  private final Long emprestimoId;

  /**
   * Cria evento de alteração de prazo.
   *
   * @param source Service que publicou o evento
   * @param emprestimoId ID do empréstimo com prazo alterado
   * @param recipient Email do usuário do empréstimo
   */
  public EmprestimoPrazoAlteradoEvent(Object source, Long emprestimoId, String recipient) {
    super(source, recipient, "Alteração do prazo de devolução", "templateAlteracaoPrazoDevolucao");
    this.emprestimoId = emprestimoId;
  }
}
