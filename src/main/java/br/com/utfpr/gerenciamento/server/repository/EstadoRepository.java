package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Estado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EstadoRepository extends JpaRepository<Estado, Long> {

    List<Estado> findByNomeLikeIgnoreCase(String query);
}
