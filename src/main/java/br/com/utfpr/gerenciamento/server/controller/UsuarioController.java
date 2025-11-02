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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("usuario")
public class UsuarioController {

  private final UsuarioService usuarioService;

  private final PermissaoService permissaoService;

  public UsuarioController(UsuarioService usuarioService, PermissaoService permissaoService) {
    this.usuarioService = usuarioService;
    this.permissaoService = permissaoService;
  }

  @GetMapping
  public List<UsuarioResponseDto> findAll() {
    return usuarioService.findAll();
  }

  @GetMapping("{id}")
  public UsuarioResponseDto findOne(@PathVariable("id") Long id) {
    return usuarioService.findOne(id);
  }

  @GetMapping("page")
  public Page<UsuarioResponseDto> findAllPaged(
      @RequestParam("page") int page,
      @RequestParam("size") int size,
      @RequestParam(required = false) String filter,
      @RequestParam(required = false) String order,
      @RequestParam(required = false) Boolean asc) {
    PageRequest pageRequest = PageRequest.of(page, size);
    if (order != null && asc != null) {
      pageRequest =
          PageRequest.of(page, size, asc ? Sort.Direction.ASC : Sort.Direction.DESC, order);
    }
    Page<UsuarioResponseDto> result;
    if (filter != null && !filter.isEmpty()) {
      Specification<Usuario> spec = usuarioService.filterByAllFields(filter);
      result = usuarioService.findAllSpecification(spec, pageRequest);
    } else {
      result = usuarioService.findAll(pageRequest);
    }
    return result;
  }

  @PostMapping
  public UsuarioResponseDto save(@RequestBody Usuario usuario) {
    return usuarioService.save(usuario);
  }

  @GetMapping("permissao")
  public List<PermissaoResponseDTO> findAllPermissao() {
    return permissaoService.findAll();
  }

  @PostMapping("change-senha")
  public UsuarioResponseDto redefinirSenha(
      @RequestBody Usuario usuario, @RequestParam("senhaAtual") String senhaAtual) {
    return usuarioService.updatePassword(usuario, senhaAtual);
  }

  @DeleteMapping("{id}")
  public void delete(@PathVariable("id") Long id) {
    usuarioService.delete(id);
  }

  @GetMapping("/complete")
  public Page<UsuarioResponseDto> complete(
      @RequestParam("query") String query,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    return usuarioService.usuarioComplete(query, pageable);
  }

  @GetMapping("/complete-custom")
  public Page<UsuarioResponseDto> completeByUserOrDocOrNome(
      @RequestParam("query") String query,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    return usuarioService.usuarioCompleteByUserAndDocAndNome(query, pageable);
  }

  @GetMapping("/complete-users-lab")
  public Page<UsuarioResponseDto> completeUserLabs(
      @RequestParam("query") String query,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    return usuarioService.usuarioCompleteLab(query, pageable);
  }

  @GetMapping("/find-by-username")
  public UsuarioResponseDto findByUsername(@RequestParam("username") String username) {
    return usuarioService.findByUsername(username);
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
    return usuarioService.saveNewUser(usuario);
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
