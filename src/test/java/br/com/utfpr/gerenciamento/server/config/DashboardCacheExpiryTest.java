package br.com.utfpr.gerenciamento.server.config;

import static org.junit.jupiter.api.Assertions.*;

import br.com.utfpr.gerenciamento.server.util.TestTicker;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testes para DashboardCacheExpiry - Validação de TTL variável baseado em sufixo de chave.
 *
 * <p>IMPORTANTE: Testes usam valores fixos e constantes para evitar flakiness. Não dependem de
 * System.currentTimeMillis() ou LocalDate.now().
 *
 * @author Rodrigo Izidoro
 * @since 2025-10-07
 */
class DashboardCacheExpiryTest {

  private DashboardCacheExpiry expiry;

  // Constantes para TTL em nanosegundos (valores fixos, não dependem de tempo do sistema)
  private static final long TTL_HISTORICAL_NANOS = Duration.ofHours(6).toNanos();
  private static final long TTL_CURRENT_NANOS = Duration.ofMinutes(5).toNanos();

  @BeforeEach
  void setUp() {
    expiry = new DashboardCacheExpiry();
  }

  @Test
  @DisplayName("expireAfterCreate deve retornar 6 horas para chave com sufixo _HISTORICAL")
  void testExpireAfterCreate_QuandoChaveHistorical_DeveRetornar6Horas() {
    // Given
    String chaveHistorica = "findDadosEmprestimoCountRange_2024-01-01_2024-12-31_HISTORICAL";
    Object valor = "dados";
    long currentTime = 0L; // Tempo fixo para determinismo

    // When
    long ttl = expiry.expireAfterCreate(chaveHistorica, valor, currentTime);

    // Then
    assertEquals(
        TTL_HISTORICAL_NANOS,
        ttl,
        "Chaves com sufixo _HISTORICAL devem ter TTL de 6 horas (em nanosegundos)");
  }

  @Test
  @DisplayName("expireAfterCreate deve retornar 5 minutos para chave com sufixo _CURRENT")
  void testExpireAfterCreate_QuandoChaveCurrent_DeveRetornar5Minutos() {
    // Given
    String chaveAtual = "findDadosEmprestimoCountRange_2025-01-01_2025-10-07_CURRENT";
    Object valor = "dados";
    long currentTime = 0L;

    // When
    long ttl = expiry.expireAfterCreate(chaveAtual, valor, currentTime);

    // Then
    assertEquals(
        TTL_CURRENT_NANOS,
        ttl,
        "Chaves com sufixo _CURRENT devem ter TTL de 5 minutos (em nanosegundos)");
  }

  @Test
  @DisplayName("expireAfterCreate deve retornar 5 minutos para chave sem sufixo (fallback)")
  void testExpireAfterCreate_QuandoChaveSemSufixo_DeveRetornarTTLPadrao() {
    // Given
    String chaveSemSufixo = "findDadosEmprestimoCountRange_2025-01-01_2025-10-07";
    Object valor = "dados";
    long currentTime = 0L;

    // When
    long ttl = expiry.expireAfterCreate(chaveSemSufixo, valor, currentTime);

    // Then
    assertEquals(
        TTL_CURRENT_NANOS, ttl, "Chaves sem sufixo devem usar TTL padrão de 5 minutos (fallback)");
  }

  @Test
  @DisplayName("expireAfterCreate deve retornar 5 minutos para chave null (proteção)")
  void testExpireAfterCreate_QuandoChaveNull_DeveRetornarTTLPadrao() {
    // Given
    Object chaveNull = null;
    Object valor = "dados";
    long currentTime = 0L;

    // When
    long ttl = expiry.expireAfterCreate(chaveNull, valor, currentTime);

    // Then
    assertEquals(
        TTL_CURRENT_NANOS, ttl, "Chaves null devem usar TTL padrão de 5 minutos (proteção)");
  }

  @Test
  @DisplayName("expireAfterUpdate deve retornar mesmo TTL que expireAfterCreate para _HISTORICAL")
  void testExpireAfterUpdate_QuandoHistorical_DeveRetornar6Horas() {
    // Given
    String chaveHistorica = "metodoDashboard_param1_param2_HISTORICAL";
    Object valor = "dados atualizados";
    long currentTime = 0L;
    long currentDuration = Duration.ofMinutes(30).toNanos(); // Duração arbitrária atual

    // When
    long ttl = expiry.expireAfterUpdate(chaveHistorica, valor, currentTime, currentDuration);

    // Then
    assertEquals(
        TTL_HISTORICAL_NANOS,
        ttl,
        "expireAfterUpdate deve reaplicar TTL de 6h para chaves _HISTORICAL");
  }

  @Test
  @DisplayName("expireAfterUpdate deve retornar mesmo TTL que expireAfterCreate para _CURRENT")
  void testExpireAfterUpdate_QuandoCurrent_DeveRetornar5Minutos() {
    // Given
    String chaveAtual = "metodoDashboard_param1_param2_CURRENT";
    Object valor = "dados atualizados";
    long currentTime = 0L;
    long currentDuration = Duration.ofMinutes(2).toNanos();

    // When
    long ttl = expiry.expireAfterUpdate(chaveAtual, valor, currentTime, currentDuration);

    // Then
    assertEquals(
        TTL_CURRENT_NANOS,
        ttl,
        "expireAfterUpdate deve reaplicar TTL de 5min para chaves _CURRENT");
  }

  @Test
  @DisplayName("expireAfterRead NÃO deve alterar TTL - deve retornar currentDuration inalterado")
  void testExpireAfterRead_QuandoChamado_DeveManterDuracaoOriginal() {
    // Given
    String chaveQualquer = "chave_HISTORICAL";
    Object valor = "dados";
    long currentTime = 0L;
    long currentDuration = Duration.ofHours(3).toNanos(); // 3 horas restantes

    // When
    long ttlAposLeitura =
        expiry.expireAfterRead(chaveQualquer, valor, currentTime, currentDuration);

    // Then
    assertEquals(
        currentDuration,
        ttlAposLeitura,
        "expireAfterRead NÃO deve modificar o TTL, apenas retornar currentDuration");
  }

  @Test
  @DisplayName(
      "Integração: Cache com _HISTORICAL deve expirar após 6 horas (usando TestTicker determinístico)")
  void testIntegracaoCache_QuandoHistorical_DeveExpirarApos6Horas() {
    // Given - Ticker mockado para controle determinístico do tempo
    TestTicker ticker = new TestTicker();

    Cache<String, String> cache =
        Caffeine.newBuilder().expireAfter(new DashboardCacheExpiry()).ticker(ticker::read).build();

    String chaveHistorica = "dashboard_2024-01-01_2024-12-31_HISTORICAL";

    // When - Adicionar entrada ao cache
    cache.put(chaveHistorica, "dados históricos");
    assertNotNull(cache.getIfPresent(chaveHistorica), "Entrada deve estar presente imediatamente");

    // Then - Avançar 5h59min: entrada ainda deve estar presente
    ticker.advance(Duration.ofHours(5).plusMinutes(59));
    cache.cleanUp();
    assertNotNull(
        cache.getIfPresent(chaveHistorica),
        "Entrada _HISTORICAL deve permanecer após 5h59min (antes de 6h)");

    // Avançar mais 2 minutos (total: 6h1min): entrada deve ter expirado
    ticker.advance(Duration.ofMinutes(2));
    cache.cleanUp();
    assertNull(
        cache.getIfPresent(chaveHistorica),
        "Entrada _HISTORICAL deve expirar após 6 horas (6h1min)");
  }

  @Test
  @DisplayName(
      "Integração: Cache com _CURRENT deve expirar após 5 minutos (usando TestTicker determinístico)")
  void testIntegracaoCache_QuandoCurrent_DeveExpirarApos5Minutos() {
    // Given - Ticker mockado para controle determinístico do tempo
    TestTicker ticker = new TestTicker();

    Cache<String, String> cache =
        Caffeine.newBuilder().expireAfter(new DashboardCacheExpiry()).ticker(ticker::read).build();

    String chaveAtual = "dashboard_2025-01-01_2025-10-07_CURRENT";

    // When - Adicionar entrada ao cache
    cache.put(chaveAtual, "dados atuais");
    assertNotNull(cache.getIfPresent(chaveAtual), "Entrada deve estar presente imediatamente");

    // Then - Avançar 4min59seg: entrada ainda deve estar presente
    ticker.advance(Duration.ofMinutes(4).plusSeconds(59));
    cache.cleanUp();
    assertNotNull(
        cache.getIfPresent(chaveAtual),
        "Entrada _CURRENT deve permanecer após 4min59s (antes de 5min)");

    // Avançar mais 2 segundos (total: 5min1s): entrada deve ter expirado
    ticker.advance(Duration.ofSeconds(2));
    cache.cleanUp();
    assertNull(
        cache.getIfPresent(chaveAtual), "Entrada _CURRENT deve expirar após 5 minutos (5min1s)");
  }

  @Test
  @DisplayName("Integração: Cache sem sufixo deve usar TTL padrão de 5 minutos (usando TestTicker)")
  void testIntegracaoCache_QuandoSemSufixo_DeveUsarTTLPadrao5Minutos() {
    // Given
    TestTicker ticker = new TestTicker();

    Cache<String, String> cache =
        Caffeine.newBuilder().expireAfter(new DashboardCacheExpiry()).ticker(ticker::read).build();

    String chaveSemSufixo = "dashboard_2025-01-01_2025-10-07";

    // When
    cache.put(chaveSemSufixo, "dados sem sufixo");
    assertNotNull(cache.getIfPresent(chaveSemSufixo));

    // Then - Avançar 4min: ainda presente
    ticker.advance(Duration.ofMinutes(4));
    cache.cleanUp();
    assertNotNull(
        cache.getIfPresent(chaveSemSufixo), "Entrada sem sufixo deve permanecer após 4min");

    // Avançar mais 2min (total: 6min): expirado
    ticker.advance(Duration.ofMinutes(2));
    cache.cleanUp();
    assertNull(
        cache.getIfPresent(chaveSemSufixo),
        "Entrada sem sufixo deve expirar após 5 minutos (fallback)");
  }

  @Test
  @DisplayName("Diferentes chaves no mesmo cache devem ter TTLs independentes baseados no sufixo")
  void testIntegracaoCache_QuandoMultiplasChaves_DevemTerTTLsIndependentes() {
    // Given
    TestTicker ticker = new TestTicker();

    Cache<String, String> cache =
        Caffeine.newBuilder().expireAfter(new DashboardCacheExpiry()).ticker(ticker::read).build();

    String chaveHistorica = "query1_HISTORICAL";
    String chaveAtual = "query2_CURRENT";

    // When - Adicionar ambas as chaves
    cache.put(chaveHistorica, "dados históricos");
    cache.put(chaveAtual, "dados atuais");

    // Then - Avançar 6 minutos
    ticker.advance(Duration.ofMinutes(6));
    cache.cleanUp();

    // Chave _CURRENT deve ter expirado (TTL: 5min)
    assertNull(
        cache.getIfPresent(chaveAtual), "Chave _CURRENT deve expirar após 6 minutos (TTL: 5min)");

    // Chave _HISTORICAL ainda deve estar presente (TTL: 6h)
    assertNotNull(
        cache.getIfPresent(chaveHistorica),
        "Chave _HISTORICAL deve permanecer após 6 minutos (TTL: 6h)");

    // Avançar até 6 horas totais
    ticker.advance(Duration.ofHours(6));
    cache.cleanUp();

    // Agora ambas devem ter expirado
    assertNull(cache.getIfPresent(chaveHistorica), "Chave _HISTORICAL deve expirar após 6 horas");
  }
}
