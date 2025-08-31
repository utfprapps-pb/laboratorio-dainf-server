package br.com.utfpr.gerenciamento.server.security.social;

import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.repository.UsuarioRepository;
import br.com.utfpr.gerenciamento.server.security.SecurityConstants;
import br.com.utfpr.gerenciamento.server.security.dto.AuthenticationResponseDTO;
import br.com.utfpr.gerenciamento.server.service.PermissaoService;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
public class AuthController {

  private final GoogleTokenVerifier googleTokenVerifier;

  private final UsuarioService usuarioService;

  private final PermissaoService permissaoService;
  private final UsuarioRepository usuarioRepository;

  @Value("${utfpr.token.secret}")
  private String tokenSecret;

  public AuthController(
      GoogleTokenVerifier googleTokenVerifier,
      UsuarioService usuarioService,
      UsuarioRepository usuarioRepository,
      PermissaoService permissaoService) {
    this.googleTokenVerifier = googleTokenVerifier;
    this.usuarioService = usuarioService;
    this.usuarioRepository = usuarioRepository;
    this.permissaoService = permissaoService;
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
          Usuario user = usuarioRepository.findByUsername(username);
          if (user == null) {
            user = new Usuario();
            user.setUsername(payload.getEmail());
            user.setEmail(payload.getEmail());
            user.setNome((String) payload.get("name"));
            user.setPassword("P4ssword");
            user.setTelefone("");
            if (payload.get("picture") != null) {
              user.setFotoUrl((String) payload.get("picture"));
            }

            user.setPermissoes(new HashSet<>());
            if (isProfessor) {
              user.getPermissoes().add(permissaoService.findByNome("ROLE_PROFESSOR"));
            } else {
              user.getPermissoes().add(permissaoService.findByNome("ROLE_ALUNO"));
            }
            usuarioService.save(user);
          } else {
            if (payload.get("picture") != null
                && (user.getFotoUrl() == null
                    || !user.getFotoUrl().equals((String) payload.get("picture")))) {
              user.setFotoUrl((String) payload.get("picture"));
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
}
