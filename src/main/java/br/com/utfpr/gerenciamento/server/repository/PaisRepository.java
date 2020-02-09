package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Pais;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaisRepository extends JpaRepository<Pais, Long> {

    List<Pais> findByNomeLikeIgnoreCase (String query);
}
