package br.com.utfpr.gerenciamento.server.util;

import java.time.Duration;

/**
 * Ticker falso para simular passagem de tempo em testes de cache. Permite testar expiração de cache
 * sem esperar o TTL real.
 *
 * <p>Uso:
 *
 * <pre>
 * TestTicker ticker = new TestTicker();
 * Cache cache = configureCache(ticker);
 * cache.put("key", "value");
 *
 * ticker.advance(Duration.ofMinutes(5)); // Simula 5 minutos
 * assertThat(cache.get("key")).isNull(); // Cache expirou
 * </pre>
 *
 * @author Rodrigo Izidoro
 * @since 2025-10-07
 */
public class TestTicker {
  private long nanos = 0;

  /** Retorna o tempo atual em nanosegundos. */
  public long read() {
    return nanos;
  }

  /**
   * Avança o tempo em uma duração especificada.
   *
   * @param duration duração para avançar o relógio
   */
  public void advance(Duration duration) {
    nanos += duration.toNanos();
  }

  /**
   * Avança o tempo em milissegundos.
   *
   * @param millis milissegundos para avançar
   */
  public void advanceMillis(long millis) {
    nanos += Duration.ofMillis(millis).toNanos();
  }

  /** Reinicia o ticker para zero. */
  public void reset() {
    nanos = 0;
  }
}
