package br.com.utfpr.gerenciamento.server.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NomeCompletoValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NomeCompleto {
  String message() default
      "Nome deve conter pelo menos nome e sobrenome, sem números ou caracteres especiais";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
