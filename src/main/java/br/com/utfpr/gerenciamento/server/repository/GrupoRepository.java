package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Grupo;
import java.util.List;

import br.com.utfpr.gerenciamento.server.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GrupoRepository extends JpaRepository<Grupo, Long> , JpaSpecificationExecutor<Grupo> {

  List<Grupo> findByDescricaoLikeIgnoreCase(String query);
}
