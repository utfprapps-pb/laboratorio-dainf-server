package br.com.utfpr.gerenciamento.server.util;

/**
 * Utilitários para manipulação de emails.
 *
 * <p>Fornece métodos auxiliares para validação e mascaramento de endereços de email, com foco em
 * proteção de PII (Personally Identifiable Information) e conformidade com GDPR/LGPD.
 *
 * @author Rodrigo Izidoro
 * @since 2025-10-22
 */
public final class EmailUtils {

  private EmailUtils() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  /**
   * Mascara um endereço de email para evitar exposição de PII (Personally Identifiable Information)
   * em logs.
   *
   * <p>Mantém os primeiros 2 caracteres da parte local, mascara o resto com asteriscos e mantém o
   * domínio completo para fins de debugging.
   *
   * <p><b>Exemplos:</b>
   *
   * <ul>
   *   <li>{@code "user@example.com"} → {@code "us***@example.com"}
   *   <li>{@code "john.doe@company.org"} → {@code "jo***@company.org"}
   *   <li>{@code "a@test.com"} → {@code "a***@test.com"}
   *   <li>{@code null} → {@code "null"}
   *   <li>{@code ""} → {@code ""}
   * </ul>
   *
   * @param email Email a ser mascarado
   * @return Email mascarado para logging seguro
   */
  public static String maskEmail(String email) {
    if (email == null || !email.contains("@")) {
      return email;
    }

    String[] parts = email.split("@");
    if (parts.length != 2) {
      return email; // Email inválido, retorna como está
    }

    String localPart = parts[0];
    String domain = parts[1];

    // Mantém primeiros 2 caracteres (ou 1 se email for muito curto)
    int keepChars = Math.min(2, localPart.length());
    String masked = localPart.substring(0, keepChars) + "***";

    return masked + "@" + domain;
  }

  /**
   * Valida se um email é válido para envio.
   *
   * <p>Verifica se o email não é null, vazio, ou apenas espaços em branco.
   *
   * @param email Email a ser validado
   * @return true se o email é válido, false caso contrário
   */
  public static boolean isValidEmail(String email) {
    return email != null && !email.trim().isEmpty();
  }
}
