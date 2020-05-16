package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Reserva;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findAllByUsuario(Usuario usuario);
}
