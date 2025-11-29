package br.com.utfpr.gerenciamento.server.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.config.RateLimitConfig;
import br.com.utfpr.gerenciamento.server.config.RateLimitConfig.RateLimitedEndpoint;
import br.com.utfpr.gerenciamento.server.config.RateLimitProperties;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RateLimitFilterTest {

  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock private FilterChain filterChain;

  private RateLimitProperties properties;
  private RateLimitConfig rateLimitConfig;
  private Cache<String, Bucket> rateLimitCache;
  private Map<RateLimitedEndpoint, RateLimitProperties.EndpointLimit> endpointLimits;
  private RateLimitFilter rateLimitFilter;

  @BeforeEach
  void setUp() {
    properties = new RateLimitProperties();
    properties.setEnabled(true);
    // Limites baixos para testes
    properties.setLogin(new RateLimitProperties.EndpointLimit(3, 15));

    rateLimitConfig = new RateLimitConfig(properties);
    rateLimitCache =
        Caffeine.newBuilder().expireAfterAccess(2, TimeUnit.HOURS).maximumSize(10_000).build();

    endpointLimits = new ConcurrentHashMap<>();
    endpointLimits.put(RateLimitedEndpoint.LOGIN, properties.getLogin());
    endpointLimits.put(RateLimitedEndpoint.AUTH, new RateLimitProperties.EndpointLimit(10, 15));
    endpointLimits.put(
        RateLimitedEndpoint.PASSWORD_RESET_REQUEST, new RateLimitProperties.EndpointLimit(3, 60));
    endpointLimits.put(RateLimitedEndpoint.NEW_USER, new RateLimitProperties.EndpointLimit(3, 60));
    endpointLimits.put(
        RateLimitedEndpoint.PASSWORD_RESET, new RateLimitProperties.EndpointLimit(5, 15));
    endpointLimits.put(
        RateLimitedEndpoint.RESEND_EMAIL, new RateLimitProperties.EndpointLimit(5, 60));
    endpointLimits.put(
        RateLimitedEndpoint.CONFIRM_EMAIL, new RateLimitProperties.EndpointLimit(10, 15));

    rateLimitFilter =
        new RateLimitFilter(properties, rateLimitConfig, rateLimitCache, endpointLimits);
  }

  @Test
  void shouldAllowRequestWhenRateLimitNotExceeded() throws Exception {
    when(request.getMethod()).thenReturn("POST");
    when(request.getRequestURI()).thenReturn("/login");
    when(request.getRemoteAddr()).thenReturn("192.168.1.1");

    rateLimitFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(response, never()).setStatus(429);
  }

  @Test
  void shouldBlockRequestWhenRateLimitExceeded() throws Exception {
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);
    when(request.getMethod()).thenReturn("POST");
    when(request.getRequestURI()).thenReturn("/login");
    when(request.getRemoteAddr()).thenReturn("192.168.1.2");

    // Consumir todos os tokens (limite = 3)
    for (int i = 0; i < 3; i++) {
      rateLimitFilter.doFilterInternal(request, response, filterChain);
    }

    // Reset writer para proxima requisicao
    stringWriter = new StringWriter();
    writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    // 4a requisicao deve ser bloqueada
    rateLimitFilter.doFilterInternal(request, response, filterChain);

    writer.flush();
    String jsonResponse = stringWriter.toString();

    verify(response).setHeader(eq("Retry-After"), anyString());
    assertTrue(jsonResponse.contains("rate-limit-exceeded"));
    assertTrue(jsonResponse.contains("Muitas tentativas"));
  }

  @Test
  void shouldSkipNonPostRequests() throws Exception {
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/login");

    rateLimitFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(response, never()).setStatus(429);
  }

  @Test
  void shouldSkipNonRateLimitedEndpoints() throws Exception {
    when(request.getMethod()).thenReturn("POST");
    when(request.getRequestURI()).thenReturn("/item");

    rateLimitFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(response, never()).setStatus(429);
  }

  @Test
  void shouldSkipWhenRateLimitDisabled() throws Exception {
    properties.setEnabled(false);
    rateLimitFilter =
        new RateLimitFilter(properties, rateLimitConfig, rateLimitCache, endpointLimits);

    when(request.getMethod()).thenReturn("POST");
    when(request.getRequestURI()).thenReturn("/login");

    rateLimitFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void shouldUseXForwardedForHeader() throws Exception {
    when(request.getMethod()).thenReturn("POST");
    when(request.getRequestURI()).thenReturn("/login");
    when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 192.168.1.1");
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");

    rateLimitFilter.doFilterInternal(request, response, filterChain);

    // Verifica que a requisicao foi processada
    verify(filterChain).doFilter(request, response);

    // Verifica que o bucket foi criado para o IP correto (primeiro da lista X-Forwarded-For)
    Bucket bucket = rateLimitCache.getIfPresent("10.0.0.1:" + RateLimitedEndpoint.LOGIN.name());
    assertNotNull(bucket);
  }

  @Test
  void shouldUseXRealIpHeader() throws Exception {
    when(request.getMethod()).thenReturn("POST");
    when(request.getRequestURI()).thenReturn("/login");
    when(request.getHeader("X-Forwarded-For")).thenReturn(null);
    when(request.getHeader("X-Real-IP")).thenReturn("10.0.0.2");
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");

    rateLimitFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);

    Bucket bucket = rateLimitCache.getIfPresent("10.0.0.2:" + RateLimitedEndpoint.LOGIN.name());
    assertNotNull(bucket);
  }

  @Test
  void shouldSeparateBucketsByEndpoint() throws Exception {
    when(request.getMethod()).thenReturn("POST");
    when(request.getRemoteAddr()).thenReturn("192.168.1.3");

    // Fazer requisicoes para /login
    when(request.getRequestURI()).thenReturn("/login");
    rateLimitFilter.doFilterInternal(request, response, filterChain);

    // Fazer requisicoes para /auth
    when(request.getRequestURI()).thenReturn("/auth");
    rateLimitFilter.doFilterInternal(request, response, filterChain);

    // Verificar que existem buckets separados
    Bucket loginBucket =
        rateLimitCache.getIfPresent("192.168.1.3:" + RateLimitedEndpoint.LOGIN.name());
    Bucket authBucket =
        rateLimitCache.getIfPresent("192.168.1.3:" + RateLimitedEndpoint.AUTH.name());

    assertNotNull(loginBucket);
    assertNotNull(authBucket);
    assertNotSame(loginBucket, authBucket);
  }

  @Test
  void shouldSeparateBucketsByIP() throws Exception {
    when(request.getMethod()).thenReturn("POST");
    when(request.getRequestURI()).thenReturn("/login");

    // Fazer requisicao do IP 1
    when(request.getRemoteAddr()).thenReturn("192.168.1.10");
    rateLimitFilter.doFilterInternal(request, response, filterChain);

    // Fazer requisicao do IP 2
    when(request.getRemoteAddr()).thenReturn("192.168.1.11");
    rateLimitFilter.doFilterInternal(request, response, filterChain);

    // Verificar que existem buckets separados por IP
    Bucket bucket1 =
        rateLimitCache.getIfPresent("192.168.1.10:" + RateLimitedEndpoint.LOGIN.name());
    Bucket bucket2 =
        rateLimitCache.getIfPresent("192.168.1.11:" + RateLimitedEndpoint.LOGIN.name());

    assertNotNull(bucket1);
    assertNotNull(bucket2);
    assertNotSame(bucket1, bucket2);
  }

  @Test
  void shouldIncludeRetryAfterHeader() throws Exception {
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);
    when(request.getMethod()).thenReturn("POST");
    when(request.getRequestURI()).thenReturn("/login");
    when(request.getRemoteAddr()).thenReturn("192.168.1.20");

    // Consumir todos os tokens
    for (int i = 0; i < 3; i++) {
      rateLimitFilter.doFilterInternal(request, response, filterChain);
    }

    stringWriter = new StringWriter();
    writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    // Proxima requisicao deve incluir Retry-After
    rateLimitFilter.doFilterInternal(request, response, filterChain);

    verify(response).setHeader(eq("Retry-After"), anyString());
  }

  @Test
  void rateLimitedEndpointFromPathShouldReturnCorrectEndpoint() {
    assertEquals(RateLimitedEndpoint.LOGIN, RateLimitedEndpoint.fromPath("/login"));
    assertEquals(RateLimitedEndpoint.AUTH, RateLimitedEndpoint.fromPath("/auth"));
    assertEquals(RateLimitedEndpoint.NEW_USER, RateLimitedEndpoint.fromPath("/usuario/new-user"));
    assertEquals(
        RateLimitedEndpoint.PASSWORD_RESET_REQUEST,
        RateLimitedEndpoint.fromPath("/usuario/request-code-reset-password"));
    assertNull(RateLimitedEndpoint.fromPath("/unknown"));
    assertNull(RateLimitedEndpoint.fromPath(null));
  }

  @Test
  void rateLimitConfigShouldCreateBucketWithCorrectCapacity() {
    RateLimitProperties.EndpointLimit limit = new RateLimitProperties.EndpointLimit(5, 15);
    Bucket bucket = rateLimitConfig.createBucket(limit);

    assertNotNull(bucket);
    // Bucket deve ter capacidade de 5 tokens
    assertTrue(bucket.tryConsume(5));
    assertFalse(bucket.tryConsume(1)); // 6o token deve falhar
  }
}
