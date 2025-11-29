package br.com.utfpr.gerenciamento.server.exception;

import org.springframework.http.HttpStatus;

/**
 * Exceção lançada quando um método HTTP não é permitido para o recurso.
 *
 * <p>Retorna HTTP 405 Method Not Allowed com detalhes no formato RFC 9457.
 */
public class MethodNotAllowedException extends BaseApiException {

  private static final HttpStatus STATUS = HttpStatus.METHOD_NOT_ALLOWED;
  private static final String TITLE = "Método não permitido";
  private static final String TYPE_URI = "/errors/metodo-nao-permitido";

  /**
   * Cria uma nova exceção de método não permitido.
   *
   * @param message mensagem descritiva do erro
   */
  public MethodNotAllowedException(String message) {
    super(STATUS, TITLE, message, TYPE_URI);
  }
}
