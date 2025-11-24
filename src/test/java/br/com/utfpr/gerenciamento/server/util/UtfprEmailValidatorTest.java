package br.com.utfpr.gerenciamento.server.util;

import static org.junit.jupiter.api.Assertions.*;

import br.com.utfpr.gerenciamento.server.annotation.UtfprEmailValidator;
import org.junit.jupiter.api.Test;

class UtfprEmailValidatorTest {
  private final UtfprEmailValidator.UtfprEmailValidatorImpl validator =
      new UtfprEmailValidator.UtfprEmailValidatorImpl();

  @Test
  void validUtfprEmailShouldPass() {
    assertTrue(validator.isValid("user@utfpr.edu.br", null));
    assertTrue(validator.isValid("USER@utfpr.edu.br", null));
    assertTrue(validator.isValid("user.name@utfpr.edu.br", null));
  }

  @Test
  void invalidDomainShouldFail() {
    assertFalse(validator.isValid("user@gmail.com", null));
    assertFalse(validator.isValid("user@utfpr.com", null));
    assertFalse(validator.isValid("user@edu.br", null));
  }

  @Test
  void invalidFormatShouldFail() {
    assertFalse(validator.isValid("userutfpr.edu.br", null));
    assertFalse(validator.isValid("user@utfpr.edu", null));
    assertFalse(validator.isValid("@utfpr.edu.br", null));
    assertFalse(validator.isValid("user@utfpr.edu.br@", null));
  }

  @Test
  void nullOrEmptyShouldFail() {
    assertFalse(validator.isValid(null, null));
    assertFalse(validator.isValid("", null));
    assertFalse(validator.isValid("   ", null));
  }
}
