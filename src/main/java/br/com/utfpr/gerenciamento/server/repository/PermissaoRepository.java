package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Permissao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PermissaoRepository
    extends JpaRepository<Permissao, Long>, JpaSpecificationExecutor<Permissao> {
  Permissao findByNome(String nome);
}
