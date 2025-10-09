package br.com.utfpr.gerenciamento.server.controller;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

import br.com.utfpr.gerenciamento.server.dto.TokenDto;
import br.com.utfpr.gerenciamento.server.enumeration.UserRole;
import br.com.utfpr.gerenciamento.server.model.Permissao;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.security.SecurityConstants;
import br.com.utfpr.gerenciamento.server.service.PermissaoService;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import com.auth0.jwt.JWT;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/oauth")
public class OauthController {

  @Value("${google.clientId}")
  String googleClientId;

  final PasswordEncoder passwordEncoder;

  final UsuarioService usuarioService;

  final PermissaoService permissaoService;

  public OauthController(
      PasswordEncoder passwordEncoder,
      UsuarioService usuarioService,
      PermissaoService permissaoService) {
    this.passwordEncoder = passwordEncoder;
    this.usuarioService = usuarioService;
    this.permissaoService = permissaoService;
  }

  @PostMapping("/google")
  public ResponseEntity<TokenDto> google(@RequestBody TokenDto tokenDto) throws IOException {
    final NetHttpTransport transport = new NetHttpTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    GoogleIdTokenVerifier.Builder verifier =
        new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
            .setAudience(Collections.singletonList(googleClientId));
    final GoogleIdToken googleIdToken =
        GoogleIdToken.parse(verifier.getJsonFactory(), tokenDto.getValue());
    final GoogleIdToken.Payload payload = googleIdToken.getPayload();
    Usuario usuario = usuarioService.findByUsername(payload.getEmail());
    if (usuario == null) {
      usuario = createOAuthUser(payload.getEmail());
    }
    TokenDto tokenRes = createJwtToken(usuario);
    return new ResponseEntity(tokenRes, HttpStatus.OK);
  }

  /**
   * Cria token JWT diretamente sem autenticação via senha.
   *
   * <p>OAuth users são pré-autenticados pelo Google, então não precisamos validar senha.
   */
  private TokenDto createJwtToken(Usuario usuario) {
    // Cria autenticação diretamente com as authorities do usuário
    Authentication authentication =
        new UsernamePasswordAuthenticationToken(usuario.getEmail(), null, usuario.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);

    String jwt =
        JWT.create()
            .withSubject(usuario.getEmail())
            .withExpiresAt(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
            .sign(HMAC512(""));

    TokenDto tokenDto = new TokenDto();
    tokenDto.setValue(jwt);
    return tokenDto;
  }

  /**
   * Cria um novo usuário OAuth com senha aleatória criptograficamente segura.
   *
   * <p>A senha nunca é revelada ao usuário e serve apenas para satisfazer requisitos do banco de
   * dados. OAuth users não fazem login com senha - apenas via Google.
   */
  private Usuario createOAuthUser(String email) {
    Usuario usuario = new Usuario();
    usuario.setEmail(email);
    usuario.setUsername(email);

    // Gera senha aleatória de 32 bytes (256 bits) - criptograficamente segura
    String randomPassword = generateSecureRandomPassword();
    usuario.setPassword(passwordEncoder.encode(randomPassword));

    Permissao permissao;
    if (email.contains("@alunos.utfpr.edu.br") || email.contains("@administrativo.utfpr.edu.br")) {
      permissao = permissaoService.findByNome(UserRole.ALUNO.getAuthority());
    } else if (email.contains("@utfpr.edu.br") || email.contains("@professores.utfpr.edu.br")) {
      permissao = permissaoService.findByNome(UserRole.PROFESSOR.getAuthority());
    } else {
      throw new IllegalArgumentException("Email domain não reconhecido: " + email);
    }

    if (permissao == null) {
      throw new IllegalStateException(
          "Permissão não encontrada no banco de dados. Verifique as migrations.");
    }

    usuario.setPermissoes(new HashSet<>());
    usuario.getPermissoes().add(permissao);
    usuario.setEmailVerificado(true); // OAuth emails são pré-verificados pelo Google

    return usuarioService.save(usuario);
  }

  /**
   * Gera senha aleatória criptograficamente segura de 32 bytes (256 bits).
   *
   * <p>Usa SecureRandom e Base64 para criar senha forte que nunca será conhecida pelo usuário.
   */
  private String generateSecureRandomPassword() {
    SecureRandom secureRandom = new SecureRandom();
    byte[] randomBytes = new byte[32]; // 256 bits
    secureRandom.nextBytes(randomBytes);
    return Base64.getEncoder().encodeToString(randomBytes);
  }
}
