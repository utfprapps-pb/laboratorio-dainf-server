package br.com.utfpr.gerenciamento.server.repository.projection;

import java.time.LocalDate;

/**
 * Projeção JPA para listagem paginada de Saídas.
 *
 * <p>Esta interface otimiza o endpoint /saida/page incluindo apenas campos essenciais para exibição
 * em tabelas.
 *
 * <p><b>Campos incluídos:</b>
 *
 * <ul>
 *   <li>id - Identificador único da saída
 *   <li>dataSaida - Data de realização da saída
 *   <li>observacao - Observações sobre a saída
 *   <li>usuarioResponsavelNome - Nome do usuário responsável
 *   <li>qtdeTotal - Quantidade total de itens na saída
 * </ul>
 */
public interface SaidaListProjection {

  /** Identificador único da saída. */
  Long getId();

  /** Data de realização da saída. */
  LocalDate getDataSaida();

  /** Observações sobre a saída. */
  String getObservacao();

  /** Nome do usuário responsável pela saída. */
  String getUsuarioResponsavelNome();

  /** Quantidade total de itens na saída (soma das quantidades de todos os SaidaItem). */
  java.math.BigDecimal getQtdeTotal();
}
