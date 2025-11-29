package br.com.utfpr.gerenciamento.server.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

/** Testes unitarios para TraceIdUtil. */
class TraceIdUtilTest {

  @AfterEach
  void cleanup() {
    TraceIdUtil.clear();
  }

  @Test
  void getOrCreateTraceId_DeveGerarNovoUuidQuandoNaoExiste() {
    // Given
    assertNull(MDC.get(TraceIdUtil.TRACE_ID_KEY));

    // When
    String traceId = TraceIdUtil.getOrCreateTraceId();

    // Then
    assertNotNull(traceId);
    assertFalse(traceId.isBlank());
    assertDoesNotThrow(() -> UUID.fromString(traceId));
  }

  @Test
  void getOrCreateTraceId_DeveRetornarMesmoUuidQuandoJaExiste() {
    // Given
    String primeiroTraceId = TraceIdUtil.getOrCreateTraceId();

    // When
    String segundoTraceId = TraceIdUtil.getOrCreateTraceId();

    // Then
    assertEquals(primeiroTraceId, segundoTraceId);
  }

  @Test
  void getOrCreateTraceId_DeveArmazenarNoMdc() {
    // When
    String traceId = TraceIdUtil.getOrCreateTraceId();

    // Then
    assertEquals(traceId, MDC.get(TraceIdUtil.TRACE_ID_KEY));
  }

  @Test
  void getTraceId_DeveRetornarNullQuandoNaoExiste() {
    // Given
    assertNull(MDC.get(TraceIdUtil.TRACE_ID_KEY));

    // When
    String traceId = TraceIdUtil.getTraceId();

    // Then
    assertNull(traceId);
  }

  @Test
  void getTraceId_DeveRetornarValorQuandoExiste() {
    // Given
    String traceIdEsperado = TraceIdUtil.getOrCreateTraceId();

    // When
    String traceIdRetornado = TraceIdUtil.getTraceId();

    // Then
    assertEquals(traceIdEsperado, traceIdRetornado);
  }

  @Test
  void clear_DeveRemoverTraceIdDoMdc() {
    // Given
    TraceIdUtil.getOrCreateTraceId();
    assertNotNull(MDC.get(TraceIdUtil.TRACE_ID_KEY));

    // When
    TraceIdUtil.clear();

    // Then
    assertNull(MDC.get(TraceIdUtil.TRACE_ID_KEY));
  }

  @Test
  void getOrCreateTraceId_DeveGerarNovoAposClear() {
    // Given
    String primeiroTraceId = TraceIdUtil.getOrCreateTraceId();
    TraceIdUtil.clear();

    // When
    String novoTraceId = TraceIdUtil.getOrCreateTraceId();

    // Then
    assertNotNull(novoTraceId);
    assertNotEquals(primeiroTraceId, novoTraceId);
  }
}
