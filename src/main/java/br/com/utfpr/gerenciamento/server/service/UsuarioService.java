package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.dto.ConfirmEmailRequestDto;
import br.com.utfpr.gerenciamento.server.dto.GenericResponse;
import br.com.utfpr.gerenciamento.server.dto.RecoverPasswordRequestDto;
import br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UsuarioService extends CrudService<Usuario, Long> {

  Page<UsuarioResponseDto> usuarioComplete(String query, Pageable pageable);

  /**
   * Busca usuario por username SEM carregar permissoes (LAZY). Use para operações normais que não
   * precisam de permissões.
   */
  Usuario findByUsername(String username);

  /**
   * Busca usuario por username COM permissoes carregadas (para autenticação). Use SOMENTE nos
   * fluxos de autenticação onde UserDetails.getAuthorities() será chamado.
   */
  Usuario findByUsernameForAuthentication(String username);

  Page<UsuarioResponseDto> usuarioCompleteByUserAndDocAndNome(String query, Pageable pageable);

  Page<UsuarioResponseDto> usuarioCompleteLab(String query, Pageable pageable);

  Usuario updateUsuario(Usuario usuario);

  UsuarioResponseDto convertToDto(Usuario entity);

  Usuario convertToEntity(UsuarioResponseDto entityDto);

  String resendEmail(ConfirmEmailRequestDto confirmEmailRequestDto);

  GenericResponse sendEmailCodeRecoverPassword(String email);

  GenericResponse confirmEmail(ConfirmEmailRequestDto confirmEmailRequestDto);

  GenericResponse resetPassword(RecoverPasswordRequestDto recoverPasswordRequestDto);

  Usuario updatePassword(Usuario entity, String password);

  Usuario saveNewUser(Usuario entity);

  Usuario findByDocumento(String documento);
}
