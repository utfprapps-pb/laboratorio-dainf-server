package br.com.utfpr.gerenciamento.server.exception;

import org.springframework.http.HttpStatus;

/**
 * Exceção lançada quando uma entidade não é encontrada.
 *
 * <p>Retorna HTTP 404 Not Found com detalhes no formato RFC 9457.
 */
public class EntityNotFoundException extends BaseApiException {

  private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;
  private static final String TITLE = "Recurso não encontrado";
  private static final String TYPE_URI = "/errors/entidade-nao-encontrada";

  /**
   * Cria uma nova exceção de entidade não encontrada.
   *
   * @param message mensagem descritiva do erro
   */
  public EntityNotFoundException(String message) {
    super(STATUS, TITLE, message, TYPE_URI);
  }
}
