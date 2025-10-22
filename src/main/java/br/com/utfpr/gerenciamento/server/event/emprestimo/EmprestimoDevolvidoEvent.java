package br.com.utfpr.gerenciamento.server.event.emprestimo;

import br.com.utfpr.gerenciamento.server.event.email.EmailEvent;
import lombok.Getter;

/**
 * Evento publicado quando itens de um empréstimo são devolvidos.
 *
 * <p>Dispara envio de email de confirmação de devolução ao usuário.
 *
 * @author Rodrigo Izidoro
 * @since 2025-10-22
 */
@Getter
public class EmprestimoDevolvidoEvent extends EmailEvent {

  private final Long emprestimoId;

  /**
   * Cria evento de devolução de empréstimo.
   *
   * @param source Service que publicou o evento
   * @param emprestimoId ID do empréstimo com devolução processada
   * @param recipient Email do usuário que fez a devolução
   */
  public EmprestimoDevolvidoEvent(Object source, Long emprestimoId, String recipient) {
    super(
        source, recipient, "Confirmação de Devolução do Empréstimo", "templateDevolucaoEmprestimo");
    this.emprestimoId = emprestimoId;
  }
}
