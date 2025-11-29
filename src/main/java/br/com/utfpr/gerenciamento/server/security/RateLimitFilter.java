package br.com.utfpr.gerenciamento.server.security;

import br.com.utfpr.gerenciamento.server.config.RateLimitConfig;
import br.com.utfpr.gerenciamento.server.config.RateLimitConfig.RateLimitedEndpoint;
import br.com.utfpr.gerenciamento.server.config.RateLimitProperties;
import com.github.benmanes.caffeine.cache.Cache;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro de rate limiting para endpoints publicos.
 *
 * <p>Intercepta requisicoes para endpoints sensiveis (login, registro, recuperacao de senha) e
 * aplica limites de taxa por IP usando o algoritmo Token Bucket.
 *
 * <p>Este filtro executa ANTES dos filtros de autenticacao JWT para bloquear requisicoes abusivas o
 * mais cedo possivel na cadeia de filtros.
 *
 * <p>Ordem de execucao:
 *
 * <ol>
 *   <li>TraceIdFilter (HIGHEST_PRECEDENCE)
 *   <li>RateLimitFilter (HIGHEST_PRECEDENCE + 1)
 *   <li>JWTAuthenticationFilter
 *   <li>JWTAuthorizationFilter
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RateLimitFilter extends OncePerRequestFilter {

  private final RateLimitProperties properties;
  private final RateLimitConfig rateLimitConfig;
  private final Cache<String, Bucket> rateLimitCache;
  private final Map<RateLimitedEndpoint, RateLimitProperties.EndpointLimit> endpointLimits;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    // Rate limiting desabilitado
    if (!properties.isEnabled()) {
      filterChain.doFilter(request, response);
      return;
    }

    // Apenas POST para endpoints de rate limit
    if (!HttpMethod.POST.matches(request.getMethod())) {
      filterChain.doFilter(request, response);
      return;
    }

    String path = request.getRequestURI();
    RateLimitedEndpoint endpoint = RateLimitedEndpoint.fromPath(path);

    // Endpoint nao esta na lista de rate limit
    if (endpoint == null) {
      filterChain.doFilter(request, response);
      return;
    }

    String clientIp = getClientIp(request);
    Bucket bucket =
        rateLimitConfig.resolveBucket(rateLimitCache, clientIp, endpoint, endpointLimits);

    ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

    if (probe.isConsumed()) {
      log.debug(
          "Rate limit OK - IP: {}, Endpoint: {}, Restante: {}",
          clientIp,
          endpoint.getPath(),
          probe.getRemainingTokens());
      filterChain.doFilter(request, response);
    } else {
      long waitTimeSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
      log.warn(
          "Rate limit excedido - IP: {}, Endpoint: {}, Aguardar: {}s",
          clientIp,
          endpoint.getPath(),
          waitTimeSeconds);

      response.setHeader("Retry-After", String.valueOf(waitTimeSeconds));
      ProblemDetailResponseWriter.writeProblemDetail(
          response,
          HttpStatus.TOO_MANY_REQUESTS,
          "Limite de requisicoes excedido",
          String.format(
              "Muitas tentativas para %s. Tente novamente em %d minutos.",
              getEndpointDescription(endpoint), (waitTimeSeconds / 60) + 1),
          "/errors/rate-limit-exceeded");
    }
  }

  /**
   * Extrai o IP real do cliente considerando proxies reversos.
   *
   * <p>Verifica os headers X-Forwarded-For e X-Real-IP antes de usar o IP remoto direto.
   *
   * @param request a requisicao HTTP
   * @return o endereco IP do cliente
   */
  private String getClientIp(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      // X-Forwarded-For pode conter multiplos IPs; o primeiro eh o cliente original
      return xForwardedFor.split(",")[0].trim();
    }

    String xRealIp = request.getHeader("X-Real-IP");
    if (xRealIp != null && !xRealIp.isEmpty()) {
      return xRealIp.trim();
    }

    return request.getRemoteAddr();
  }

  /**
   * Retorna uma descricao amigavel do endpoint para mensagens de erro.
   *
   * @param endpoint o endpoint
   * @return descricao em portugues
   */
  private String getEndpointDescription(RateLimitedEndpoint endpoint) {
    return switch (endpoint) {
      case LOGIN -> "login";
      case AUTH -> "autenticacao";
      case PASSWORD_RESET_REQUEST -> "solicitacao de recuperacao de senha";
      case NEW_USER -> "registro de usuario";
      case PASSWORD_RESET -> "redefinicao de senha";
      case RESEND_EMAIL -> "reenvio de email";
      case CONFIRM_EMAIL -> "confirmacao de email";
    };
  }
}
