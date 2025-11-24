package br.com.utfpr.gerenciamento.server.config;

import com.github.benmanes.caffeine.cache.Expiry;
import java.time.Duration;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Expiry customizado para cache de Dashboard com TTL variável baseado no tipo de dados.
 *
 * <p>Estratégia de expiração baseada no sufixo da chave: - Chaves com "_HISTORICAL": TTL longo (6
 * horas) - dados históricos não mudam - Chaves com "_CURRENT": TTL curto (5 minutos) - dados podem
 * mudar ao longo do dia - Chaves sem sufixo: TTL padrão (5 minutos)
 *
 * <p>Este Expiry trabalha em conjunto com DashboardCacheKeyGenerator que adiciona os sufixos às
 * chaves.
 *
 * @author Rodrigo Izidoro
 * @since 2025-10-07
 */
public class DashboardCacheExpiry implements Expiry<Object, Object> {

  private static final long TTL_HISTORICAL_NANOS = Duration.ofHours(6).toNanos();
  private static final long TTL_CURRENT_NANOS = Duration.ofMinutes(5).toNanos();
  private static final String HISTORICAL_SUFFIX = "_HISTORICAL";
  private static final String CURRENT_SUFFIX = "_CURRENT";

  @Override
  public long expireAfterCreate(@NonNull Object key, @NonNull Object value, long currentTime) {
    return getTtlForKey(key);
  }

  @Override
  public long expireAfterUpdate(
      @NonNull Object key,
      @NonNull Object value,
      long currentTime,
      @NonNegative long currentDuration) {
    return getTtlForKey(key);
  }

  @Override
  public long expireAfterRead(
      @NonNull Object key,
      @NonNull Object value,
      long currentTime,
      @NonNegative long currentDuration) {
    // Não altera o TTL na leitura, mantém o original
    return currentDuration;
  }

  /**
   * Determina o TTL baseado no sufixo da chave.
   *
   * @param key Chave do cache (geralmente String com sufixo _HISTORICAL ou _CURRENT)
   * @return TTL em nanosegundos
   */
  private long getTtlForKey(Object key) {
    if (key == null) {
      return TTL_CURRENT_NANOS;
    }

    String keyStr = key.toString();

    if (keyStr.endsWith(HISTORICAL_SUFFIX)) {
      return TTL_HISTORICAL_NANOS;
    } else if (keyStr.endsWith(CURRENT_SUFFIX)) {
      return TTL_CURRENT_NANOS;
    } else {
      // Fallback para chaves sem sufixo
      return TTL_CURRENT_NANOS;
    }
  }
}
