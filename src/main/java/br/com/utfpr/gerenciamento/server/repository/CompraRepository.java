package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Compra;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensAdquiridos;
import br.com.utfpr.gerenciamento.server.repository.projection.CompraListProjection;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompraRepository
    extends JpaRepository<Compra, Long>, JpaSpecificationExecutor<Compra> {

  @Query(
      "SELECT new br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensAdquiridos(SUM(ci.qtde), i.nome) "
          + "FROM CompraItem ci "
          + "LEFT JOIN Compra c "
          + "ON c.id = ci.compra.id "
          + "LEFT JOIN Item i "
          + "ON i.id = ci.item.id "
          + "WHERE c.dataCompra between :dtIni and :dtFim "
          + "GROUP BY i.nome")
  List<DashboardItensAdquiridos> findItensMaisAdquiridos(
      @Param("dtIni") LocalDate dtIni, @Param("dtFim") LocalDate dtFim);

  /**
   * Busca paginada de compras para listagem com campos otimizados.
   *
   * @param pageable paginação e ordenação
   * @return Page de projeções com apenas campos necessários para tabela
   */
  @Query(
      """
      SELECT c.id as id,
             c.dataCompra as dataCompra,
             f.razaoSocial as fornecedorRazaoSocial,
             f.nomeFantasia as fornecedorNomeFantasia
      FROM Compra c
      LEFT JOIN c.fornecedor f
      """)
  Page<CompraListProjection> findAllProjected(Pageable pageable);

  /**
   * Busca paginada de compras com filtro de texto.
   *
   * @param filter texto para filtrar por id ou fornecedor
   * @param pageable paginação e ordenação
   * @return Page de projeções filtradas
   */
  @Query(
      """
      SELECT c.id as id,
             c.dataCompra as dataCompra,
             f.razaoSocial as fornecedorRazaoSocial,
             f.nomeFantasia as fornecedorNomeFantasia
      FROM Compra c
      LEFT JOIN c.fornecedor f
      WHERE CAST(c.id AS string) LIKE CONCAT('%', :filter, '%')
         OR LOWER(f.razaoSocial) LIKE LOWER(CONCAT('%', :filter, '%'))
         OR LOWER(f.nomeFantasia) LIKE LOWER(CONCAT('%', :filter, '%'))
      """)
  Page<CompraListProjection> findAllProjectedWithFilter(
      @Param("filter") String filter, Pageable pageable);
}
