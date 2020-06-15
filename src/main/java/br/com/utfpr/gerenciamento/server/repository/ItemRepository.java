package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.model.ItemImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findAllBySaldoIsGreaterThan(BigDecimal saldo);

    List<Item> findByNomeLikeIgnoreCase(String query);

    List<Item> findByNomeLikeIgnoreCaseAndSaldoIsGreaterThan(String query, BigDecimal saldo);

    List<Item> findByGrupoId(Long idGrupo);

    @Query("SELECT COUNT(i.id) FROM Item i WHERE i.saldo <= i.qtdeMinima")
    long countAllByQtdeMinimaIsLessThanSaldo();
}
