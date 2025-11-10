package br.com.utfpr.gerenciamento.server.constants;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Teste para validar as constantes centralizadas. */
class TestConstantsTest {

  @Test
  @DisplayName("Deve validar constantes de documentos centralizadas")
  void deveValidarConstantesDocumentos() {
    assertAll(
        "Validação de constantes de documentos",
        () -> assertEquals("111111", TestConstants.Documentos.ALUNO_PADRAO),
        () -> assertEquals("1234567", TestConstants.Documentos.SERVIDOR_PADRAO),
        () -> assertEquals("9999999", TestConstants.Documentos.ADMINISTRADOR_PADRAO),
        () -> assertEquals("000000", TestConstants.Documentos.INEXISTENTE));
  }

  @Test
  @DisplayName("Deve validar constantes de emails centralizadas")
  void deveValidarConstantesEmails() {
    assertAll(
        "Validação de constantes de emails",
        () -> assertEquals("111111@utfpr.edu.br", TestConstants.Emails.ALUNO_PADRAO),
        () -> assertEquals("1234567@utfpr.edu.br", TestConstants.Emails.SERVIDOR_PADRAO),
        () -> assertEquals("9999999@utfpr.edu.br", TestConstants.Emails.ADMINISTRADOR_PADRAO),
        () -> assertEquals("email-invalido", TestConstants.Emails.INVALIDO));
  }

  @Test
  @DisplayName("Deve validar constantes de nomes centralizadas")
  void deveValidarConstantesNomes() {
    assertAll(
        "Validação de constantes de nomes",
        () -> assertEquals("João Silva", TestConstants.Nomes.ALUNO_PADRAO),
        () -> assertEquals("Maria Souza", TestConstants.Nomes.SERVIDOR_PADRAO),
        () -> assertEquals("Admin Sistema", TestConstants.Nomes.ADMINISTRADOR_PADRAO));
  }

  @Test
  @DisplayName("Deve validar constantes de itens centralizadas")
  void deveValidarConstantesItens() {
    assertAll(
        "Validação de constantes de itens",
        () -> assertEquals("Notebook Dell", TestConstants.Itens.NOTEBOOK_PADRAO),
        () -> assertEquals("Mouse USB", TestConstants.Itens.MOUSE_PADRAO),
        () -> assertEquals("Caderno 100 folhas", TestConstants.Itens.CADERNO_PADRAO));
  }

  @Test
  @DisplayName("Deve validar constantes de quantidades centralizadas")
  void deveValidarConstantesQuantidades() {
    assertAll(
        "Validação de constantes de quantidades",
        () ->
            assertEquals(TestConstants.Quantidades.PERMANENTE_SALDO_PADRAO, new BigDecimal("5.00")),
        () ->
            assertEquals(
                TestConstants.Quantidades.PERMANENTE_MINIMO_PADRAO, new BigDecimal("1.00")),
        () ->
            assertEquals(
                TestConstants.Quantidades.CONSUMIVEL_SALDO_PADRAO, new BigDecimal("10.00")),
        () ->
            assertEquals(
                TestConstants.Quantidades.CONSUMIVEL_MINIMO_PADRAO, new BigDecimal("2.00")),
        () -> assertEquals(BigDecimal.ZERO, TestConstants.Quantidades.ZERO),
        () -> assertEquals(BigDecimal.ONE, TestConstants.Quantidades.UM));
  }

  @Test
  @DisplayName("Deve validar constantes de prazos centralizadas")
  void deveValidarConstantesPrazos() {
    assertAll(
        "Validação de constantes de prazos",
        () -> assertEquals(7, TestConstants.Prazos.PRAZO_PADRAO_DIAS),
        () -> assertEquals(3, TestConstants.Prazos.PRAZO_CURTO_DIAS),
        () -> assertEquals(30, TestConstants.Prazos.PRAZO_LONGO_DIAS));
  }

  @Test
  @DisplayName("Deve validar constantes de paginação centralizadas")
  void deveValidarConstantesPaginacao() {
    assertAll(
        "Validação de constantes de paginação",
        () -> assertEquals(0, TestConstants.Paginacao.PAGINA_PADRAO),
        () -> assertEquals(1, TestConstants.Paginacao.PAGINA_SEGUNDA),
        () -> assertEquals(10, TestConstants.Paginacao.TAMANHO_PADRAO),
        () -> assertEquals(5, TestConstants.Paginacao.TAMANHO_PEQUENO));
  }

  @Test
  @DisplayName("Deve validar constantes de segurança centralizadas")
  void deveValidarConstantesSeguranca() {
    assertAll(
        "Validação de constantes de senhas",
        () -> assertEquals("senha123", TestConstants.Senhas.SENHA_PADRAO),
        () -> assertEquals("$2a$10$encodedPassword", TestConstants.Senhas.SENHA_CODIFICADA_PADRAO));

    assertAll(
        "Validação de constantes de tokens",
        () ->
            assertEquals(
                "test-secret-key-for-jwt-validation-minimum-256-bits",
                TestConstants.Tokens.TOKEN_SECRETO_TESTE),
        () ->
            assertEquals(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test", TestConstants.Tokens.TOKEN_VALIDO),
        () -> assertEquals("token-invalido", TestConstants.Tokens.TOKEN_INVALIDO));
  }

  @Test
  @DisplayName("Deve validar constantes de permissões centralizadas")
  void deveValidarConstantesPermissoes() {
    assertAll(
        "Validação de constantes de permissões",
        () -> assertEquals("ALUNO", TestConstants.Permissoes.ALUNO),
        () -> assertEquals("SERVIDOR", TestConstants.Permissoes.SERVIDOR),
        () -> assertEquals("ADMINISTRADOR", TestConstants.Permissoes.ADMINISTRADOR),
        () -> assertEquals("VISITANTE", TestConstants.Permissoes.VISITANTE));
  }

  @Test
  @DisplayName("Deve validar emails UTFPR usando pattern matching")
  void deveValidarEmailsUtfprUsandoPatternMatching() {
    assertAll(
        "Validação de emails UTFPR válidos",
        () -> assertTrue(TestConstants.isEmailUtfprValido("111111@utfpr.edu.br")),
        () -> assertTrue(TestConstants.isEmailUtfprValido("professor@utfpr.edu.br")),
        () -> assertTrue(TestConstants.isEmailUtfprValido("aluno@aluno.utfpr.edu.br")),
        () -> assertTrue(TestConstants.isEmailUtfprValido("admin@administrativo.utfpr.edu.br")));

    assertAll(
        "Validação de emails inválidos",
        () -> assertFalse(TestConstants.isEmailUtfprValido("user@gmail.com")),
        () -> assertFalse(TestConstants.isEmailUtfprValido("invalid-email")),
        () -> assertFalse(TestConstants.isEmailUtfprValido("@utfpr.edu.br")),
        () -> assertFalse(TestConstants.isEmailUtfprValido("user@")),
        () -> assertFalse(TestConstants.isEmailUtfprValido("")),
        () -> assertFalse(TestConstants.isEmailUtfprValido(null)));
  }

  @Test
  @DisplayName("Deve criar datas relativas usando switch expression")
  void deveCriarDatasRelativasUsandoSwitchExpression() {
    var hoje = LocalDate.now();

    assertAll(
        "Validação de datas relativas",
        () -> assertEquals(hoje, TestConstants.dataRelativa(0), "Data relativa 0 deveria ser hoje"),
        () ->
            assertEquals(
                hoje.plusDays(1),
                TestConstants.dataRelativa(1),
                "Data relativa 1 deveria ser amanhã"),
        () ->
            assertEquals(
                hoje.plusDays(5),
                TestConstants.dataRelativa(5),
                "Data relativa 5 deveria ser hoje + 5 dias"),
        () ->
            assertEquals(
                hoje.minusDays(1),
                TestConstants.dataRelativa(-1),
                "Data relativa -1 deveria ser ontem"),
        () ->
            assertEquals(
                hoje.minusDays(10),
                TestConstants.dataRelativa(-10),
                "Data relativa -10 deveria ser hoje - 10 dias"));

    // Teste de exceção para valor inválido
    assertThrows(
        IllegalArgumentException.class,
        () -> TestConstants.dataRelativa(999),
        "Deveria lançar exceção para valor inválido");
  }

  @Test
  @DisplayName("Deve criar emails únicos para testes")
  void deveCriarEmailsUnicosParaTestes() {
    // Act
    var emailUnico1 = TestConstants.emailUnico("test");
    var emailUnico2 = TestConstants.emailUnico("test");

    // Assert
    assertAll(
        "Validação de emails únicos",
        () ->
            assertNotEquals(
                emailUnico1, emailUnico2, "Emails únicos com mesmo prefixo devem ser diferentes"),
        () -> assertTrue(emailUnico1.startsWith("test+"), "Email único deve começar com prefixo +"),
        () ->
            assertTrue(
                emailUnico1.endsWith("@test.com"), "Email único deve terminar com @test.com"),
        () ->
            assertTrue(emailUnico2.startsWith("test+"), "Email único deve começar com prefixo +"));
  }

  @Test
  @DisplayName("Deve criar timestamp para teste")
  void deveCriarTimestampParaTeste() {
    // Act
    var timestamp1 = TestConstants.timestampParaTeste();
    var timestamp2 = TestConstants.timestampParaTeste();

    // Assert
    assertAll(
        "Validação de timestamp",
        () -> assertNotNull(timestamp1, "Timestamp não pode ser nulo"),
        () -> assertNotNull(timestamp2, "Timestamp não pode ser nulo"),
        () -> assertNotEquals(timestamp1, timestamp2, "Timestamps devem ser únicos"),
        () -> assertTrue(Long.parseLong(timestamp1) > 0, "Timestamp deve ser positivo"),
        () -> assertTrue(Long.parseLong(timestamp2) > 0, "Timestamp deve ser positivo"));
  }

  @Test
  @DisplayName("Deve demonstrar benefícios das constantes centralizadas")
  void deveDemonstrarBeneficiosConstantesCentralizadas() {
    // Antes: magic numbers e strings espalhados pelo código

    // Depois: constantes centralizadas e semânticas
    String email = TestConstants.Emails.ALUNO_PADRAO;
    int prazo = TestConstants.Prazos.PRAZO_PADRAO_DIAS;
    String permissao = TestConstants.Permissoes.ALUNO;
    BigDecimal saldo = TestConstants.Quantidades.PERMANENTE_SALDO_PADRAO;

    // Assert - Valida que os valores são os esperados
    assertAll(
        "Validação de valores usando constantes",
        () -> assertEquals("111111@utfpr.edu.br", email),
        () -> assertEquals(7, prazo),
        () -> assertEquals("ALUNO", permissao),
        () -> assertEquals(new BigDecimal("5.00"), saldo));
  }
}
