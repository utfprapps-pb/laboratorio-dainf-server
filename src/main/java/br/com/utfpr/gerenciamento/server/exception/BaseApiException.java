package br.com.utfpr.gerenciamento.server.exception;

import br.com.utfpr.gerenciamento.server.util.TraceIdUtil;
import java.net.URI;
import java.time.Instant;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * Classe base para excecoes da API que seguem o padrao RFC 9457 (Problem Details).
 *
 * <p>Todas as excecoes de dominio devem estender esta classe para garantir respostas de erro
 * padronizadas com traceId e timestamp.
 */
public abstract class BaseApiException extends ErrorResponseException {

  protected static final String TRACE_ID_PROPERTY = "traceId";
  protected static final String TIMESTAMP_PROPERTY = "timestamp";

  protected BaseApiException(HttpStatusCode status, String title, String detail, String typeUri) {
    this(status, title, detail, typeUri, null);
  }

  protected BaseApiException(
      HttpStatusCode status, String title, String detail, String typeUri, Throwable cause) {
    super(status, criarProblemDetail(status, title, detail, typeUri), cause);
  }

  private static ProblemDetail criarProblemDetail(
      HttpStatusCode status, String title, String detail, String typeUri) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
    pd.setTitle(title);
    pd.setType(URI.create(typeUri));
    pd.setProperty(TRACE_ID_PROPERTY, TraceIdUtil.getOrCreateTraceId());
    pd.setProperty(TIMESTAMP_PROPERTY, Instant.now().toString());
    return pd;
  }
}
