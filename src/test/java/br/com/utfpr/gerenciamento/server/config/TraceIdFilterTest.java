package br.com.utfpr.gerenciamento.server.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.util.TraceIdUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/** Testes unitarios para TraceIdFilter. */
@ExtendWith(MockitoExtension.class)
class TraceIdFilterTest {

  private TraceIdFilter traceIdFilter;

  @Mock private FilterChain filterChain;

  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  @BeforeEach
  void setUp() {
    traceIdFilter = new TraceIdFilter();
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
  }

  @AfterEach
  void cleanup() {
    TraceIdUtil.clear();
  }

  @Test
  void doFilterInternal_DeveAdicionarHeaderXTraceId() throws ServletException, IOException {
    // When
    traceIdFilter.doFilterInternal(request, response, filterChain);

    // Then
    String traceIdHeader = response.getHeader(TraceIdFilter.TRACE_ID_HEADER);
    assertNotNull(traceIdHeader);
    assertFalse(traceIdHeader.isBlank());
  }

  @Test
  void doFilterInternal_DeveChamarFilterChain() throws ServletException, IOException {
    // When
    traceIdFilter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilterInternal_DeveLimparMdcAposRequisicao() throws ServletException, IOException {
    // When
    traceIdFilter.doFilterInternal(request, response, filterChain);

    // Then
    assertNull(TraceIdUtil.getTraceId());
  }

  @Test
  void doFilterInternal_DeveLimparMdcMesmoComExcecao() throws ServletException, IOException {
    // Given
    doThrow(new ServletException("Erro simulado")).when(filterChain).doFilter(request, response);

    // When & Then
    assertThrows(
        ServletException.class,
        () -> traceIdFilter.doFilterInternal(request, response, filterChain));
    assertNull(TraceIdUtil.getTraceId());
  }

  @Test
  void doFilterInternal_TraceIdDeveSerUuidValido() throws ServletException, IOException {
    // When
    traceIdFilter.doFilterInternal(request, response, filterChain);

    // Then
    String traceIdHeader = response.getHeader(TraceIdFilter.TRACE_ID_HEADER);
    assertDoesNotThrow(
        () -> java.util.UUID.fromString(traceIdHeader), "TraceId deve ser um UUID valido");
  }

  @Test
  void headerConstante_DeveSerXTraceId() {
    // Then
    assertEquals("X-Trace-Id", TraceIdFilter.TRACE_ID_HEADER);
  }
}
