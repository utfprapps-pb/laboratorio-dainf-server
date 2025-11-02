package br.com.utfpr.gerenciamento.server.security;

import static br.com.utfpr.gerenciamento.server.security.SecurityConstants.*;

import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.service.impl.UsuarioServiceImpl;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

  private final UsuarioServiceImpl usuarioService;
  private final String tokenSecret;

  public JWTAuthorizationFilter(
      AuthenticationManager authenticationManager,
      UsuarioServiceImpl usuarioService,
      Environment env) {
    super(authenticationManager);
    this.usuarioService = usuarioService;
    this.tokenSecret = env.getProperty("utfpr.token.secret");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    String header = req.getHeader(HEADER_STRING);

    if (header == null || !header.startsWith(TOKEN_PREFIX)) {
      chain.doFilter(req, res);
      return;
    }

    UsernamePasswordAuthenticationToken authentication = getAuthentication(req);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    chain.doFilter(req, res);
  }

  private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
    String token = request.getHeader(HEADER_STRING);
    if (token != null) {
      String user =
          JWT.require(Algorithm.HMAC512(tokenSecret))
              .build()
              .verify(token.replace(TOKEN_PREFIX, ""))
              .getSubject();
      if (user != null) {
        // IMPORTANTE: Usa método COM @EntityGraph porque getAuthorities() precisa das permissoes
        Usuario u = usuarioService.toEntity( usuarioService.findByUsernameForAuthentication(user));
        return new UsernamePasswordAuthenticationToken(user, null, u.getAuthorities());
      }
      return null;
    }
    return null;
  }
}
