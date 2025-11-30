package br.com.utfpr.gerenciamento.server.repository.projection;

import java.time.LocalDate;

/**
 * Projeção JPA para listagem paginada de Solicitações de Compra.
 *
 * <p>Esta interface otimiza o endpoint /solicitacao-compra/page incluindo apenas campos essenciais
 * para exibição em tabelas.
 *
 * <p><b>Campos incluídos:</b>
 *
 * <ul>
 *   <li>id - Identificador único da solicitação
 *   <li>descricao - Descrição da solicitação
 *   <li>dataSolicitacao - Data de criação da solicitação
 *   <li>usuarioNome - Nome do usuário solicitante
 * </ul>
 */
public interface SolicitacaoListProjection {

  /** Identificador único da solicitação. */
  Long getId();

  /** Descrição da solicitação de compra. */
  String getDescricao();

  /** Data de criação da solicitação. */
  LocalDate getDataSolicitacao();

  /** Nome do usuário que fez a solicitação. */
  String getUsuarioNome();
}
