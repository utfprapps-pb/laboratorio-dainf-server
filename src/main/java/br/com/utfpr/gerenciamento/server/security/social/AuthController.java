package br.com.utfpr.gerenciamento.server.security.social;

import br.com.utfpr.gerenciamento.server.ennumeation.AuthProvider;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.repository.UsuarioRepository;
import br.com.utfpr.gerenciamento.server.security.SecurityConstants;
import br.com.utfpr.gerenciamento.server.security.dto.AuthenticationResponseDTO;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@RestController
@RequestMapping("auth")
public class AuthController {

    private final GoogleTokenVerifier googleTokenVerifier;

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    @Value("${utfpr.token.secret}")
    private String tokenSecret;

    public AuthController(GoogleTokenVerifier googleTokenVerifier, UsuarioService usuarioService,
                          UsuarioRepository usuarioRepository) {
        this.googleTokenVerifier = googleTokenVerifier;
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping
    ResponseEntity<AuthenticationResponseDTO> auth(HttpServletRequest request, HttpServletResponse response) {
        String idToken = request.getHeader("Auth-Id-Token");
        if (idToken != null) {
            final Payload payload;
            try {
                payload = googleTokenVerifier.verify(idToken.replace(SecurityConstants.TOKEN_PREFIX, ""));
                if (payload != null) {
                    String username = payload.getEmail();
                    Usuario user = usuarioRepository.findByUsername(username);
                    if (user == null) {
                        user = new Usuario();
                        user.setUsername(payload.getEmail());
                        user.setNome( (String) payload.get("name"));
                        user.setPassword("P4ssword");
                        // user.setProvider(AuthProvider.google);
                        usuarioService.save(user);
                    }

                    String token = JWT.create()
                            .withSubject(username)
                            .withExpiresAt(new Date(System.currentTimeMillis() +
                                    SecurityConstants.EXPIRATION_TIME))
                            .sign(Algorithm.HMAC512(tokenSecret));

                    return  ResponseEntity.ok(new AuthenticationResponseDTO(token));

                }
            } catch (Exception e) {
                // This is not a valid token, the application will send HTTP 401 as a response
            }
        }
        return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    }
}
