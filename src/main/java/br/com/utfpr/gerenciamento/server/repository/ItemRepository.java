package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.repository.projection.ItemCompleteWithDisponibilidade;
import br.com.utfpr.gerenciamento.server.repository.projection.ItemListProjection;
import br.com.utfpr.gerenciamento.server.repository.projection.ItemWithQtdeEmprestada;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
      LEFT JOIN EmprestimoItem ei ON ei.item.id = i.id AND ei.emprestimo.dataDevolucao IS NULL
      WHERE i.id = :id
      GROUP BY i.id
      """)
  Optional<ItemWithQtdeEmprestada> findByIdWithQtdeEmprestada(@Param("id") Long id);

  /**
   * Busca itens para autocomplete com dados de disponibilidade calculados.
   *
   * <p><b>Lógica de Disponibilidade:</b>
   *
   * <ul>
   *   <li>Itens PERMANENTES: disponibilidade = saldo - qtdeEmprestada
   *   <li>Itens CONSUMÍVEIS: considerados disponíveis se saldo > 0
   * </ul>
   *
   * @param query Texto para busca por nome (case insensitive)
   * @return Lista de projeções com dados essenciais para autocomplete
   */
  @Query(
      """
      SELECT i.id as id,
             i.nome as nome,
             i.saldo as saldo,
             i.valor as valor,
             i.tipoItem as tipoItem,
             i.grupo as grupo,
             COALESCE(SUM(ei.qtde), 0) as qtdeEmprestada
      FROM Item i
      LEFT JOIN EmprestimoItem ei ON ei.item.id = i.id AND ei.emprestimo.dataDevolucao IS NULL
      WHERE (:query IS NULL OR :query = '' OR LOWER(i.nome) LIKE LOWER(CONCAT('%', :query, '%')))
      GROUP BY i.id, i.nome, i.saldo, i.valor, i.tipoItem, i.grupo
      ORDER BY i.nome
      """)
  List<ItemCompleteWithDisponibilidade> findCompleteWithDisponibilidade(
      @Param("query") String query);

  /**
   * Busca itens para autocomplete filtrando apenas disponíveis para empréstimo.
   *
   * <p><b>Lógica de Disponibilidade:</b>
   *
   * <ul>
   *   <li>Itens PERMANENTES: disponibilidade = (saldo - qtdeEmprestada) > 0
   *   <li>Itens CONSUMÍVEIS: saldo > 0
   * </ul>
   *
   * @param query Texto para busca por nome (case insensitive)
   * @return Lista de projeções com apenas itens disponíveis para empréstimo
   */
  @Query(
      """
      SELECT i.id as id,
             i.nome as nome,
             i.saldo as saldo,
             i.valor as valor,
             i.tipoItem as tipoItem,
             i.grupo as grupo,
             COALESCE(SUM(ei.qtde), 0) as qtdeEmprestada
      FROM Item i
      LEFT JOIN EmprestimoItem ei ON ei.item.id = i.id AND ei.emprestimo.dataDevolucao IS NULL
      WHERE (:query IS NULL OR :query = '' OR LOWER(i.nome) LIKE LOWER(CONCAT('%', :query, '%')))
      AND (
        (i.tipoItem = 'C' AND i.saldo > 0)
        OR
        i.tipoItem = 'P'
      )
      GROUP BY i.id, i.nome, i.saldo, i.valor, i.tipoItem, i.grupo
      HAVING (
        (i.tipoItem = 'C' AND i.saldo > 0)
        OR
        (i.tipoItem = 'P' AND (i.saldo - COALESCE(SUM(ei.qtde), 0)) > 0)
      )
      ORDER BY i.nome
      """)
  List<ItemCompleteWithDisponibilidade> findCompleteAvailableForLoan(@Param("query") String query);

  /**
   * Busca paginada de itens para listagem com campos otimizados.
   *
   * <p>Inclui primeira imagem via subquery para exibição na tabela.
   *
   * @param pageable paginação e ordenação
   * @return Page de projeções com apenas campos necessários para tabela
   */
  @Query(
      """
      SELECT i.id as id,
             i.nome as nome,
             i.localizacao as localizacao,
             i.saldo as saldo,
             g.id as grupoId,
             g.descricao as grupoDescricao,
             (SELECT ii.nameImage FROM ItemImage ii WHERE ii.item.id = i.id ORDER BY ii.id ASC LIMIT 1) as imagemUrl
      FROM Item i
      LEFT JOIN i.grupo g
      """)
  Page<ItemListProjection> findAllProjected(Pageable pageable);

  /**
   * Busca paginada de itens com filtro de texto.
   *
   * @param filter texto para filtrar por id, nome, localização ou grupo
   * @param pageable paginação e ordenação
   * @return Page de projeções filtradas
   */
  @Query(
      """
      SELECT i.id as id,
             i.nome as nome,
             i.localizacao as localizacao,
             i.saldo as saldo,
             g.id as grupoId,
             g.descricao as grupoDescricao,
             (SELECT ii.nameImage FROM ItemImage ii WHERE ii.item.id = i.id ORDER BY ii.id ASC LIMIT 1) as imagemUrl
      FROM Item i
      LEFT JOIN i.grupo g
      WHERE CAST(i.id AS string) LIKE CONCAT('%', :filter, '%')
         OR LOWER(i.nome) LIKE LOWER(CONCAT('%', :filter, '%'))
         OR LOWER(i.localizacao) LIKE LOWER(CONCAT('%', :filter, '%'))
         OR LOWER(g.descricao) LIKE LOWER(CONCAT('%', :filter, '%'))
      """)
  Page<ItemListProjection> findAllProjectedWithFilter(
      @Param("filter") String filter, Pageable pageable);
}
