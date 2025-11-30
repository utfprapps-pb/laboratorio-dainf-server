package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Reserva;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.repository.projection.ReservaListProjection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservaRepository
    extends JpaRepository<Reserva, Long>, JpaSpecificationExecutor<Reserva> {

  List<Reserva> findAllByUsuario(Usuario usuario);

  @Query(
      value =
          """
          SELECT R.*
          FROM RESERVA R
          LEFT JOIN RESERVA_ITEM RI
              ON RI.RESERVA_ID = R.ID
          WHERE RI.ITEM_ID = :IDITEM
          """,
      nativeQuery = true)
  List<Reserva> findReservaByIdItem(@Param("IDITEM") Long id);

  /**
   * Busca paginada de reservas para listagem com campos otimizados.
   *
   * @param pageable paginação e ordenação
   * @return Page de projeções com apenas campos necessários para tabela
   */
  @Query(
      """
      SELECT r.id as id,
             r.descricao as descricao,
             r.dataReserva as dataReserva,
             r.dataRetirada as dataRetirada,
             u.nome as usuarioNome
      FROM Reserva r
      LEFT JOIN r.usuario u
      """)
  Page<ReservaListProjection> findAllProjected(Pageable pageable);

  /**
   * Busca paginada de reservas com filtro de texto.
   *
   * @param filter texto para filtrar por id, descrição ou data
   * @param pageable paginação e ordenação
   * @return Page de projeções filtradas
   */
  @Query(
      """
      SELECT r.id as id,
             r.descricao as descricao,
             r.dataReserva as dataReserva,
             r.dataRetirada as dataRetirada,
             u.nome as usuarioNome
      FROM Reserva r
      LEFT JOIN r.usuario u
      WHERE CAST(r.id AS string) LIKE CONCAT('%', :filter, '%')
         OR LOWER(r.descricao) LIKE LOWER(CONCAT('%', :filter, '%'))
      """)
  Page<ReservaListProjection> findAllProjectedWithFilter(
      @Param("filter") String filter, Pageable pageable);

  /**
   * Busca paginada de reservas por usuário específico (para role-based filtering).
   *
   * @param username username do usuário
   * @param pageable paginação e ordenação
   * @return Page de projeções do usuário
   */
  @Query(
      """
      SELECT r.id as id,
             r.descricao as descricao,
             r.dataReserva as dataReserva,
             r.dataRetirada as dataRetirada,
             u.nome as usuarioNome
      FROM Reserva r
      LEFT JOIN r.usuario u
      WHERE u.username = :username
      """)
  Page<ReservaListProjection> findAllProjectedByUsername(
      @Param("username") String username, Pageable pageable);

  /**
   * Busca paginada de reservas por usuário específico com filtro de texto.
   *
   * @param username username do usuário
   * @param filter texto para filtrar
   * @param pageable paginação e ordenação
   * @return Page de projeções filtradas do usuário
   */
  @Query(
      """
      SELECT r.id as id,
             r.descricao as descricao,
             r.dataReserva as dataReserva,
             r.dataRetirada as dataRetirada,
             u.nome as usuarioNome
      FROM Reserva r
      LEFT JOIN r.usuario u
      WHERE u.username = :username
        AND (CAST(r.id AS string) LIKE CONCAT('%', :filter, '%')
             OR LOWER(r.descricao) LIKE LOWER(CONCAT('%', :filter, '%')))
      """)
  Page<ReservaListProjection> findAllProjectedByUsernameWithFilter(
      @Param("username") String username, @Param("filter") String filter, Pageable pageable);
}
