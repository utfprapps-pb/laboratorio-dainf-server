package br.com.utfpr.gerenciamento.server.repository.projection;

import java.time.LocalDate;

/**
 * Projeção JPA para listagem paginada de Empréstimos.
 *
 * <p>Esta interface otimiza o endpoint /emprestimo/page incluindo apenas campos essenciais para
 * exibição em tabelas, evitando carregar entidades completas e relacionamentos desnecessários.
 *
 * <p><b>Campos incluídos:</b>
 *
 * <ul>
 *   <li>id - Identificador único do empréstimo
 *   <li>dataEmprestimo - Data de realização do empréstimo
 *   <li>prazoDevolucao - Data limite para devolução
 *   <li>dataDevolucao - Data efetiva de devolução (null se em andamento)
 *   <li>usuarioEmprestimoNome - Nome do usuário que realizou o empréstimo
 * </ul>
 */
public interface EmprestimoListProjection {

  /** Identificador único do empréstimo. */
  Long getId();

  /** Data de realização do empréstimo. */
  LocalDate getDataEmprestimo();

  /** Data limite para devolução dos itens. */
  LocalDate getPrazoDevolucao();

  /** Data efetiva de devolução. Null indica empréstimo em andamento. */
  LocalDate getDataDevolucao();

  /** Nome do usuário que realizou o empréstimo (aluno/professor). */
  String getUsuarioEmprestimoNome();
}
