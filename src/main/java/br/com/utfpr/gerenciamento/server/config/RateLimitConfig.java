package br.com.utfpr.gerenciamento.server.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracao do sistema de rate limiting usando Bucket4j com cache Caffeine.
 *
 * <p>Utiliza o algoritmo Token Bucket para controle de taxa de requisicoes. Cada IP possui um
 * bucket separado por endpoint, que se recarrega gradualmente ao longo do tempo.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RateLimitConfig {

  private final RateLimitProperties properties;

  /** Enum que mapeia endpoints para suas configuracoes de rate limit. */
  public enum RateLimitedEndpoint {
    LOGIN("/login"),
    AUTH("/auth"),
    PASSWORD_RESET_REQUEST("/usuario/request-code-reset-password"),
    NEW_USER("/usuario/new-user"),
    PASSWORD_RESET("/usuario/reset-password"),
    RESEND_EMAIL("/usuario/resend-confirm-email"),
    CONFIRM_EMAIL("/usuario/confirm-email");

    private final String path;

    RateLimitedEndpoint(String path) {
      this.path = path;
    }

    public String getPath() {
      return path;
    }

    /**
     * Encontra o endpoint correspondente ao path da requisicao.
     *
     * @param requestPath path da requisicao HTTP
     * @return o endpoint correspondente ou null se nao encontrado
     */
    public static RateLimitedEndpoint fromPath(String requestPath) {
      if (requestPath == null) {
        return null;
      }
      for (RateLimitedEndpoint endpoint : values()) {
        if (requestPath.equals(endpoint.path)) {
          return endpoint;
        }
      }
      return null;
    }
  }

  /**
   * Cache de buckets por IP e endpoint.
   *
   * <p>Chave: "IP:ENDPOINT" (ex: "192.168.1.1:LOGIN") Valor: Bucket com a configuracao do endpoint
   *
   * <p>Buckets expiram apos 2 horas de inatividade para liberar memoria.
   */
  @Bean
  public Cache<String, Bucket> rateLimitCache() {
    return Caffeine.newBuilder()
        .expireAfterAccess(2, TimeUnit.HOURS)
        .maximumSize(10_000)
        .recordStats()
        .build();
  }

  /** Mapa com configuracoes de limites por endpoint (carregado das properties). */
  @Bean
  public Map<RateLimitedEndpoint, RateLimitProperties.EndpointLimit> endpointLimits() {
    Map<RateLimitedEndpoint, RateLimitProperties.EndpointLimit> limits = new ConcurrentHashMap<>();

    limits.put(RateLimitedEndpoint.LOGIN, properties.getLogin());
    limits.put(RateLimitedEndpoint.AUTH, properties.getAuth());
    limits.put(RateLimitedEndpoint.PASSWORD_RESET_REQUEST, properties.getPasswordResetRequest());
    limits.put(RateLimitedEndpoint.NEW_USER, properties.getNewUser());
    limits.put(RateLimitedEndpoint.PASSWORD_RESET, properties.getPasswordReset());
    limits.put(RateLimitedEndpoint.RESEND_EMAIL, properties.getResendEmail());
    limits.put(RateLimitedEndpoint.CONFIRM_EMAIL, properties.getConfirmEmail());

    log.info("Rate limiting configurado para {} endpoints", limits.size());
    limits.forEach(
        (endpoint, limit) ->
            log.debug(
                "  {} -> {} requisicoes / {} minutos",
                endpoint.getPath(),
                limit.getRequests(),
                limit.getDurationMinutes()));

    return limits;
  }

  /**
   * Cria um novo bucket com a configuracao especificada.
   *
   * @param limit configuracao de limite do endpoint
   * @return um novo Bucket configurado
   */
  public Bucket createBucket(RateLimitProperties.EndpointLimit limit) {
    Bandwidth bandwidth =
        Bandwidth.builder()
            .capacity(limit.getRequests())
            .refillGreedy(limit.getRequests(), Duration.ofMinutes(limit.getDurationMinutes()))
            .build();

    return Bucket.builder().addLimit(bandwidth).build();
  }

  /**
   * Obtem ou cria um bucket para o IP e endpoint especificados.
   *
   * @param cache o cache de buckets
   * @param ip endereco IP do cliente
   * @param endpoint o endpoint sendo acessado
   * @param limits mapa de configuracoes de limites
   * @return o bucket existente ou um novo bucket criado
   */
  public Bucket resolveBucket(
      Cache<String, Bucket> cache,
      String ip,
      RateLimitedEndpoint endpoint,
      Map<RateLimitedEndpoint, RateLimitProperties.EndpointLimit> limits) {

    String key = ip + ":" + endpoint.name();
    return cache.get(key, k -> createBucket(limits.get(endpoint)));
  }
}
