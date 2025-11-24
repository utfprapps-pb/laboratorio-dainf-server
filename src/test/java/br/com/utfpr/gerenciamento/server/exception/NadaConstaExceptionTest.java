package br.com.utfpr.gerenciamento.server.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class NadaConstaExceptionTest {
  @Test
  void testMessageIsSetCorrectly() {
    String msg = "Custom error message";
    NadaConstaException ex = new NadaConstaException(msg);
    assertEquals(msg, ex.getMessage());
  }

  @Test
  void testIsRuntimeException() {
    NadaConstaException ex = new NadaConstaException("msg");
    assertTrue(ex instanceof RuntimeException);
  }
}
