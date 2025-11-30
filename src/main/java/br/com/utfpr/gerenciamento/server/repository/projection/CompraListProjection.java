package br.com.utfpr.gerenciamento.server.repository.projection;

import java.time.LocalDate;

/**
 * Projeção JPA para listagem paginada de Compras.
 *
 * <p>Esta interface otimiza o endpoint /compra/page incluindo apenas campos essenciais para
 * exibição em tabelas.
 *
 * <p><b>Campos incluídos:</b>
 *
 * <ul>
 *   <li>id - Identificador único da compra
 *   <li>dataCompra - Data de realização da compra
 *   <li>fornecedorRazaoSocial - Razão social do fornecedor
 *   <li>fornecedorNomeFantasia - Nome fantasia do fornecedor
 * </ul>
 */
public interface CompraListProjection {

  /** Identificador único da compra. */
  Long getId();

  /** Data de realização da compra. */
  LocalDate getDataCompra();

  /** Razão social do fornecedor. */
  String getFornecedorRazaoSocial();

  /** Nome fantasia do fornecedor. */
  String getFornecedorNomeFantasia();
}
