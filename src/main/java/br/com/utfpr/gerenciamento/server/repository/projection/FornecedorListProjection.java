package br.com.utfpr.gerenciamento.server.repository.projection;

/**
 * Projeção JPA para listagem paginada de Fornecedores.
 *
 * <p>Esta interface otimiza o endpoint /fornecedor/page incluindo apenas campos essenciais para
 * exibição em tabelas.
 *
 * <p><b>Campos incluídos:</b>
 *
 * <ul>
 *   <li>id - Identificador único do fornecedor
 *   <li>razaoSocial - Razão social do fornecedor
 *   <li>nomeFantasia - Nome fantasia do fornecedor
 *   <li>cnpj - CNPJ do fornecedor
 * </ul>
 */
public interface FornecedorListProjection {

  /** Identificador único do fornecedor. */
  Long getId();

  /** Razão social do fornecedor. */
  String getRazaoSocial();

  /** Nome fantasia do fornecedor. */
  String getNomeFantasia();

  /** CNPJ do fornecedor (14 dígitos, sem formatação). */
  String getCnpj();
}
