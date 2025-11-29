package br.com.utfpr.gerenciamento.server.security;

import br.com.utfpr.gerenciamento.server.util.TraceIdUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;

/**
 * Utilitario para escrever respostas ProblemDetail em filtros de seguranca.
 *
 * <p>Como os filtros de seguranca (JWT) executam antes do Spring MVC, nao temos acesso ao mecanismo
 * padrao de tratamento de excecoes. Esta classe fornece metodos para escrever respostas RFC 7807
 * diretamente no HttpServletResponse.
 */
public final class ProblemDetailResponseWriter {

  private static final ObjectMapper MAPPER =
      new ObjectMapper().registerModule(new JavaTimeModule());

  private static final String TRACE_ID_PROPERTY = "traceId";
  private static final String TIMESTAMP_PROPERTY = "timestamp";

  private ProblemDetailResponseWriter() {}

  /** Escreve um ProblemDetail na resposta HTTP. */
  public static void writeProblemDetail(
      HttpServletResponse response, HttpStatus status, String title, String detail, String typeUri)
      throws IOException {

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
    problemDetail.setTitle(title);
    problemDetail.setType(URI.create(typeUri));
    problemDetail.setProperty(TRACE_ID_PROPERTY, TraceIdUtil.getOrCreateTraceId());
    problemDetail.setProperty(TIMESTAMP_PROPERTY, Instant.now().toString());

    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    MAPPER.writeValue(response.getWriter(), problemDetail);
  }

  /** Escreve erro de autenticacao (401 Unauthorized). */
  public static void writeUnauthorized(HttpServletResponse response, String detail)
      throws IOException {
    writeProblemDetail(
        response,
        HttpStatus.UNAUTHORIZED,
        "Falha na autenticacao",
        detail,
        "/errors/autenticacao-falhou");
  }

  /** Escreve erro de credenciais invalidas (401 Unauthorized). */
  public static void writeBadCredentials(HttpServletResponse response) throws IOException {
    writeProblemDetail(
        response,
        HttpStatus.UNAUTHORIZED,
        "Credenciais invalidas",
        "Usuario ou senha incorretos.",
        "/errors/credenciais-invalidas");
  }

  /** Escreve erro de token expirado (401 Unauthorized). */
  public static void writeTokenExpired(HttpServletResponse response) throws IOException {
    writeProblemDetail(
        response,
        HttpStatus.UNAUTHORIZED,
        "Token expirado",
        "Sua sessao expirou. Por favor, faca login novamente.",
        "/errors/token-expirado");
  }

  /** Escreve erro de token invalido (401 Unauthorized). */
  public static void writeTokenInvalid(HttpServletResponse response) throws IOException {
    writeProblemDetail(
        response,
        HttpStatus.UNAUTHORIZED,
        "Token invalido",
        "Token de autenticacao invalido.",
        "/errors/token-invalido");
  }

  /** Escreve erro de precondicao (428 Precondition Required). */
  public static void writePreconditionRequired(HttpServletResponse response, String detail)
      throws IOException {
    writeProblemDetail(
        response,
        HttpStatus.valueOf(428),
        "Precondicao requerida",
        detail,
        "/errors/precondicao-requerida");
  }

  /** Escreve erro de conta desabilitada (403 Forbidden). */
  public static void writeForbiddenDisabled(HttpServletResponse response, String detail)
      throws IOException {
    writeProblemDetail(
        response, HttpStatus.FORBIDDEN, "Conta desabilitada", detail, "/errors/conta-desabilitada");
  }

  /** Escreve erro de acesso negado (403 Forbidden). */
  public static void writeAccessDenied(HttpServletResponse response) throws IOException {
    writeProblemDetail(
        response,
        HttpStatus.FORBIDDEN,
        "Acesso negado",
        "Voce nao tem permissao para acessar este recurso.",
        "/errors/acesso-negado");
  }
}
