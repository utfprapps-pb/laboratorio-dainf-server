package br.com.utfpr.gerenciamento.server.exception;

/** Exceção lançada quando há falhas no envio de emails. */
public class EmailException extends RuntimeException {

  public EmailException(String message) {
    super(message);
  }

  public EmailException(String message, Throwable cause) {
    super(message, cause);
  }
}
