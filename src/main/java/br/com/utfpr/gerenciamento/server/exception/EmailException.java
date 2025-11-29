package br.com.utfpr.gerenciamento.server.exception;

import org.springframework.http.HttpStatus;

/**
 * Excecao lancada quando ha falhas no envio de emails.
 *
 * <p>Retorna HTTP 503 Service Unavailable com detalhes no formato RFC 9457.
 */
public class EmailException extends BaseApiException {

  private static final HttpStatus STATUS = HttpStatus.SERVICE_UNAVAILABLE;
  private static final String TITLE = "Servico de email indisponivel";
  private static final String TYPE_URI = "/errors/email-indisponivel";

  public EmailException(String message) {
    super(STATUS, TITLE, message, TYPE_URI);
  }

  public EmailException(String message, Throwable cause) {
    super(STATUS, TITLE, message, TYPE_URI, cause);
  }
}
