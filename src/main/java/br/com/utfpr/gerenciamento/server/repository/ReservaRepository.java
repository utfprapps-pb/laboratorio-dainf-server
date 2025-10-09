package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Reserva;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import java.util.List;
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
          SELECT *
          FROM RESERVA R
          LEFT JOIN RESERVA_ITEM RI
              ON RI.RESERVA_ID = R.ID
          WHERE RI.ITEM_ID = :IDITEM
          """,
      nativeQuery = true)
  List<Reserva> findReservaByIdItem(@Param("IDITEM") Long id);
}
