package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Fornecedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface FornecedorRepository
    extends JpaRepository<Fornecedor, Long>, JpaSpecificationExecutor<Fornecedor> {

  @Query(
      "SELECT f FROM Fornecedor f WHERE "
          + "LOWER(f.nomeFantasia) LIKE LOWER(CONCAT(:query, '%')) OR "
          + "LOWER(f.razaoSocial) LIKE LOWER(CONCAT(:query, '%'))")
  Page<Fornecedor> findByNomeFantasiaLikeIgnoreCaseOrRazaoSocialLikeIgnoreCase(
      @Param("query") String query, Pageable pageable);
}
