package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Saida;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensSaidas;
import br.com.utfpr.gerenciamento.server.repository.projection.SaidaListProjection;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SaidaRepository
    extends JpaRepository<Saida, Long>, JpaSpecificationExecutor<Saida> {

  @Query(
      """
      SELECT new br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensSaidas(SUM(si.qtde), i.nome)
      FROM SaidaItem si
      LEFT JOIN Saida s ON s.id = si.saida.id
      LEFT JOIN Item i ON i.id = si.item.id
      WHERE s.dataSaida BETWEEN :dtIni AND :dtFim
      GROUP BY i.nome
      """)
  List<DashboardItensSaidas> findItensMaisSaidas(
      @Param("dtIni") LocalDate dtIni, @Param("dtFim") LocalDate dtFim);

  Saida findByIdEmprestimo(Long idEmprestimo);

  /**
   * Busca paginada de saídas para listagem com campos otimizados.
   *
   * @param pageable paginação e ordenação
   * @return Page de projeções com apenas campos necessários para tabela
   */
  @Query(
      """
      SELECT s.id as id,
             s.dataSaida as dataSaida,
             s.observacao as observacao,
             u.nome as usuarioResponsavelNome,
             (SELECT COALESCE(SUM(si.qtde), 0) FROM SaidaItem si WHERE si.saida.id = s.id) as qtdeTotal
      FROM Saida s
      LEFT JOIN s.usuarioResponsavel u
      """)
  Page<SaidaListProjection> findAllProjected(Pageable pageable);

  /**
   * Busca paginada de saídas com filtro de texto.
   *
   * @param filter texto para filtrar por id, observação ou usuário responsável
   * @param pageable paginação e ordenação
   * @return Page de projeções filtradas
   */
  @Query(
      """
      SELECT s.id as id,
             s.dataSaida as dataSaida,
             s.observacao as observacao,
             u.nome as usuarioResponsavelNome,
             (SELECT COALESCE(SUM(si.qtde), 0) FROM SaidaItem si WHERE si.saida.id = s.id) as qtdeTotal
      FROM Saida s
      LEFT JOIN s.usuarioResponsavel u
      WHERE CAST(s.id AS string) LIKE CONCAT('%', :filter, '%')
         OR LOWER(s.observacao) LIKE LOWER(CONCAT('%', :filter, '%'))
         OR LOWER(u.nome) LIKE LOWER(CONCAT('%', :filter, '%'))
      """)
  Page<SaidaListProjection> findAllProjectedWithFilter(
      @Param("filter") String filter, Pageable pageable);
}
