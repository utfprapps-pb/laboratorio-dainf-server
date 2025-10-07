package br.com.utfpr.gerenciamento.server.security;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.service.impl.UsuarioServiceImpl;
import com.auth0.jwt.JWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  private final AuthenticationManager authenticationManager;
  private final UsuarioServiceImpl usuarioService;
  private final String tokenSecret;

  public JWTAuthenticationFilter(
      AuthenticationManager authenticationManager,
      UsuarioServiceImpl usuarioService,
      Environment env) {
    this.authenticationManager = authenticationManager;
    this.usuarioService = usuarioService;
    this.tokenSecret = env.getProperty("utfpr.token.secret");
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
      throws AuthenticationException {
    try {
      Usuario credentials = new ObjectMapper().readValue(req.getInputStream(), Usuario.class);
      if (credentials.getUsername().contains("@professores.utfpr.edu.br")) {
        credentials.setUsername(credentials.getUsername().replace("professores.", ""));
      } else if (credentials.getUsername().contains("@administrativo.utfpr.edu.br")) {
        credentials.setUsername(credentials.getUsername().replace("administrativo.", ""));
      }
      // IMPORTANTE: Usa método COM @EntityGraph porque getAuthorities() precisa das permissoes
      Usuario user = usuarioService.findByUsernameForAuthentication(credentials.getUsername());

      return authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              credentials.getUsername(), credentials.getPassword(), user.getAuthorities()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void successfulAuthentication(
      HttpServletRequest req, HttpServletResponse res, FilterChain chain, Authentication auth)
      throws IOException, ServletException {
    String token =
        JWT.create()
            .withSubject(auth.getName())
            .withExpiresAt(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
            .sign(HMAC512(tokenSecret));
    res.getWriter().write(token);
  }
}
