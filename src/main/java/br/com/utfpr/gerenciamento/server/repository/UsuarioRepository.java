package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Usuario findByUsername(String username);
}
