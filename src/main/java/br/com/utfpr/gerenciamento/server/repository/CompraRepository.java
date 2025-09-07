package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Compra;
import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensAdquiridos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CompraRepository extends JpaRepository<Compra, Long>, JpaSpecificationExecutor<Compra> {

    @Query("SELECT new br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensAdquiridos(SUM(ci.qtde), i.nome) " +
            "FROM CompraItem ci " +
            "LEFT JOIN Compra c " +
            "ON c.id = ci.compra.id " +
            "LEFT JOIN Item i " +
            "ON i.id = ci.item.id " +
            "WHERE c.dataCompra between :dtIni and :dtFim " +
            "GROUP BY i.nome")
    List<DashboardItensAdquiridos> findItensMaisAdquiridos(@Param("dtIni") LocalDate dtIni,
                                                           @Param("dtFim") LocalDate dtFim);
}
