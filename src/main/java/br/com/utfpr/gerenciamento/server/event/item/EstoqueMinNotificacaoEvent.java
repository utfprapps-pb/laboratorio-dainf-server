package br.com.utfpr.gerenciamento.server.event.item;

import br.com.utfpr.gerenciamento.server.event.email.EmailEvent;
import lombok.Getter;

/**
 * Evento publicado quando itens atingem estoque mínimo e necessitam notificação.
 *
 * <p>Dispara envio de email administrativo com relatório PDF anexado contendo a lista de todos os
 * itens que atingiram ou estão abaixo do estoque mínimo configurado.
 *
 * <p><b>Características:</b>
 *
 * <ul>
 *   <li>Email enviado apenas se houver itens abaixo do estoque mínimo
 *   <li>Relatório Jasper (ID 6) gerado automaticamente pelo listener
 *   <li>PDF anexado ao email para facilitar análise
 *   <li>Template: templateNotificacaoEstoqueMinimo
 * </ul>
 *
 * @author Rodrigo Izidoro
 * @since 2025-10-26
 */
@Getter
public class EstoqueMinNotificacaoEvent extends EmailEvent {

  /**
   * Cria evento de notificação de estoque mínimo.
   *
   * <p>Este evento não requer IDs de entidades pois o relatório é gerado dinamicamente pela query
   * {@code countAllByQtdeMinimaIsLessThanSaldo()}.
   *
   * @param source Service que publicou o evento
   */
  public EstoqueMinNotificacaoEvent(Object source) {
    super(
        source,
        "dainf.labs@gmail.com", // Destinatário fixo (email administrativo)
        "Notificação: Itens que atingiram o estoque mínimo",
        "templateNotificacaoEstoqueMinimo");
  }
}
