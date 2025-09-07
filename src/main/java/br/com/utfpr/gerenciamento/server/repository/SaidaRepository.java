package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.model.Saida;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensSaidas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

<<<<<<< Updated upstream
<<<<<<< Updated upstream
import java.time.LocalDate;
import java.util.List;

public interface SaidaRepository extends JpaRepository<Saida, Long> {
=======
public interface SaidaRepository extends JpaRepository<Saida, Long>, JpaSpecificationExecutor<Saida> {
>>>>>>> Stashed changes
=======
public interface SaidaRepository extends JpaRepository<Saida, Long>, JpaSpecificationExecutor<Saida> {
>>>>>>> Stashed changes

    @Query("SELECT new br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensSaidas(SUM(si.qtde), i.nome) \n" +
            "FROM SaidaItem si " +
            "LEFT JOIN Saida s " +
            "ON s.id = si.saida.id " +
            "LEFT JOIN Item i " +
            "ON i.id = si.item.id " +
            "WHERE s.dataSaida between :dtIni and :dtFim " +
            "GROUP BY i.nome")
    List<DashboardItensSaidas> findItensMaisSaidas(@Param("dtIni") LocalDate dtIni,
                                                   @Param("dtFim") LocalDate dtFim);

    Saida findByIdEmprestimo(Long idEmprestimo);
}
