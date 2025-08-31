package br.com.utfpr.gerenciamento.server.error;

public class RecoverCodeInvalidException extends RuntimeException {

  public RecoverCodeInvalidException(String message) {
    super(message);
  }
}
