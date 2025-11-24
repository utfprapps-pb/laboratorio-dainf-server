package br.com.utfpr.gerenciamento.server.annotation;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
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
    private static final String UTFPR_DOMAIN = "utfpr.edu.br";

    /**
     * Valida se uma string representa um endereço de email válido do domínio "utfpr.edu.br".
     *
     * <p>Entradas nulas ou vazias são consideradas inválidas; a validação também verifica sintaxe
     * de endereço de email e compara o domínio (parte após '@') com "utfpr.edu.br" de forma
     * case-insensitive.
     *
     * @param value o email a ser validado (pode conter espaços que serão removidos)
     * @param context contexto do validador (fornecido pela infraestrutura de validação)
     * @return `true` se o email for sintaticamente válido e pertencer ao domínio `utfpr.edu.br`,
     *     `false` caso contrário.
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
      if (value == null) return false;
      String trimmed = value.trim().toLowerCase();
      if (trimmed.isEmpty() || trimmed.contains("\n") || trimmed.contains("\r")) return false;
      try {
        InternetAddress emailAddr = new InternetAddress(trimmed);
        emailAddr.validate();
      } catch (AddressException ex) {
        return false;
      }
      int atIdx = trimmed.lastIndexOf('@');
      if (atIdx < 0 || atIdx == trimmed.length() - 1) return false;
      String domain = trimmed.substring(atIdx + 1);
      return domain.equals(UTFPR_DOMAIN);
    }
  }
}
