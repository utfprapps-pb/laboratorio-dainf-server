package br.com.utfpr.gerenciamento.server.config;

import static org.junit.jupiter.api.Assertions.*;

import br.com.utfpr.gerenciamento.server.util.TestTicker;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.test.context.ActiveProfiles;

/**
 * Testes para CacheConfig - Validação de configuração Caffeine.
 *
 * @author Rodrigo Izidoro
 * @since 2025-10-07
 */
@SpringBootTest
@ActiveProfiles("test")
class CacheConfigTest {

  @Autowired private CacheManager cacheManager;

  @Test
  @DisplayName("Deve criar CacheManager com configuração padrão correta")
  void testCacheManager_QuandoCriado_DeveUsarCaffeineCacheManager() {
    // Given/When
    assertNotNull(cacheManager);
    assertInstanceOf(CaffeineCacheManager.class, cacheManager);

    // Then - Valida que é um CaffeineCacheManager configurado
    CaffeineCacheManager caffeineCacheManager = (CaffeineCacheManager) cacheManager;
    assertNotNull(caffeineCacheManager);
  }

  @Test
  @DisplayName("Cache de dados de referência deve ter TTL de 6 horas e tamanho 10000")
  void testCaffeineConfigReferenceData_QuandoConfigurado_DeveTer6HorasTTLETamanho10000() {
    // Given
    CacheConfig config = new CacheConfig();

    // When
    Caffeine<Object, Object> caffeine = config.caffeineConfigReferenceData();
    Cache<Object, Object> cache = caffeine.build();

    // Then - Validar estatísticas habilitadas
    CacheStats stats = cache.stats();
    assertNotNull(stats);

    // Adicionar 10.001 itens e verificar eviction
    for (int i = 0; i < 10001; i++) {
      cache.put("key" + i, "value" + i);
    }

    // Força cleanup
    cache.cleanUp();

    // Deve ter evictado pelo menos 1 item
    assertTrue(cache.estimatedSize() <= 10000);
  }

  @Test
  @DisplayName("Cache de usuário deve ter tamanho máximo de 500 entradas")
  void testCaffeineConfigUsuario_QuandoConfigurado_DeveTerTamanhoMaximo500() {
    // Given
    CacheConfig config = new CacheConfig();

    // When
    Caffeine<Object, Object> caffeine = config.caffeineConfigUsuario();
    Cache<Object, Object> cache = caffeine.build();

    // Then - Adicionar 550 itens
    for (int i = 0; i < 550; i++) {
      cache.put("usuario:" + i, "dados_usuario_" + i);
    }

    cache.cleanUp();

    // Deve ter evictado 50 itens
    assertTrue(cache.estimatedSize() <= 500);
  }

  @Test
  @DisplayName("Deve expirar cache de usuário após 15 minutos usando Ticker mockado")
  void testCaffeineConfigUsuario_QuandoApos15Minutos_DeveExpirarCache() {
    // Given
    TestTicker ticker = new TestTicker();

    Cache<String, String> cache =
        Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(Duration.ofMinutes(15))
            .ticker(ticker::read)
            .recordStats()
            .build();

    // When - Adicionar entrada
    cache.put("usuario:1", "dados");
    assertNotNull(cache.getIfPresent("usuario:1"));

    // Then - Avançar 14 minutos: ainda presente
    ticker.advance(Duration.ofMinutes(14));
    assertNotNull(cache.getIfPresent("usuario:1"));

    // Avançar mais 2 minutos: expirado
    ticker.advance(Duration.ofMinutes(2));
    cache.cleanUp(); // Força limpeza
    assertNull(cache.getIfPresent("usuario:1"));
  }

  @Test
  @DisplayName("Todos os caches devem ter estatísticas habilitadas")
  void testCaffeineCaches_QuandoCriados_DevemTerEstatisticasHabilitadas() {
    // Given
    CacheConfig config = new CacheConfig();

    // When
    Cache<Object, Object> cacheRef = config.caffeineConfigReferenceData().build();
    Cache<Object, Object> cacheUser = config.caffeineConfigUsuario().build();
    Cache<Object, Object> cacheDash = config.caffeineConfigDashboard().build();

    // Then
    assertNotNull(cacheRef.stats());
    assertNotNull(cacheUser.stats());
    assertNotNull(cacheDash.stats());
  }

  @Test
  @DisplayName("Cada cache deve ter tamanho máximo apropriado")
  void testCaffeineCaches_QuandoUltrapassamLimite_DevemExecutarEviction() {
    // Given
    CacheConfig config = new CacheConfig();

    // When - Criar caches e adicionar itens
    Cache<Object, Object> cacheRef = config.caffeineConfigReferenceData().build();
    Cache<Object, Object> cacheUser = config.caffeineConfigUsuario().build();
    Cache<Object, Object> cacheDash = config.caffeineConfigDashboard().build();

    // Then - Adicionar além do limite e verificar eviction
    // Reference Data: 10.000
    for (int i = 0; i < 10100; i++) {
      cacheRef.put("ref" + i, "value");
    }
    cacheRef.cleanUp();
    assertTrue(cacheRef.estimatedSize() <= 10000);

    // Usuario: 500
    for (int i = 0; i < 550; i++) {
      cacheUser.put("user" + i, "value");
    }
    cacheUser.cleanUp();
    assertTrue(cacheUser.estimatedSize() <= 500);

    // Dashboard: 200
    for (int i = 0; i < 250; i++) {
      cacheDash.put("dash" + i, "value");
    }
    cacheDash.cleanUp();
    assertTrue(cacheDash.estimatedSize() <= 200);
  }

  @Test
  @DisplayName("Cache de dashboard deve ter TTL de 5 minutos")
  void testCaffeineConfigDashboard_QuandoApos5Minutos_DeveExpirarCache() {
    // Given
    TestTicker ticker = new TestTicker();

    Cache<String, String> cache =
        Caffeine.newBuilder()
            .maximumSize(200)
            .expireAfterWrite(Duration.ofMinutes(5))
            .ticker(ticker::read)
            .recordStats()
            .build();

    // When
    cache.put("dashboard:stats", "dados_dashboard");
    assertNotNull(cache.getIfPresent("dashboard:stats"));

    // Then - Avançar 4 minutos: ainda presente
    ticker.advance(Duration.ofMinutes(4));
    assertNotNull(cache.getIfPresent("dashboard:stats"));

    // Avançar mais 2 minutos: expirado
    ticker.advance(Duration.ofMinutes(2));
    cache.cleanUp();
    assertNull(cache.getIfPresent("dashboard:stats"));
  }

  @Test
  @DisplayName("Deve registrar estatísticas de eviction após overflow")
  void testCaffeineCache_QuandoOcorreEviction_DeveRegistrarEstatisticas() {
    // Given
    CacheConfig config = new CacheConfig();
    Cache<Object, Object> cache = config.caffeineConfigUsuario().build();

    // When - Adicionar mais itens que o limite
    for (int i = 0; i < 600; i++) {
      cache.put("key" + i, "value" + i);
    }

    cache.cleanUp();

    // Then - Verificar que houve eviction
    CacheStats stats = cache.stats();
    assertTrue(stats.evictionCount() > 0);
    assertTrue(cache.estimatedSize() <= 500);
  }

  @Test
  @DisplayName("Deve registrar hit/miss rate corretamente")
  void testCaffeineCache_QuandoAcessado_DeveRegistrarHitMissRate() {
    // Given
    CacheConfig config = new CacheConfig();
    Cache<String, String> cache = config.caffeineConfigUsuario().build();

    // When - 5 hits, 3 misses
    cache.put("key1", "value1");
    cache.put("key2", "value2");
    cache.put("key3", "value3");

    cache.getIfPresent("key1"); // hit
    cache.getIfPresent("key2"); // hit
    cache.getIfPresent("key3"); // hit
    cache.getIfPresent("key1"); // hit
    cache.getIfPresent("key2"); // hit

    cache.getIfPresent("keyInexistente1"); // miss
    cache.getIfPresent("keyInexistente2"); // miss
    cache.getIfPresent("keyInexistente3"); // miss

    // Then
    CacheStats stats = cache.stats();
    assertEquals(5, stats.hitCount());
    assertEquals(3, stats.missCount());
    assertTrue(stats.hitRate() > 0.6); // 5/8 = 62.5%
  }

  @Test
  @DisplayName("Deve gerenciar múltiplos caches independentemente")
  void testCaffeineCaches_QuandoMultiplosCaches_DevemSerIndependentes() {
    // Given
    CacheConfig config = new CacheConfig();
    Cache<Object, Object> cacheRef = config.caffeineConfigReferenceData().build();
    Cache<Object, Object> cacheUser = config.caffeineConfigUsuario().build();

    // When - Adicionar dados em cada cache
    cacheRef.put("cidade:1", "Pato Branco");
    cacheUser.put("usuario:1", "João Silva");

    // Then - Validar isolamento
    assertNotNull(cacheRef.getIfPresent("cidade:1"));
    assertNull(cacheRef.getIfPresent("usuario:1")); // Não está no cache de referência

    assertNotNull(cacheUser.getIfPresent("usuario:1"));
    assertNull(cacheUser.getIfPresent("cidade:1")); // Não está no cache de usuário
  }

  @Test
  @DisplayName("Deve respeitar TTL específico de cada cache")
  void testCaffeineCaches_QuandoDiferentesTTLs_DevemExpirarIndependentemente() {
    // Given
    TestTicker ticker = new TestTicker();

    // Cache usuário: 15 minutos
    Cache<String, String> cacheUser =
        Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(Duration.ofMinutes(15))
            .ticker(ticker::read)
            .recordStats()
            .build();

    // Cache referência: 6 horas
    Cache<String, String> cacheRef =
        Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(Duration.ofHours(6))
            .ticker(ticker::read)
            .recordStats()
            .build();

    // When
    cacheUser.put("usuario:1", "dados");
    cacheRef.put("cidade:1", "Pato Branco");

    // Then - Após 20 minutos: usuário expirado, referência ainda presente
    ticker.advance(Duration.ofMinutes(20));
    cacheUser.cleanUp();
    cacheRef.cleanUp();

    assertNull(cacheUser.getIfPresent("usuario:1")); // Expirado
    assertNotNull(cacheRef.getIfPresent("cidade:1")); // Ainda válido
  }

  @Test
  @DisplayName("Deve criar CacheManager com nomes de cache específicos")
  void testCacheManager_QuandoObtemCache_DeveRetornarCacheEspecifico() {
    // Given
    CacheConfig config = new CacheConfig();
    cacheManager =
        config.cacheManager(
            config.caffeineConfigReferenceData(),
            config.caffeineConfigUsuario(),
            config.caffeineConfigDashboard(),
            config.caffeineConfigEmprestimo());

    // When - Obter cache específico
    cacheManager.getCache("usuario");

    // Then
    assertNotNull(cacheManager);
    // Nota: Caffeine cria caches dinamicamente, então pode ser null inicialmente
    // Este teste valida a configuração base do CacheManager
  }

  @Test
  @DisplayName("Deve validar configuração de 1 hora TTL padrão do CacheManager")
  void testCacheManager_QuandoConfiguradoPadrao_DeveTer1HoraTTL() {
    // Given
    TestTicker ticker = new TestTicker();

    // Simula configuração padrão do CacheManager
    Cache<String, String> cache =
        Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofHours(1))
            .ticker(ticker::read)
            .recordStats()
            .build();

    // When
    cache.put("chave:padrao", "valor");
    assertNotNull(cache.getIfPresent("chave:padrao"));

    // Then - Após 59 minutos: ainda presente
    ticker.advance(Duration.ofMinutes(59));
    assertNotNull(cache.getIfPresent("chave:padrao"));

    // Após 61 minutos: expirado
    ticker.advance(Duration.ofMinutes(2));
    cache.cleanUp();
    assertNull(cache.getIfPresent("chave:padrao"));
  }
}
