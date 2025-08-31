package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Cidade;
import br.com.utfpr.gerenciamento.server.model.Estado;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CidadeRepository extends JpaRepository<Cidade, Long> {

  List<Cidade> findByNomeLikeIgnoreCase(String query);

  List<Cidade> findAllByEstado(Estado estado);

  List<Cidade> findByNomeLikeIgnoreCaseAndEstado(String query, Estado estado);
}
