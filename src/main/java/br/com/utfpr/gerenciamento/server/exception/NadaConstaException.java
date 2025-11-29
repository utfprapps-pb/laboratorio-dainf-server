package br.com.utfpr.gerenciamento.server.exception;

import org.springframework.http.HttpStatus;

/**
 * Excecao lancada quando o usuario tem pendencias de nada consta.
 *
 * <p>Retorna HTTP 422 Unprocessable Entity com detalhes no formato RFC 9457.
 */
public class NadaConstaException extends BaseApiException {

  private static final HttpStatus STATUS = HttpStatus.UNPROCESSABLE_ENTITY;
  private static final String TITLE = "Nada consta pendente";
  private static final String TYPE_URI = "/errors/nada-consta";

  public NadaConstaException(String message) {
    super(STATUS, TITLE, message, TYPE_URI);
  }
}
