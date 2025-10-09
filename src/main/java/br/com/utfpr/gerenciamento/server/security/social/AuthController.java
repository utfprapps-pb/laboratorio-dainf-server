package br.com.utfpr.gerenciamento.server.security.social;

import br.com.utfpr.gerenciamento.server.enumeration.UserRole;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.security.SecurityConstants;
import br.com.utfpr.gerenciamento.server.security.dto.AuthenticationResponseDTO;
import br.com.utfpr.gerenciamento.server.service.PermissaoService;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
public class AuthController {

  private static final String PICTURE = "picture";
  private final GoogleTokenVerifier googleTokenVerifier;

  private final UsuarioService usuarioService;

  private final PermissaoService permissaoService;

  private final PasswordEncoder passwordEncoder;

  @Value("${utfpr.token.secret}")
  private String tokenSecret;

  public AuthController(
      GoogleTokenVerifier googleTokenVerifier,
      UsuarioService usuarioService,
      PermissaoService permissaoService,
      PasswordEncoder passwordEncoder) {
    this.googleTokenVerifier = googleTokenVerifier;
    this.usuarioService = usuarioService;
    this.permissaoService = permissaoService;
    this.passwordEncoder = passwordEncoder;
  }

  @PostMapping
  ResponseEntity<AuthenticationResponseDTO> auth(
      HttpServletRequest request, HttpServletResponse response) {
    String idToken = request.getHeader("Auth-Id-Token");
    if (idToken != null) {
      final Payload payload;
      boolean isProfessor = false;
      try {
        payload = googleTokenVerifier.verify(idToken.replace(SecurityConstants.TOKEN_PREFIX, ""));
        if (payload != null
            && (payload.getEmail().contains("@alunos.utfpr.edu.br")
                || payload.getEmail().contains("@professores.utfpr.edu.br")
                || payload.getEmail().contains("@administrativo.utfpr.edu.br")
                || payload.getEmail().contains("@utfpr.edu.br"))) {

          if (payload.getEmail().contains("@professores.utfpr.edu.br")) {
            payload.setEmail(payload.getEmail().replace("professores.", ""));
            isProfessor = true;
          } else if (payload.getEmail().contains("@administrativo.utfpr.edu.br")) {
            payload.setEmail(payload.getEmail().replace("administrativo.", ""));
          } else if (payload.getEmail().contains("@utfpr.edu.br")) {
            isProfessor = true;
          }

          String username = payload.getEmail();
          Usuario user = usuarioService.findByUsernameForAuthentication(username);
          if (user == null) {
            user = createOAuthUser(payload, isProfessor);
          } else {
            // Atualiza foto se mudou no Google
            if (payload.get(PICTURE) != null
                && (user.getFotoUrl() == null || !user.getFotoUrl().equals(payload.get(PICTURE)))) {
              user.setFotoUrl((String) payload.get(PICTURE));
              usuarioService.save(user);
            }
          }

          String token =
              JWT.create()
                  .withSubject(username)
                  .withExpiresAt(
                      new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
                  .sign(Algorithm.HMAC512(tokenSecret));

          return ResponseEntity.ok(
              new AuthenticationResponseDTO(
                  token, user.getUsername(), user.getNome(), user.getEmail()));

        } else {
          throw new Exception("O email precisa ser da UTFPR");
        }
      } catch (Exception e) {
        e.printStackTrace();
        // This is not a valid token, the application will send HTTP 401 as a response
      }
    }
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
  }

  /**
   * Cria um novo usuário OAuth com senha aleatória criptograficamente segura.
   *
   * <p>A senha nunca é revelada ao usuário e serve apenas para satisfazer requisitos do banco de
   * dados. OAuth users não fazem login com senha - apenas via Google.
   */
  private Usuario createOAuthUser(Payload payload, boolean isProfessor) {
    Usuario user = new Usuario();
    user.setUsername(payload.getEmail());
    user.setEmail(payload.getEmail());
    user.setNome((String) payload.get("name"));

    // Gera senha aleatória de 32 bytes (256 bits) - criptograficamente segura
    String randomPassword = generateSecureRandomPassword();
    user.setPassword(passwordEncoder.encode(randomPassword));

    user.setTelefone("");
    if (payload.get(PICTURE) != null) {
      user.setFotoUrl((String) payload.get(PICTURE));
    }

    user.setPermissoes(new HashSet<>());
    if (isProfessor) {
      user.getPermissoes().add(permissaoService.findByNome(UserRole.PROFESSOR.getAuthority()));
    } else {
      user.getPermissoes().add(permissaoService.findByNome(UserRole.ALUNO.getAuthority()));
    }
    user.setEmailVerificado(true); // OAuth emails são pré-verificados pelo Google

    return usuarioService.save(user);
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
