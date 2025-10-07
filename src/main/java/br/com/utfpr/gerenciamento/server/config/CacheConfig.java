package br.com.utfpr.gerenciamento.server.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração de cache em memória usando Caffeine.
 *
 * <p>Estratégia de caching otimizada para ambiente de baixo tráfego: - Dados de referência (Cidade,
 * Estado, Pais): TTL longo (6 horas) pois raramente mudam - Dashboard: TTL dinâmico baseado na data
 * (implementado via custom key generator) - Usuario: TTL médio (15 minutos) para autenticação
 *
 * @author Rodrigo Izidoro
 * @since 2025-10-07
 */
@Configuration
@EnableCaching
public class CacheConfig {

  /**
   * Cache manager principal com configuração padrão do Caffeine.
   *
   * <p>Configuração base: - Maximum size: 1000 entradas por cache - TTL padrão: 1 hora - Eviction:
   * LRU automático
   */
  @Bean
  public CacheManager cacheManager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager();

    // Configuração padrão para todos os caches
    cacheManager.setCaffeine(
        Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofHours(1))
            .recordStats()); // Habilita métricas para monitoramento

    return cacheManager;
  }

  /**
   * Cache dedicado para dados de referência geográfica (Cidade, Estado, Pais).
   *
   * <p>Características: - TTL: 6 horas (dados raramente mudam) - Tamanho: 10.000 entradas (5570
   * cidades + 27 estados + países) - Uso: Cidades e Estados brasileiros são estáticos
   *
   * <p>Nota: Brasil possui 5.570 municípios e 27 estados que raramente são adicionados no sistema.
   */
  @Bean
  public Caffeine<Object, Object> caffeineConfigReferenceData() {
    return Caffeine.newBuilder()
        .maximumSize(10000) // Comporta todas cidades + estados + países
        .expireAfterWrite(Duration.ofHours(6)) // TTL agressivo para dados estáticos
        .recordStats();
  }

  /**
   * Cache para dados de usuário (autenticação e queries gerais).
   *
   * <p>TTL médio pois dados podem mudar (perfil, permissões, etc).
   */
  @Bean
  public Caffeine<Object, Object> caffeineConfigUsuario() {
    return Caffeine.newBuilder()
        .maximumSize(500) // Usuários ativos
        .expireAfterWrite(Duration.ofMinutes(15))
        .recordStats();
  }

  /**
   * Cache para dashboard com TTL curto.
   *
   * <p>TTL curto porque: - Data atual: Dados podem mudar ao longo do dia (novos empréstimos) -
   * Datas passadas: Dados históricos não mudam (ver DashboardCacheKeyGenerator)
   *
   * <p>Nota: Para queries de datas passadas, o DashboardCacheKeyGenerator aplica TTL mais longo.
   */
  @Bean
  public Caffeine<Object, Object> caffeineConfigDashboard() {
    return Caffeine.newBuilder()
        .maximumSize(200) // Combinações de filtros de data
        .expireAfterWrite(Duration.ofMinutes(5)) // TTL base curto para dados recentes
        .recordStats();
  }
}
