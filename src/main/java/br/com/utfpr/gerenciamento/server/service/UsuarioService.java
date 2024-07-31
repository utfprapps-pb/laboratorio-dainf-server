package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.dto.ConfirmEmailRequestDto;
import br.com.utfpr.gerenciamento.server.dto.GenericResponse;
import br.com.utfpr.gerenciamento.server.dto.RecoverPasswordRequestDto;
import br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto;
import br.com.utfpr.gerenciamento.server.model.Usuario;

import java.util.List;

public interface UsuarioService extends CrudService<Usuario, Long> {

    List<Usuario> usuarioComplete(String query);

    Usuario findByUsername(String username);

    List<Usuario> usuarioCompleteByUserAndDocAndNome(String query);

    List<Usuario> usuarioCompleteLab(String query);

    Usuario updateUsuario(Usuario usuario);

    UsuarioResponseDto convertToDto(Usuario entity);

    Usuario convertToEntity(UsuarioResponseDto entityDto);

    String resendEmail(ConfirmEmailRequestDto confirmEmailRequestDto);

    GenericResponse sendEmailCodeRecoverPassword(String email);

    GenericResponse confirmEmail(ConfirmEmailRequestDto confirmEmailRequestDto);

    GenericResponse resetPassword(RecoverPasswordRequestDto recoverPasswordRequestDto);
}
