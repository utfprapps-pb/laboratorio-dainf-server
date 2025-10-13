package br.com.utfpr.gerenciamento.server.util;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

@Documented
@Constraint(validatedBy = UtfprEmailValidator.UtfprEmailValidatorImpl.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UtfprEmailValidator {
  String message() default "O email deve ser do domínio @utfpr.edu.br";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  class UtfprEmailValidatorImpl implements ConstraintValidator<UtfprEmailValidator, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
      if (value == null) return false;
      String trimmed = value.trim();
      if (trimmed.isEmpty()) return false;
      try {
        InternetAddress emailAddr = new InternetAddress(trimmed);
        emailAddr.validate();
      } catch (AddressException ex) {
        return false;
      }
      int atIdx = trimmed.lastIndexOf('@');
      if (atIdx < 0 || atIdx == trimmed.length() - 1) return false;
      String domain = trimmed.substring(atIdx + 1).toLowerCase();
      return domain.equals("utfpr.edu.br");
    }
  }
}
