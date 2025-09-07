package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Fornecedor;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FornecedorRepository
    extends JpaRepository<Fornecedor, Long>, JpaSpecificationExecutor<Fornecedor> {

  List<Fornecedor> findByNomeFantasiaLikeIgnoreCase(String query);
}
