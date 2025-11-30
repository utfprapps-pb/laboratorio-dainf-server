package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardEmprestimoCountRange;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardEmprestimoDia;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensEmprestados;
import br.com.utfpr.gerenciamento.server.repository.projection.EmprestimoListProjection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmprestimoRepository
    extends JpaRepository<Emprestimo, Long>, JpaSpecificationExecutor<Emprestimo> {

  @Query(
      """
      SELECT new br.com.utfpr.gerenciamento.server.model.dashboards.DashboardEmprestimoDia(COUNT(e), e.dataEmprestimo)
      FROM Emprestimo e
      WHERE e.dataEmprestimo BETWEEN :dtIni AND :dtFim
      GROUP BY e.dataEmprestimo
      ORDER BY e.dataEmprestimo ASC
      """)
  List<DashboardEmprestimoDia> countByDataEmprestimo(
      @Param("dtIni") LocalDate dtIni, @Param("dtFim") LocalDate dtFim);

  List<Emprestimo> findAllByDataEmprestimoBetween(LocalDate dtIni, LocalDate dtFim);

  /**
   * Query otimizada para dashboard com agregação no banco de dados.
   *
   * <p>Substitui o carregamento de todos emprestimos + 4 iterações stream por uma única query com
   * agregação.
   *
   * @param dtIni Data inicial do range
   * @param dtFim Data final do range
   * @return Objeto com totalizadores (total, emAndamento, emAtraso, finalizado)
   */
  @Query(
      """
              SELECT new br.com.utfpr.gerenciamento.server.model.dashboards.DashboardEmprestimoCountRange(
                  COUNT(e),
                  COALESCE(SUM(CASE WHEN e.dataDevolucao IS NULL AND e.prazoDevolucao >= CURRENT_DATE THEN 1L ELSE 0L END), 0L),
                  COALESCE(SUM(CASE WHEN e.dataDevolucao IS NULL AND e.prazoDevolucao < CURRENT_DATE THEN 1L ELSE 0L END), 0L),
                  COALESCE(SUM(CASE WHEN e.dataDevolucao IS NOT NULL THEN 1L ELSE 0L END), 0L))
              FROM Emprestimo e
              WHERE e.dataEmprestimo BETWEEN :dtIni AND :dtFim
              """)
  DashboardEmprestimoCountRange countEmprestimosByStatusInRange(
      @Param("dtIni") LocalDate dtIni, @Param("dtFim") LocalDate dtFim);

  @Query(
      """
      SELECT new br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensEmprestados(SUM(e.qtde), i.nome)
      FROM EmprestimoItem e
      LEFT JOIN Emprestimo em ON em.id = e.emprestimo.id
      LEFT JOIN Item i ON i.id = e.item.id
      WHERE em.dataEmprestimo BETWEEN :dtIni AND :dtFim
      GROUP BY i.nome
      """)
  List<DashboardItensEmprestados> findItensMaisEmprestados(
      @Param("dtIni") LocalDate dtIni, @Param("dtFim") LocalDate dtFim);

  List<Emprestimo> findAllByUsuarioEmprestimo(Usuario usuario);

  List<Emprestimo> findAllByDataDevolucaoIsNullOrderById();

  List<Emprestimo> findByDataDevolucaoIsNullAndPrazoDevolucaoEquals(LocalDate dt);

  List<Emprestimo> findAllByUsuarioEmprestimoAndDataDevolucaoIsNull(Usuario usuarioEmprestimo);

  /**
   * Busca Emprestimo por ID com eager loading de todas as relações necessárias para email.
   *
   * <p>Elimina N+1 queries ao carregar todas as associações em uma única query com JOIN FETCH.
   *
   * <p>Performance: 5 queries → 1 query (redução de 80%)
   *
   * @param id ID do empréstimo
   * @return Optional com Emprestimo e todas as relações carregadas, ou empty se não encontrado
   */
  @EntityGraph(
      attributePaths = {
        "usuarioEmprestimo",
        "usuarioResponsavel",
        "emprestimoItem",
        "emprestimoItem.item",
        "emprestimoDevolucaoItem",
        "emprestimoDevolucaoItem.item"
      })
  @Query("SELECT e FROM Emprestimo e WHERE e.id = :id")
  Optional<Emprestimo> findEmprestimoByIdWithRelations(@Param("id") Long id);

  /**
   * Busca paginada de empréstimos para listagem com campos otimizados.
   *
   * @param pageable paginação e ordenação
   * @return Page de projeções com apenas campos necessários para tabela
   */
  @Query(
      """
      SELECT e.id as id,
             e.dataEmprestimo as dataEmprestimo,
             e.prazoDevolucao as prazoDevolucao,
             e.dataDevolucao as dataDevolucao,
             ue.nome as usuarioEmprestimoNome
      FROM Emprestimo e
      LEFT JOIN e.usuarioEmprestimo ue
      """)
  Page<EmprestimoListProjection> findAllProjected(Pageable pageable);

  /**
   * Busca paginada de empréstimos com filtro de texto.
   *
   * @param filter texto para filtrar por id ou nome do usuário
   * @param pageable paginação e ordenação
   * @return Page de projeções filtradas
   */
  @Query(
      """
      SELECT e.id as id,
             e.dataEmprestimo as dataEmprestimo,
             e.prazoDevolucao as prazoDevolucao,
             e.dataDevolucao as dataDevolucao,
             ue.nome as usuarioEmprestimoNome
      FROM Emprestimo e
      LEFT JOIN e.usuarioEmprestimo ue
      WHERE CAST(e.id AS string) LIKE CONCAT('%', :filter, '%')
         OR LOWER(ue.nome) LIKE LOWER(CONCAT('%', :filter, '%'))
      """)
  Page<EmprestimoListProjection> findAllProjectedWithFilter(
      @Param("filter") String filter, Pageable pageable);

  /**
   * Busca paginada de empréstimos por usuário específico (para role-based filtering).
   *
   * @param username username do usuário
   * @param pageable paginação e ordenação
   * @return Page de projeções do usuário
   */
  @Query(
      """
      SELECT e.id as id,
             e.dataEmprestimo as dataEmprestimo,
             e.prazoDevolucao as prazoDevolucao,
             e.dataDevolucao as dataDevolucao,
             ue.nome as usuarioEmprestimoNome
      FROM Emprestimo e
      LEFT JOIN e.usuarioEmprestimo ue
      WHERE ue.username = :username
      """)
  Page<EmprestimoListProjection> findAllProjectedByUsername(
      @Param("username") String username, Pageable pageable);

  /**
   * Busca paginada de empréstimos por usuário específico com filtro de texto.
   *
   * @param username username do usuário
   * @param filter texto para filtrar
   * @param pageable paginação e ordenação
   * @return Page de projeções filtradas do usuário
   */
  @Query(
      """
      SELECT e.id as id,
             e.dataEmprestimo as dataEmprestimo,
             e.prazoDevolucao as prazoDevolucao,
             e.dataDevolucao as dataDevolucao,
             ue.nome as usuarioEmprestimoNome
      FROM Emprestimo e
      LEFT JOIN e.usuarioEmprestimo ue
      WHERE ue.username = :username
        AND (CAST(e.id AS string) LIKE CONCAT('%', :filter, '%')
             OR LOWER(ue.nome) LIKE LOWER(CONCAT('%', :filter, '%')))
      """)
  Page<EmprestimoListProjection> findAllProjectedByUsernameWithFilter(
      @Param("username") String username, @Param("filter") String filter, Pageable pageable);
}
