package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Pais;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PaisRepository extends JpaRepository<Pais, Long>, JpaSpecificationExecutor<Pais> {

  List<Pais> findByNomeLikeIgnoreCase(String query);
}
