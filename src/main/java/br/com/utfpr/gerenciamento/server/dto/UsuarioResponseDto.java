package br.com.utfpr.gerenciamento.server.dto;

import br.com.utfpr.gerenciamento.server.model.Permissao;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Set;

@Data
public class UsuarioResponseDto {

    private Long id;

    private String nome;

    private String username;

    private String documento;

    private String email;

    private String telefone;

    private Set<Permissao> permissoes;

    private String fotoUrl;

    private boolean emailVerificado;

    private Collection<? extends GrantedAuthority> authorities;
}
