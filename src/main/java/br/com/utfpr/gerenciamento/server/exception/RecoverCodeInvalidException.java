package br.com.utfpr.gerenciamento.server.exception;

import org.springframework.http.HttpStatus;

/**
 * Excecao lancada quando o codigo de recuperacao de senha e invalido ou expirado.
 *
 * <p>Retorna HTTP 400 Bad Request com detalhes no formato RFC 9457.
 */
public class RecoverCodeInvalidException extends BaseApiException {

  private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;
  private static final String TITLE = "Codigo de recuperacao invalido";
  private static final String TYPE_URI = "/errors/codigo-recuperacao-invalido";

  public RecoverCodeInvalidException(String message) {
    super(STATUS, TITLE, message, TYPE_URI);
  }
}
