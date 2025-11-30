package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Fornecedor;
import br.com.utfpr.gerenciamento.server.repository.projection.FornecedorListProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FornecedorRepository
    extends JpaRepository<Fornecedor, Long>, JpaSpecificationExecutor<Fornecedor> {

  @Query(
      "SELECT f FROM Fornecedor f WHERE "
          + "LOWER(f.nomeFantasia) LIKE LOWER(CONCAT(:query, '%')) OR "
          + "LOWER(f.razaoSocial) LIKE LOWER(CONCAT(:query, '%'))")
  Page<Fornecedor> findByNomeFantasiaLikeIgnoreCaseOrRazaoSocialLikeIgnoreCase(
      @Param("query") String query, Pageable pageable);

  /**
   * Busca paginada de fornecedores para listagem com campos otimizados.
   *
   * @param pageable paginação e ordenação
   * @return Page de projeções com apenas campos necessários para tabela
   */
  @Query(
      """
      SELECT f.id as id,
             f.razaoSocial as razaoSocial,
             f.nomeFantasia as nomeFantasia,
             f.cnpj as cnpj
      FROM Fornecedor f
      """)
  Page<FornecedorListProjection> findAllProjected(Pageable pageable);

  /**
   * Busca paginada de fornecedores com filtro de texto.
   *
   * @param filter texto para filtrar por id, razão social, nome fantasia ou CNPJ
   * @param pageable paginação e ordenação
   * @return Page de projeções filtradas
   */
  @Query(
      """
      SELECT f.id as id,
             f.razaoSocial as razaoSocial,
             f.nomeFantasia as nomeFantasia,
             f.cnpj as cnpj
      FROM Fornecedor f
      WHERE CAST(f.id AS string) LIKE CONCAT('%', :filter, '%')
         OR LOWER(f.razaoSocial) LIKE LOWER(CONCAT('%', :filter, '%'))
         OR LOWER(f.nomeFantasia) LIKE LOWER(CONCAT('%', :filter, '%'))
         OR f.cnpj LIKE CONCAT('%', :filter, '%')
      """)
  Page<FornecedorListProjection> findAllProjectedWithFilter(
      @Param("filter") String filter, Pageable pageable);
}
