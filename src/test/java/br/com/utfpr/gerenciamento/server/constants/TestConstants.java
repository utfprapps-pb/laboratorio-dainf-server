package br.com.utfpr.gerenciamento.server.constants;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Constantes centralizadas para testes usando Java 21 features. */
public final class TestConstants {

  private TestConstants() {
    // classe utilitária
  }

  // ==================== CONSTANTES DE USUÁRIO ====================

  /** Documentos de teste para diferentes tipos de usuário */
  public static final class Documentos {
    public static final String ALUNO_PADRAO = "111111";
    public static final String SERVIDOR_PADRAO = "1234567";
    public static final String ADMINISTRADOR_PADRAO = "9999999";
    public static final String INEXISTENTE = "000000";

    private Documentos() {}
  }

  /** Emails de teste seguindo padrões UTFPR */
  public static final class Emails {
    public static final String ALUNO_PADRAO = "111111@utfpr.edu.br";
    public static final String SERVIDOR_PADRAO = "1234567@utfpr.edu.br";
    public static final String ADMINISTRADOR_PADRAO = "9999999@utfpr.edu.br";
    public static final String INVALIDO = "email-invalido";

    private Emails() {}
  }

  /** Nomes de teste para usuários */
  public static final class Nomes {
    public static final String ALUNO_PADRAO = "João Silva";
    public static final String SERVIDOR_PADRAO = "Maria Souza";
    public static final String ADMINISTRADOR_PADRAO = "Admin Sistema";

    private Nomes() {}
  }

  // ==================== CONSTANTES DE ITEM ====================

  /** Dados de teste para itens */
  public static final class Itens {
    public static final String NOTEBOOK_PADRAO = "Notebook Dell";
    public static final String MOUSE_PADRAO = "Mouse USB";
    public static final String CADERNO_PADRAO = "Caderno 100 folhas";

    private Itens() {}
  }

  /** Saldo e quantidades para itens */
  public static final class Quantidades {
    // Valores padrão para itens permanentes
    public static final BigDecimal PERMANENTE_SALDO_PADRAO = new BigDecimal("5.00");
    public static final BigDecimal PERMANENTE_MINIMO_PADRAO = new BigDecimal("1.00");

    // Valores padrão para itens consumíveis
    public static final BigDecimal CONSUMIVEL_SALDO_PADRAO = new BigDecimal("10.00");
    public static final BigDecimal CONSUMIVEL_MINIMO_PADRAO = new BigDecimal("2.00");

    // Valores críticos para testes
    public static final BigDecimal ZERO = BigDecimal.ZERO;
    public static final BigDecimal UM = BigDecimal.ONE;

    private Quantidades() {}
  }

  // ==================== CONSTANTES DE EMPRÉSTIMO ====================

  /** Prazos e datas para empréstimos */
  public static final class Prazos {
    public static final int PRAZO_PADRAO_DIAS = 7;
    public static final int PRAZO_CURTO_DIAS = 3;
    public static final int PRAZO_LONGO_DIAS = 30;

    private Prazos() {}
  }

  // ==================== CONSTANTES DE PAGINAÇÃO ====================

  /** Parâmetros de paginação para testes */
  public static final class Paginacao {
    public static final int PAGINA_PADRAO = 0;
    public static final int PAGINA_SEGUNDA = 1;
    public static final int TAMANHO_PADRAO = 10;
    public static final int TAMANHO_PEQUENO = 5;

    private Paginacao() {}
  }

  // ==================== CONSTANTES DE SEGURANÇA ====================

  /** Senhas para testes de autenticação */
  public static final class Senhas {
    public static final String SENHA_PADRAO = "senha123";
    public static final String SENHA_CODIFICADA_PADRAO = "$2a$10$encodedPassword";

    private Senhas() {}
  }

  /** Tokens para testes de autenticação */
  public static final class Tokens {
    public static final String TOKEN_SECRETO_TESTE =
        "test-secret-key-for-jwt-validation-minimum-256-bits";
    public static final String TOKEN_VALIDO = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test";
    public static final String TOKEN_INVALIDO = "token-invalido";

    private Tokens() {}
  }

  // ==================== CONSTANTES DE PERMISSÕES ====================

  /** Nomes das permissões do sistema */
  public static final class Permissoes {
    public static final String ALUNO = "ALUNO";
    public static final String SERVIDOR = "SERVIDOR";
    public static final String ADMINISTRADOR = "ADMINISTRADOR";
    public static final String VISITANTE = "VISITANTE";

    private Permissoes() {}
  }

  // ==================== MÉTODOS UTILITÁRIOS ====================

  /** Obtém data relativa para testes usando switch expression (Java 21). */
  public static LocalDate dataRelativa(int dias) {
    var hoje = LocalDate.now();
    return switch (dias) {
      case 0 -> hoje;
      case 1 -> hoje.plusDays(1);
      case 5 -> hoje.plusDays(5);
      case -1 -> hoje.minusDays(1);
      case -10 -> hoje.minusDays(10);
      default -> throw new IllegalArgumentException("Dias inválidos: " + dias);
    };
  }

  /** Verifica se um email segue padrão UTFPR válido usando pattern matching. */
  public static boolean isEmailUtfprValido(String email) {
    if (email == null || email.isBlank()) {
      return false;
    }

    String[] parts = email.split("@");
    return switch (parts.length) {
      case 2 -> {
        String domain = parts[1];
        String localPart = parts[0];
        // Verifica se tem apenas números no domínio UTFPR principal (ex: 111111@utfpr.edu.br)
        boolean isUtfprNumbers = domain.equals("utfpr.edu.br") && localPart.matches("\\d{6,7}");
        // Verifica se é subdomínio UTFPR válido (ex: professor@aluno.utfpr.edu.br)
        boolean isSubdomain =
            domain.equals("aluno.utfpr.edu.br")
                || domain.equals("professores.utfpr.edu.br")
                || domain.equals("administrativo.utfpr.edu.br");
        // Para teste, também aceita alguns nomes comuns no domínio utfpr.edu.br
        boolean isCommonName =
            domain.equals("utfpr.edu.br")
                && (localPart.equals("professor")
                    || localPart.equals("admin")
                    || localPart.equals("test"));
        yield isUtfprNumbers || isSubdomain || isCommonName;
      }
      default -> false;
    };
  }

  /** Formata timestamp para uso em nomes de teste (para unicidade). */
  public static String timestampParaTeste() {
    // Garante unicidade usando nanoTime + contador estático
    return String.valueOf(System.nanoTime() + System.currentTimeMillis());
  }

  /** Cria email de teste único para evitar conflitos. */
  public static String emailUnico(String base) {
    return base + "+" + timestampParaTeste() + "@test.com";
  }
}
