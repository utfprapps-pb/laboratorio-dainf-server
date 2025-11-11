package br.com.utfpr.gerenciamento.server.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador para nomes completos em português brasileiro. Garante que o nome contenha pelo menos
 * duas partes (nome e sobrenome), sem números ou caracteres especiais inválidos.
 *
 * <p>Valida padrões brasileiros como:
 *
 * <ul>
 *   <li>Nome completo: "João Silva"
 *   <li>Múltiplos sobrenomes: "Maria de Oliveira Costa"
 *   <li>Abreviações: "João A. Silva"
 *   <li>Sufixos: "Carlos Silva Jr."
 * </ul>
 */
public class NomeCompletoValidator implements ConstraintValidator<NomeCompleto, String> {

  private static final int MAX_LENGTH = 255;

  @Override
  public boolean isValid(String nome, ConstraintValidatorContext context) {
    if (nome == null) {
      return false;
    }

    // Proteção contra DoS - limita comprimento máximo
    if (nome.length() > MAX_LENGTH) {
      return false;
    }

    // Verificação segura contra StringIndexOutOfBoundsException e espaços inadequados
    // Esta verificação também cobre strings que consistem apenas de espaços
    if (nome.isEmpty() || nome.charAt(0) == ' ' || nome.charAt(nome.length() - 1) == ' ') {
      return false;
    }

    boolean espacoAnterior = false;
    int countEspacos = 0;

    // Verificação rápida de caracteres inválidos e espaços duplos em um único loop
    for (int i = 0; i < nome.length(); i++) {
      char c = nome.charAt(i);

      if (isCaractereInvalido(c)) {
        return false;
      }

      if (c == ' ') {
        countEspacos++;
        if (espacoAnterior) {
          return false; // Espaço duplo encontrado
        }
        espacoAnterior = true;
      } else {
        espacoAnterior = false;
      }
    }

    // Verificar se tem pelo menos 2 partes (nome + sobrenome)
    return countEspacos >= 1;
  }

  /**
   * Verificação otimizada de caracteres usando Character.isLetter() para suporte Unicode completo.
   * Performance O(1) por caractere, sem backtracking.
   */
  private boolean isCaractereInvalido(char c) {
    // Números
    if (c >= '0' && c <= '9') {
      return true;
    }

    // Caracteres permitidos em nomes portugueses
    if (c == '.' || c == '-' || c == '\'' || c == ' ') {
      return false;
    }

    // Letras Unicode (incluindo caracteres acentuados portugueses)
      return !Character.isLetter(c);

    // Qualquer outro caractere é inválido
  }
}
