package br.com.utfpr.gerenciamento.server.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class NomeCompletoValidator implements ConstraintValidator<NomeCompleto, String> {

  private static final Pattern CARACTERES_INVALIDOS =
      Pattern.compile(".*[0-9@#$%^&*()_+=\\[\\]{};:'\"<>,.?/\\\\].*");
  private static final int MIN_PARTES = 2;

  @Override
  public boolean isValid(String nome, ConstraintValidatorContext context) {
    if (nome == null || nome.trim().isEmpty()) {
      return false;
    }

    // Verificar se começa ou termina com espaços
    if (!nome.equals(nome.trim())) {
      return false;
    }

    // Verificar se tem espaços duplos no meio
    if (nome.contains("  ")) {
      return false;
    }

    // Verificar se tem caracteres inválidos
    if (CARACTERES_INVALIDOS.matcher(nome).matches()) {
      return false;
    }

    // Verificar se tem pelo menos 2 partes (nome + sobrenome)
    String[] partes = nome.trim().split("\\s+");
    return partes.length >= MIN_PARTES && !partes[0].isEmpty() && !partes[1].isEmpty();
  }
}
