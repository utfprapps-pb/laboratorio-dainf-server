package br.com.utfpr.gerenciamento.server.exception;

/** Exceção lançada quando operações de arquivo não são seguras ou violam políticas de segurança. */
public class ArquivoException extends RuntimeException {

  public ArquivoException(String message) {
    super(message);
  }

  public ArquivoException(String message, Throwable cause) {
    super(message, cause);
  }
}
