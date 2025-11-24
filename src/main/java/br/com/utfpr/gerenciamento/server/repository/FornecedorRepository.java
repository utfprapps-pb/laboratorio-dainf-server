package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Fornecedor;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FornecedorRepository
    extends JpaRepository<Fornecedor, Long>, JpaSpecificationExecutor<Fornecedor> {

  @Query("SELECT f FROM Fornecedor f WHERE LOWER(f.nomeFantasia) LIKE LOWER(:query) OR LOWER(f.razaoSocial) LIKE LOWER(:query)")
  List<Fornecedor> findByNomeFantasiaLikeIgnoreCaseOrRazaoSocialLikeIgnoreCase(@Param("query") String query);
}
