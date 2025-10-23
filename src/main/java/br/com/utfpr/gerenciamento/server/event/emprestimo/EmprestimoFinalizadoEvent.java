package br.com.utfpr.gerenciamento.server.event.emprestimo;

import br.com.utfpr.gerenciamento.server.event.email.EmailEvent;
import lombok.Getter;

/**
 * Evento publicado quando um empréstimo é finalizado (itens baixados, empréstimo salvo).
 *
 * <p>Dispara envio de email de confirmação com template apropriado:
 *
 * <ul>
 *   <li>templateConfirmacaoEmprestimo - se houver itens de devolução (consumíveis)
 *   <li>templateConfirmacaoFinalizacaoEmprestimo - se não houver itens de devolução
 * </ul>
 *
 * @author Rodrigo Izidoro
 * @since 2025-10-22
 */
@Getter
public class EmprestimoFinalizadoEvent extends EmailEvent {

  private final Long emprestimoId;
  private final boolean temItensDevolucao;

  /**
   * Cria evento de empréstimo finalizado.
   *
   * @param source Service que publicou o evento
   * @param emprestimoId ID do empréstimo finalizado
   * @param recipient Email do usuário que fez o empréstimo
   * @param temItensDevolucao Se true, usa template com itens de devolução
   */
  public EmprestimoFinalizadoEvent(
      Object source, Long emprestimoId, String recipient, boolean temItensDevolucao) {
    super(
        source,
        recipient,
        "Confirmação de Empréstimo",
        temItensDevolucao
            ? "templateConfirmacaoEmprestimo"
            : "templateConfirmacaoFinalizacaoEmprestimo");
    this.emprestimoId = emprestimoId;
    this.temItensDevolucao = temItensDevolucao;
  }
}
