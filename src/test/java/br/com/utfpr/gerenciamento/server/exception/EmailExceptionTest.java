package br.com.utfpr.gerenciamento.server.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Testes unitários para EmailException. */
class EmailExceptionTest {

  @Test
  void deveInstanciarComMensagem() {
    // Given
    String mensagem = "Falha ao enviar email";

    // When
    EmailException exception = new EmailException(mensagem);

    // Then
    assertNotNull(exception);
    assertEquals(mensagem, exception.getMessage());
    assertNull(exception.getCause());
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
    assertEquals(mensagem, exception.getMessage());
    assertEquals(causa, exception.getCause());
    assertEquals("SMTP connection timeout", exception.getCause().getMessage());
  }

  @Test
  void deveSerRuntimeException() {
    // Given/When
    EmailException exception = new EmailException("Erro de email");

    // Then
    assertInstanceOf(RuntimeException.class, exception);
  }

  @Test
  void devePreservarStackTrace() {
    // Given
    Exception causaOriginal = new IllegalStateException("Serviço indisponível");
    EmailException exception = new EmailException("Erro no envio", causaOriginal);

    // When
    StackTraceElement[] stackTrace = exception.getStackTrace();

    // Then
    assertNotNull(stackTrace);
    assertTrue(stackTrace.length > 0);
  }
}
