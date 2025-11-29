package br.com.utfpr.gerenciamento.server.security;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.repository.UsuarioRepository;
import br.com.utfpr.gerenciamento.server.service.impl.UsuarioServiceImpl;
import com.auth0.jwt.JWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  private final AuthenticationManager authenticationManager;
  private final UsuarioServiceImpl usuarioService;
  private final UsuarioRepository usuarioRepository;
  private final String tokenSecret;

  public JWTAuthenticationFilter(
      AuthenticationManager authenticationManager,
      UsuarioServiceImpl usuarioService,
      UsuarioRepository usuarioRepository,
      Environment env) {
    this.authenticationManager = authenticationManager;
    this.usuarioService = usuarioService;
    this.usuarioRepository = usuarioRepository;
    this.tokenSecret = env.getProperty("utfpr.token.secret");
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
      throws AuthenticationException {
    try {
      Usuario credentials = new ObjectMapper().readValue(req.getInputStream(), Usuario.class);
      String loginIdentifier = credentials.getUsername();
      String password = credentials.getPassword();

      if (loginIdentifier == null
          || loginIdentifier.trim().isEmpty()
          || password == null
          || password.trim().isEmpty()) {
        throw new BadCredentialsException("Credenciais inválidas");
      }

      Usuario user =
          usuarioRepository.findWithPermissoesByUsernameOrEmail(loginIdentifier, loginIdentifier);
      if (user == null) {
        throw new BadCredentialsException("Credenciais inválidas");
      }

      Authentication auth =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                  loginIdentifier, credentials.getPassword(), user.getAuthorities()));

      // Somente após autenticação bem-sucedida, verificar outras condições
      if (usuarioService.hasSolicitacaoNadaConstaPendingOrCompleted(loginIdentifier)) {
        throw new PreconditionRequiredAuthenticationException(
            "Foi realizado uma solicitação de nada consta para o usuário. Contate a administração.");
      }
      if (!user.getEmailVerificado()) {
        throw new DisabledException("Email não verificado. Por favor, verifique seu email.");
      }

      return auth;
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      throw new BadCredentialsException("Credenciais inválidas");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void successfulAuthentication(
      HttpServletRequest req, HttpServletResponse res, FilterChain chain, Authentication auth)
      throws IOException {
    var token =
        JWT.create()
            .withSubject(auth.getName())
            .withExpiresAt(Instant.now().plusMillis(SecurityConstants.EXPIRATION_TIME))
            .sign(HMAC512(tokenSecret));
    res.setContentType("application/json");
    res.addHeader(SecurityConstants.HEADER_STRING, SecurityConstants.TOKEN_PREFIX + token);
    res.getWriter().write(token);
  }

  @Override
  protected void unsuccessfulAuthentication(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException failed)
      throws IOException {
    String message = failed.getMessage();

    if (failed instanceof PreconditionRequiredAuthenticationException) {
      ProblemDetailResponseWriter.writePreconditionRequired(response, message);
    } else if (failed instanceof DisabledException) {
      ProblemDetailResponseWriter.writeForbiddenDisabled(response, message);
    } else {
      ProblemDetailResponseWriter.writeBadCredentials(response);
    }
  }
}
