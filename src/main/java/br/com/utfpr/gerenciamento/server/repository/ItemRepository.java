package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.repository.projection.ItemWithQtdeEmprestada;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {

  List<Item> findAllBySaldoIsGreaterThanOrderByNome(BigDecimal saldo);

  List<Item> findByNomeLikeIgnoreCaseOrderByNome(String query);

  List<Item> findByNomeLikeIgnoreCaseAndSaldoIsGreaterThanOrderByNome(
      String query, BigDecimal saldo);

  List<Item> findByGrupoIdOrderByNome(Long idGrupo);

  @Query("SELECT COUNT(i.id) FROM Item i WHERE i.saldo <= i.qtdeMinima")
  long countAllByQtdeMinimaIsLessThanSaldo();

  List<Item> findAllByOrderByNome();

  /**
   * Busca Item com quantidade emprestada calculada via agregação SQL.
   *
   * <p><b>Spring Data JPA Projection:</b> Retorna interface projection automaticamente mapeada
   * pelos aliases da query (item → getItem(), qtdeEmprestada → getQtdeEmprestada()).
   *
   * <p><b>Business Rules:</b>
   *
   * <ul>
   *   <li>RN-004: Considera apenas empréstimos ativos (data_devolucao IS NULL)
   *   <li>Otimização: Evita N+1 queries usando LEFT JOIN + GROUP BY
   *   <li>Retorna Optional.empty() se item não existir
   * </ul>
   *
   * @param id ID do item a ser buscado
   * @return Optional contendo projection type-safe, ou empty se não encontrado
   * @see ItemWithQtdeEmprestada
   */
  @Query(
      """
      SELECT i as item, COALESCE(SUM(ei.qtde), 0) as qtdeEmprestada
      FROM Item i
      LEFT JOIN EmprestimoItem ei ON ei.item.id = i.id
      LEFT JOIN Emprestimo e ON ei.emprestimo.id = e.id AND e.dataDevolucao IS NULL
      WHERE i.id = :id
      GROUP BY i.id
      """)
  Optional<ItemWithQtdeEmprestada> findByIdWithQtdeEmprestada(@Param("id") Long id);
}
