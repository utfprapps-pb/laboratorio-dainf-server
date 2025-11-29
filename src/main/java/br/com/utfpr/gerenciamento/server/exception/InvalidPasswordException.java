package br.com.utfpr.gerenciamento.server.exception;

import org.springframework.http.HttpStatus;

/**
 * Excecao lancada quando as validacoes de senha falham.
 *
 * <p>Retorna HTTP 400 Bad Request com detalhes no formato RFC 9457.
 */
public class InvalidPasswordException extends BaseApiException {

  private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;
  private static final String TITLE = "Senha invalida";
  private static final String TYPE_URI = "/errors/senha-invalida";

  public InvalidPasswordException(String message) {
    super(STATUS, TITLE, message, TYPE_URI);
  }
}
