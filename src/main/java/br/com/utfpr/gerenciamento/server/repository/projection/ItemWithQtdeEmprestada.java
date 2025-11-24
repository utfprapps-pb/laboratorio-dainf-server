package br.com.utfpr.gerenciamento.server.repository.projection;

import br.com.utfpr.gerenciamento.server.model.Item;
import java.math.BigDecimal;

/**
 * Projeção JPA para retornar Item com quantidade emprestada calculada.
 *
 * <p>Esta interface evita o uso de Object[] inseguro, fornecendo type safety em tempo de compilação
 * e documentação clara dos campos retornados pela query.
 *
 * <p>Usado por {@link
 * br.com.utfpr.gerenciamento.server.repository.ItemRepository#findByIdWithQtdeEmprestada(Long)}
 * para otimizar cálculo de disponibilidade sem N+1 queries.
 */
public interface ItemWithQtdeEmprestada {
  /**
   * Item completo recuperado do banco de dados.
   *
   * @return entidade Item com todos os campos carregados
   */
  Item getItem();

  /**
   * Quantidade total emprestada atualmente ativa (data_devolucao IS NULL).
   *
   * <p>Calculado via agregação SQL: {@code COALESCE(SUM(ei.qtde), 0)}
   *
   * @return quantidade emprestada (nunca null devido ao COALESCE)
   */
  BigDecimal getQtdeEmprestada();
}
