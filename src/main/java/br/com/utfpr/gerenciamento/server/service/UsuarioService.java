package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.model.Usuario;

import java.util.List;

public interface UsuarioService extends CrudService<Usuario, Long> {

    List<Usuario> usuarioComplete(String query);

    Usuario findByUsername(String username);

    List<Usuario> usuarioCompleteByUserAndDocAndNome(String query);
}
