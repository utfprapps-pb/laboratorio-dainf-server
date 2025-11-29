package br.com.utfpr.gerenciamento.server.exception;

import org.springframework.http.HttpStatus;

/**
 * Excecao lancada quando o saldo de itens e insuficiente para a operacao.
 *
 * <p>Retorna HTTP 422 Unprocessable Entity com detalhes no formato RFC 9457.
 */
public class SaldoInsuficienteException extends BaseApiException {

  private static final HttpStatus STATUS = HttpStatus.UNPROCESSABLE_ENTITY;
  private static final String TITLE = "Saldo insuficiente";
  private static final String TYPE_URI = "/errors/saldo-insuficiente";

  public SaldoInsuficienteException(String message) {
    super(STATUS, TITLE, message, TYPE_URI);
  }
}
