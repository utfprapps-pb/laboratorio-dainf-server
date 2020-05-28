package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardEmprestimoDia;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensEmprestados;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {

    @Query("SELECT new br.com.utfpr.gerenciamento.server.model.dashboards.DashboardEmprestimoDia(COUNT(e), e.dataEmprestimo) \n" +
            "FROM Emprestimo e\n" +
            "WHERE e.dataEmprestimo between :dtIni and :dtFim\n" +
            "GROUP BY e.dataEmprestimo")
    List<DashboardEmprestimoDia> countByDataEmprestimo(@Param("dtIni") LocalDate dtIni,
                                                       @Param("dtFim") LocalDate dtFim);

    List<Emprestimo> findAllByDataEmprestimoBetween(LocalDate dtIni, LocalDate dtFim);

    @Query("SELECT new br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensEmprestados(SUM(e.qtde), i.nome) \n" +
            "FROM EmprestimoItem e " +
            "LEFT JOIN Emprestimo em " +
            "ON em.id = e.emprestimo " +
            "LEFT JOIN Item i " +
            "ON i.id = e.item " +
            "WHERE em.dataEmprestimo between :dtIni and :dtFim " +
            "GROUP BY i.nome")
    List<DashboardItensEmprestados> findItensMaisEmprestados(@Param("dtIni") LocalDate dtIni,
                                                             @Param("dtFim") LocalDate dtFim);

    List<Emprestimo> findAllByUsuarioEmprestimo(Usuario usuario);

    List<Emprestimo> findAllByDataDevolucaoIsNullOrderById();

    List<Emprestimo> findByDataDevolucaoIsNullAndPrazoDevolucaoEquals(LocalDate dt);
}
