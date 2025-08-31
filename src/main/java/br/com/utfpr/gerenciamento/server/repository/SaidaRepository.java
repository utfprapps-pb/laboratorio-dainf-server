package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Saida;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensSaidas;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SaidaRepository extends JpaRepository<Saida, Long> {

  @Query(
      "SELECT new br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensSaidas(SUM(si.qtde), i.nome) \n"
          + "FROM SaidaItem si "
          + "LEFT JOIN Saida s "
          + "ON s.id = si.saida.id "
          + "LEFT JOIN Item i "
          + "ON i.id = si.item.id "
          + "WHERE s.dataSaida between :dtIni and :dtFim "
          + "GROUP BY i.nome")
  List<DashboardItensSaidas> findItensMaisSaidas(
      @Param("dtIni") LocalDate dtIni, @Param("dtFim") LocalDate dtFim);

  Saida findByIdEmprestimo(Long idEmprestimo);
}
