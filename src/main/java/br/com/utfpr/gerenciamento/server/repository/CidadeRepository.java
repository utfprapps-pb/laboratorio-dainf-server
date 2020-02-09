package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Cidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CidadeRepository extends JpaRepository<Cidade, Long> {

    List<Cidade> findByNomeLikeIgnoreCase(String query);
}
