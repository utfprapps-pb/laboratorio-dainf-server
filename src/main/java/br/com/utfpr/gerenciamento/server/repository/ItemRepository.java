package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.model.ItemImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
<<<<<<< Updated upstream
=======
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
>>>>>>> Stashed changes

public interface ItemRepository extends JpaRepository<Item, Long> , JpaSpecificationExecutor<Item>{

    List<Item> findAllBySaldoIsGreaterThanOrderByNome(BigDecimal saldo);

    List<Item> findByNomeLikeIgnoreCaseOrderByNome(String query);

    List<Item> findByNomeLikeIgnoreCaseAndSaldoIsGreaterThanOrderByNome(String query, BigDecimal saldo);

    List<Item> findByGrupoIdOrderByNome(Long idGrupo);

    @Query("SELECT COUNT(i.id) FROM Item i WHERE i.saldo <= i.qtdeMinima")
    long countAllByQtdeMinimaIsLessThanSaldo();


    List<Item> findAllByOrderByNome();
}
