package br.com.utfpr.gerenciamento.server.exception;

public class SaldoInsuficienteException extends RuntimeException {

  public SaldoInsuficienteException(String message) {
    super(message);
  }
}
