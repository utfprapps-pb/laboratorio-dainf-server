package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.model.Permissao;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.repository.UsuarioRepository;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UsuarioServiceImpl extends CrudServiceImpl<Usuario, Long>
        implements UsuarioService, UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    protected JpaRepository<Usuario, Long> getRepository() {
        return usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username);
        if (usuario == null) {
            throw new UsernameNotFoundException("Usuário não encontrado");
        }
        return usuario;
    }

    @Override
    public List<Usuario> usuarioComplete(String query) {
        if ("".equalsIgnoreCase(query)) {
            return usuarioRepository.findAll();
        }
        return usuarioRepository.findByNomeLikeIgnoreCase("%" + query + "%");
    }

    @Override
    public Usuario findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Override
    public List<Usuario> usuarioCompleteByUserAndDocAndNome(String query) {
        if ("".equalsIgnoreCase(query)) {
            return usuarioRepository.findAllCustom();
        }
        return usuarioRepository.findUsuarioCompleteCustom("%" + query.toUpperCase() + "%");
    }

    @Override
    public List<Usuario> usuarioCompleteLab(String query) {
        if ("".equalsIgnoreCase(query)) {
            return usuarioRepository.findAllCustomLab();
        }
        return usuarioRepository.findUsuarioCompleteCustomLab("%" + query.toUpperCase() + "%");
    }
}
