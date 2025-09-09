package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.dto.*;
import br.com.utfpr.gerenciamento.server.model.Permissao;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.service.PermissaoService;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("usuario")
public class UsuarioController {

  private final UsuarioService usuarioService;

  private final PermissaoService permissaoService;

  public UsuarioController(
      UsuarioService usuarioService,
      PermissaoService permissaoService) {
    this.usuarioService = usuarioService;
    this.permissaoService = permissaoService;
  }

  @GetMapping
  public List<UsuarioResponseDto> findAll() {
    return usuarioService.findAll()
            .stream()
            .map(usuarioService::convertToDto)
            .toList();
  }

  @GetMapping("{id}")
  public UsuarioResponseDto findOne(@PathVariable("id") Long id) {
    return usuarioService.convertToDto(usuarioService.findOne(id));
  }

  @PostMapping
  public UsuarioResponseDto save(@RequestBody Usuario usuario) {
    return usuarioService.convertToDto(usuarioService.save(usuario));
  }

  @GetMapping("permissao")
  public List<Permissao> findAllPermissao() {
    return permissaoService.findAll();
  }

  @PostMapping("change-senha")
  public UsuarioResponseDto redefinirSenha(
      @RequestBody Usuario usuario,
      @RequestParam("senhaAtual") String senhaAtual) {
    return usuarioService.convertToDto(usuarioService.updatePassword(usuario, senhaAtual));
  }

  @DeleteMapping("{id}")
  public void delete(@PathVariable("id") Long id) {
    usuarioService.delete(id);
  }

  @GetMapping("/complete")
  public List<UsuarioResponseDto> complete(@RequestParam("query") String query) {
    return usuarioService.usuarioComplete(query);
  }

  @GetMapping("/complete-custom")
  public List<UsuarioResponseDto> completeByUserOrDocOrNome(@RequestParam("query") String query) {
    return usuarioService.usuarioCompleteByUserAndDocAndNome(query);
  }

  @GetMapping("/complete-users-lab")
  public List<UsuarioResponseDto> completeUserLabs(@RequestParam("query") String query) {
    return usuarioService.usuarioCompleteLab(query);
  }

  @GetMapping("/find-by-username")
  public UsuarioResponseDto findByUsername(@RequestParam("username") String username) {
    return usuarioService.convertToDto(usuarioService.findByUsername(username));
  }

  @GetMapping("/user-info")
  public Principal principal(Principal principal) {
    return principal;
  }

  @PostMapping("/update-user")
  public void atualizarUsuario(@RequestBody Usuario usuario) {
    usuarioService.updateUsuario(usuario);
  }

  @PostMapping(path = "resend-confirm-email")
  public ResponseEntity<GenericResponse> resendEmail(
      @RequestBody @Valid ConfirmEmailRequestDto confirmEmailRequestDto) {
    return ResponseEntity.ok(
        GenericResponse.builder()
            .message(usuarioService.resendEmail(confirmEmailRequestDto))
            .build());
  }

  @Value("${utfpr.front.url}")
  private String frontBaseUrl;

  @PostMapping("new-user")
  public UsuarioResponseDto saveNewUser(@RequestBody @Valid Usuario usuario) {
    return usuarioService.convertToDto(usuarioService.saveNewUser(usuario));
  }

  @PostMapping(path = "confirm-email")
  public ResponseEntity<GenericResponse> confirmEmail(
      @RequestBody @Valid ConfirmEmailRequestDto confirmEmailRequestDto) {
    return ResponseEntity.ok(usuarioService.confirmEmail(confirmEmailRequestDto));
  }

  @PostMapping(path = "request-code-reset-password")
  public ResponseEntity<GenericResponse> sendEmailCodeRecoverPassword(
      @RequestBody @Valid ConfirmEmailRequestDto confirmEmailRequestDto) {
    return ResponseEntity.ok(
        usuarioService.sendEmailCodeRecoverPassword(confirmEmailRequestDto.getEmail()));
  }

  @PostMapping(path = "reset-password")
  public ResponseEntity<GenericResponse> resetPassword(
      @RequestBody @Valid RecoverPasswordRequestDto recoverPasswordRequestDto) {
    return ResponseEntity.ok(usuarioService.resetPassword(recoverPasswordRequestDto));
  }
}
