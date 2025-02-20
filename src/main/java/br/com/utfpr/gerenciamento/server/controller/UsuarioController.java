package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.dto.*;
import br.com.utfpr.gerenciamento.server.model.Permissao;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.service.EmailService;
import br.com.utfpr.gerenciamento.server.service.PermissaoService;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import br.com.utfpr.gerenciamento.server.util.Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;

    private final PermissaoService permissaoService;

    private final EmailService emailService;

    public UsuarioController(UsuarioService usuarioService, PermissaoService permissaoService, EmailService emailService) {
        this.usuarioService = usuarioService;
        this.permissaoService = permissaoService;
        this.emailService = emailService;
    }

    @GetMapping
    public List<Usuario> findAll() {
        return usuarioService.findAll();
    }

    @GetMapping("{id}")
    public Usuario findOne(@PathVariable("id") Long id) {
        return usuarioService.findOne(id);
    }

    @PostMapping
    public Usuario save(@RequestBody Usuario usuario) {
        // TODO - remover as regras de negócio do controller e colocar no service.
        if (!Util.isPasswordEncoded(usuario.getPassword())) {
            usuario.setPassword(new BCryptPasswordEncoder().encode(usuario.getPassword()));
        }
        Set<Permissao> permissoes = new HashSet<>();
        usuario.getPermissoes().forEach(permissao ->
                permissoes.add(permissaoService.findOne(permissao.getId()))
        );
        usuario.setPermissoes(permissoes);
        return usuarioService.save(usuario);
    }

    @GetMapping("permissao")
    public List<Permissao> findAllPermissao() {
        return permissaoService.findAll();
    }

    @PostMapping("change-senha")
    public Usuario redefinirSenha(@RequestBody Usuario usuario,
                                  @RequestParam("senhaAtual") String senhaAtual) {
        // TODO - remover as regras de negócio do controller e colocar no service.
        Usuario userTemp = usuarioService.findOne(usuario.getId());
        BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder();
        usuario.setEmailVerificado(userTemp.getEmailVerificado());
        if (bCrypt.matches(senhaAtual, userTemp.getPassword())) {
            usuario.setPassword(bCrypt.encode(usuario.getPassword()));
            return usuarioService.save(usuario);
        }
        throw new RuntimeException("Senha incorreta");
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable("id") Long id) {
        usuarioService.delete(id);
    }

    @GetMapping("/complete")
    public List<Usuario> complete(@RequestParam("query") String query) {
        return usuarioService.usuarioComplete(query);
    }

    @GetMapping("/complete-custom")
    public List<Usuario> completeByUserOrDocOrNome(@RequestParam("query") String query) {
        return usuarioService.usuarioCompleteByUserAndDocAndNome(query);
    }

    @GetMapping("/complete-users-lab")
    public List<Usuario> completeUserLabs(@RequestParam("query") String query) {
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
    public ResponseEntity<GenericResponse> resendEmail(@RequestBody @Valid ConfirmEmailRequestDto confirmEmailRequestDto) throws Exception {
        return ResponseEntity.ok(GenericResponse.builder().message(usuarioService.resendEmail(confirmEmailRequestDto)).build());
    }
    @Value("${utfpr.front.url}")
    private String frontBaseUrl;

    @PostMapping("new-user")
    public Usuario saveNewUser(@RequestBody @Valid Usuario usuario) {
        // TODO - remover as regras de negócio do controller e colocar no service.
        if (!Util.isPasswordEncoded(usuario.getPassword())) {
            usuario.setPassword(new BCryptPasswordEncoder().encode(usuario.getPassword()));
        }
        try {
            usuario.setPermissoes(new HashSet<>());
            usuario.setUsername(usuario.getEmail());
            if (usuario.getEmail().contains("@utfpr.edu.br")) {
                usuario.getPermissoes().add(permissaoService.findByNome("ROLE_PROFESSOR"));
            } else {
                usuario.getPermissoes().add(permissaoService.findByNome("ROLE_ALUNO"));
            }
            usuario.setCodigoVerificacao(UUID.randomUUID().toString());
            usuario.setEmailVerificado(false);
            usuarioService.save(usuario);

            EmailDto emailDto = new EmailDto();
            emailDto.setEmailTo(usuario.getEmail());
            emailDto.setUsuario(usuario.getNome());
            emailDto.setUrl(frontBaseUrl + "/confirmar-email/" + usuario.getCodigoVerificacao());
            // TODO - adicionar constante com o nome do laboratório.
            emailDto.setSubject("Confirmação de email - Laboratório DAINF-PB (UTFPR)");
            emailDto.setSubjectBody("Confirmação de email - Laboratório DAINF-PB (UTFPR)");

            emailService.sendEmailWithTemplate(emailDto, emailDto.getEmailTo(), emailDto.getSubject(), "templateConfirmacaoCadastro");

            return usuario;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @PostMapping(path = "confirm-email")
    public ResponseEntity<GenericResponse> confirmEmail(@RequestBody @Valid ConfirmEmailRequestDto confirmEmailRequestDto) throws Exception {
        return ResponseEntity.ok(usuarioService.confirmEmail(confirmEmailRequestDto));
    }

    @PostMapping(path = "request-code-reset-password")
    public ResponseEntity<GenericResponse> sendEmailCodeRecoverPassword(@RequestBody @Valid ConfirmEmailRequestDto confirmEmailRequestDto) throws Exception {
        return ResponseEntity.ok(usuarioService.sendEmailCodeRecoverPassword(confirmEmailRequestDto.getEmail()));
    }

    @PostMapping(path = "reset-password")
    public ResponseEntity<GenericResponse> resetPassword(@RequestBody @Valid RecoverPasswordRequestDto recoverPasswordRequestDto) throws Exception {
        return ResponseEntity.ok(usuarioService.resetPassword(recoverPasswordRequestDto));
    }
}
