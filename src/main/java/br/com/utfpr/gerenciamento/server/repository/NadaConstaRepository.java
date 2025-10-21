package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.enumeration.NadaConstaStatus;
import br.com.utfpr.gerenciamento.server.model.NadaConsta;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface NadaConstaRepository
    extends JpaRepository<NadaConsta, Long>, JpaSpecificationExecutor<NadaConsta> {
  // Métodos customizados podem ser adicionados aqui
  List<NadaConsta> findAllByUsuario(Usuario usuario);

  boolean existsByUsuarioAndStatusIn(Usuario usuario, Set<NadaConstaStatus> status);
}
