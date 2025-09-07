package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.model.Pais;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

<<<<<<< Updated upstream
import java.util.List;

public interface PaisRepository extends JpaRepository<Pais, Long> {
=======
public interface PaisRepository extends JpaRepository<Pais, Long> , JpaSpecificationExecutor<Pais> {
>>>>>>> Stashed changes

    List<Pais> findByNomeLikeIgnoreCase (String query);
}
