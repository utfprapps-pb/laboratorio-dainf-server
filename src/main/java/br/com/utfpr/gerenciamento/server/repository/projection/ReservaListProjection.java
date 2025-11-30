package br.com.utfpr.gerenciamento.server.repository.projection;

import java.time.LocalDate;

/**
 * Projeção JPA para listagem paginada de Reservas.
 *
 * <p>Esta interface otimiza o endpoint /reserva/page incluindo apenas campos essenciais para
 * exibição em tabelas.
 *
 * <p><b>Campos incluídos:</b>
 *
 * <ul>
 *   <li>id - Identificador único da reserva
 *   <li>descricao - Descrição/motivo da reserva
 *   <li>dataReserva - Data de criação da reserva
 *   <li>dataRetirada - Data prevista para retirada
 *   <li>usuarioNome - Nome do usuário que fez a reserva
 * </ul>
 */
public interface ReservaListProjection {

  /** Identificador único da reserva. */
  Long getId();

  /** Descrição/motivo da reserva. */
  String getDescricao();

  /** Data de criação da reserva. */
  LocalDate getDataReserva();

  /** Data prevista para retirada dos itens. */
  LocalDate getDataRetirada();

  /** Nome do usuário que fez a reserva. */
  String getUsuarioNome();
}
