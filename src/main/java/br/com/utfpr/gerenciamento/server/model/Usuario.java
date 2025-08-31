package br.com.utfpr.gerenciamento.server.model;

import br.com.utfpr.gerenciamento.server.config.CustomAuthorityDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(
    ignoreUnknown = true,
    value = {"emailVerificado"})
@Builder
public class Usuario implements Serializable, UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "nome", length = 255, nullable = false)
  private String nome;

  @Column(name = "username", length = 100, nullable = false, unique = true)
  private String username;

  @Column(name = "documento", length = 25)
  private String documento;

  @Column(name = "password", length = 255, nullable = false)
  private String password;

  @Column(name = "email", length = 100, nullable = false)
  private String email;

  @Column(name = "telefone", length = 15, nullable = false)
  private String telefone;

  @ManyToMany(
      cascade = {CascadeType.MERGE, CascadeType.PERSIST},
      fetch = FetchType.EAGER)
  private Set<Permissao> permissoes;

  @Column(name = "foto_url", length = 2048)
  private String fotoUrl;

  @Column(name = "codigo_verificacao", length = 512)
  private String codigoVerificacao;

  @Column(name = "email_verificado")
  private boolean emailVerificado;

  public boolean getEmailVerificado() {
    return emailVerificado;
  }

  @JsonDeserialize(using = CustomAuthorityDeserializer.class)
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    List<GrantedAuthority> list = new ArrayList<>();
    list.addAll(this.permissoes);
    return list;
  }

  @Override
  public String getPassword() {
    return this.password;
  }

  @Override
  public String getUsername() {
    return this.username;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return this.emailVerificado;
  }
}
