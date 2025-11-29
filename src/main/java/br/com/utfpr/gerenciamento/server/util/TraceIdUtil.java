package br.com.utfpr.gerenciamento.server.util;

import java.util.UUID;
import org.slf4j.MDC;

/**
 * Utilitario para geracao e gerenciamento de traceId para correlacao de erros.
 *
 * <p>Utiliza o MDC (Mapped Diagnostic Context) do SLF4J para armazenar o traceId de forma
 * thread-safe, permitindo que seja acessado em qualquer ponto da requisicao.
 */
public final class TraceIdUtil {

  public static final String TRACE_ID_KEY = "traceId";

  private TraceIdUtil() {}

  /**
   * Gera um novo traceId ou retorna o existente no MDC.
   *
   * @return traceId atual ou novo UUID se nao existir
   */
  public static String getOrCreateTraceId() {
    String traceId = MDC.get(TRACE_ID_KEY);
    if (traceId == null || traceId.isBlank()) {
      traceId = UUID.randomUUID().toString();
      MDC.put(TRACE_ID_KEY, traceId);
    }
    return traceId;
  }

  /**
   * Retorna o traceId atual do MDC, ou null se nao existir.
   *
   * @return traceId atual ou null
   */
  public static String getTraceId() {
    return MDC.get(TRACE_ID_KEY);
  }

  /** Limpa o traceId do MDC. Deve ser chamado ao final de cada requisicao. */
  public static void clear() {
    MDC.remove(TRACE_ID_KEY);
  }
}
