package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findAllBySaldoIsGreaterThan(BigDecimal saldo);

    List<Item> findByNomeLikeIgnoreCase(String query);

    List<Item> findByNomeLikeIgnoreCaseAndSaldoIsGreaterThan(String query, BigDecimal saldo);

    List<Item> findByGrupoId(Long idGrupo);
}
