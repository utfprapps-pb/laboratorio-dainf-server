package br.com.utfpr.gerenciamento.server.exception;

import org.springframework.http.HttpStatus;

/**
 * Excecao lancada quando operacoes de arquivo nao sao seguras ou violam politicas de seguranca.
 *
 * <p>Retorna HTTP 400 Bad Request com detalhes no formato RFC 9457.
 */
public class ArquivoException extends BaseApiException {

  private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;
  private static final String TITLE = "Erro no arquivo";
  private static final String TYPE_URI = "/errors/arquivo-invalido";

  public static final String MSG_NOME_VAZIO = "Nome do arquivo nao pode ser vazio";
  public static final String MSG_NOME_INVALIDO_PREFIX = "Nome de arquivo invalido: ";
  public static final String MSG_ACESSO_INSEGURO_PREFIX = "Acesso inseguro ao arquivo: ";

  public ArquivoException(String message) {
    super(STATUS, TITLE, message, TYPE_URI);
  }

  public ArquivoException(String message, Throwable cause) {
    super(STATUS, TITLE, message, TYPE_URI, cause);
  }
}
