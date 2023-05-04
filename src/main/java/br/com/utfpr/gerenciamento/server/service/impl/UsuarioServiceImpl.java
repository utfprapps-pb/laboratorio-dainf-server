package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.repository.UsuarioRepository;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioServiceImpl extends CrudServiceImpl<Usuario, Long>
        implements UsuarioService, UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    private final ModelMapper modelMapper;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, ModelMapper modelMapper) {
        this.usuarioRepository = usuarioRepository;
        this.modelMapper = modelMapper;
    }

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
        if (username.contains("@professores.utfpr.edu.br")) {
            username = username.replace("professores.", "");
        } else if (username.contains("@administrativo.utfpr.edu.br")) {
            username = username.replace("administrativo.", "");
        }
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

    @Override
    public Usuario updateUsuario(Usuario usuario) {
        if (usuario.getUsername().equals((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal())) {
            Usuario usuarioTmp = usuarioRepository.findByUsername(usuario.getUsername());
            usuarioTmp.setTelefone(usuario.getTelefone());
            usuarioTmp.setDocumento(usuario.getDocumento());
            usuarioRepository.save(usuarioTmp);
            return usuarioTmp;
        }
        return null;
    }

    public UsuarioResponseDto convertToDto(Usuario entity) {
        return modelMapper.map(entity, UsuarioResponseDto.class);
    }

    public Usuario convertToEntity(UsuarioResponseDto entityDto) {
        return modelMapper.map(entityDto, Usuario.class);
    }

}
