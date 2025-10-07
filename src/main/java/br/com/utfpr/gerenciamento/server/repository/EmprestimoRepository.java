package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardEmprestimoDia;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensEmprestados;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmprestimoRepository
    extends JpaRepository<Emprestimo, Long>, JpaSpecificationExecutor<Emprestimo> {

  @Query(
      "SELECT new br.com.utfpr.gerenciamento.server.model.dashboards.DashboardEmprestimoDia(COUNT(e), e.dataEmprestimo) "
          + "FROM Emprestimo e\n"
          + "WHERE e.dataEmprestimo between :dtIni and :dtFim\n"
          + "GROUP BY e.dataEmprestimo")
  List<DashboardEmprestimoDia> countByDataEmprestimo(
      @Param("dtIni") LocalDate dtIni, @Param("dtFim") LocalDate dtFim);

  List<Emprestimo> findAllByDataEmprestimoBetween(LocalDate dtIni, LocalDate dtFim);

  /**
   * Query otimizada para dashboard com agregação no banco de dados.
   *
   * <p>Substitui o carregamento de todos emprestimos + 4 iterações stream por uma única query com
   * agregação.
   *
   * <p>Melhoria esperada: 60-75% redução no tempo de execução.
   *
   * @param dtIni Data inicial do range
   * @param dtFim Data final do range
   * @return Array com [total, emAtraso, emAndamento, finalizado]
   */
  @Query(
      "SELECT "
          + "COUNT(e), "
          + "SUM(CASE WHEN e.dataDevolucao IS NULL AND e.prazoDevolucao < CURRENT_DATE THEN 1 ELSE 0 END), "
          + "SUM(CASE WHEN e.dataDevolucao IS NULL AND e.prazoDevolucao >= CURRENT_DATE THEN 1 ELSE 0 END), "
          + "SUM(CASE WHEN e.dataDevolucao IS NOT NULL THEN 1 ELSE 0 END) "
          + "FROM Emprestimo e "
          + "WHERE e.dataEmprestimo BETWEEN :dtIni AND :dtFim")
  Object[] countEmprestimosByStatusInRange(
      @Param("dtIni") LocalDate dtIni, @Param("dtFim") LocalDate dtFim);

  @Query(
      "SELECT new br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensEmprestados(SUM(e.qtde), i.nome) "
          + "FROM EmprestimoItem e "
          + "LEFT JOIN Emprestimo em "
          + "ON em.id = e.emprestimo.id "
          + "LEFT JOIN Item i "
          + "ON i.id = e.item .id "
          + "WHERE em.dataEmprestimo between :dtIni and :dtFim "
          + "GROUP BY i.nome")
  List<DashboardItensEmprestados> findItensMaisEmprestados(
      @Param("dtIni") LocalDate dtIni, @Param("dtFim") LocalDate dtFim);

  List<Emprestimo> findAllByUsuarioEmprestimo(Usuario usuario);

  List<Emprestimo> findAllByDataDevolucaoIsNullOrderById();

  List<Emprestimo> findByDataDevolucaoIsNullAndPrazoDevolucaoEquals(LocalDate dt);
}
