package br.com.utfpr.gerenciamento.server.config;

import br.com.utfpr.gerenciamento.server.util.TraceIdUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro que gera um traceId unico no inicio de cada requisicao.
 *
 * <p>O traceId e armazenado no MDC (Mapped Diagnostic Context) e adicionado ao header X-Trace-Id da
 * resposta HTTP, permitindo correlacao entre requisicoes cliente e logs do servidor.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

  public static final String TRACE_ID_HEADER = "X-Trace-Id";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      String traceId = TraceIdUtil.getOrCreateTraceId();
      response.setHeader(TRACE_ID_HEADER, traceId);
      filterChain.doFilter(request, response);
    } finally {
      TraceIdUtil.clear();
    }
  }
}
