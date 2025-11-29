package br.com.utfpr.gerenciamento.server.exception;

import static org.junit.jupiter.api.Assertions.*;

import br.com.utfpr.gerenciamento.server.util.TraceIdUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Testes unitarios especificos para EmailException.
 *
 * <p>Testes de estrutura RFC 9457 estao em BaseApiExceptionSubclassesTest.
 */
class EmailExceptionTest {

  @AfterEach
  void cleanup() {
    TraceIdUtil.clear();
  }

  @Test
  void deveInstanciarComMensagemECausa() {
    // Given
    String mensagem = "Falha ao enviar email";
    Throwable causa = new RuntimeException("SMTP connection timeout");

    // When
    EmailException exception = new EmailException(mensagem, causa);

    // Then
    assertNotNull(exception);
    assertEquals(mensagem, exception.getBody().getDetail());
    assertEquals(causa, exception.getCause());
    assertEquals("SMTP connection timeout", exception.getCause().getMessage());
  }
}
