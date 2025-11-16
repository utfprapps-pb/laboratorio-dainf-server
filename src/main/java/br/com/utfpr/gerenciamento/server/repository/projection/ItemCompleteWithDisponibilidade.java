package br.com.utfpr.gerenciamento.server.repository.projection;

import br.com.utfpr.gerenciamento.server.enumeration.TipoItem;
import br.com.utfpr.gerenciamento.server.model.Grupo;
import java.math.BigDecimal;

/**
 * Projeção JPA para autocomplete de Item com dados de disponibilidade calculados.
 *
 * <p>Esta interface otimiza o endpoint /item/complete incluindo apenas campos essenciais para
 * autocomplete, evitando carregar entidades completas e calculando disponibilidade via SQL
 * agregado.
 *
 * <p><b>Campos incluídos:</b>
 *
 * <ul>
 *   <li>id - Identificador único do item
 *   <li>nome - Nome para exibição no autocomplete
 *   <li>saldo - Saldo atual (para itens consumíveis)
 *   <li>tipoItem - Tipo do item (P = Permanente, C = Consumível)
 *   <li>qtdeEmprestada - Quantidade atualmente emprestada (calculada via SQL)
 * </ul>
 *
 * <p><b>Regras de Negócio Aplicadas:</b>
 *
 * <ul>
 *   <li>RN-001: itens PERMANENTES calculam disponibilidade = saldo - qtdeEmprestada
 *   <li>RN-002: itens CONSUMÍVEIS não têm disponibilidade calculada (sempre null)
 *   <li>RN-003: disponibilidade nunca é negativa (limitada a zero)
 * </ul>
 *
 * <p>Usado por {@link
 * br.com.utfpr.gerenciamento.server.repository.ItemRepository#findCompleteWithDisponibilidade(String,
 * boolean)} para otimizar performance do endpoint autocomplete.
 */
public interface ItemCompleteWithDisponibilidade {

  /**
   * Identificador único do item.
   *
   * @return ID do item
   */
  Long getId();

  /**
   * Nome do item para exibição no autocomplete.
   *
   * @return nome do item
   */
  String getNome();

  /**
   * Saldo atual do item em estoque.
   *
   * <p>Relevante principalmente para itens consumíveis onde indica quantidade disponível.
   *
   * @return saldo atual (pode ser null para itens permanentes sem controle de saldo)
   */
  BigDecimal getSaldo();

  /**
   * Tipo do item (Permanente ou Consumível).
   *
   * @return tipo do item
   */
  TipoItem getTipoItem();

  /**
   * Quantidade total emprestada atualmente ativa (data_devolucao IS NULL).
   *
   * <p>Calculado via agregação SQL: {@code COALESCE(SUM(ei.qtde), 0)}
   *
   * @return quantidade emprestada (nunca null devido ao COALESCE)
   */
  BigDecimal getQtdeEmprestada();

  /**
   * Valor unitário do item.
   *
   * @return valor do item
   */
  BigDecimal getValor();

  /**
   * Grupo do item.
   *
   * @return grupo do item
   */
  Grupo getGrupo();
}
