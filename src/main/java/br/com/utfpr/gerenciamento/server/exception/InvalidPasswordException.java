package br.com.utfpr.gerenciamento.server.exception;

/** Exceção lançada quando as validações de senha falham. */
public class InvalidPasswordException extends RuntimeException {

  public InvalidPasswordException(String message) {
    super(message);
  }
}
