package br.com.utfpr.gerenciamento.server.util;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.*;

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
      return value != null && value.toLowerCase().endsWith("@utfpr.edu.br");
    }
  }
}
