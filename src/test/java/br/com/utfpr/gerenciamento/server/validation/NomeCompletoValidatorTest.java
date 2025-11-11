package br.com.utfpr.gerenciamento.server.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NomeCompletoValidatorTest {

  private NomeCompletoValidator validator;
  private ConstraintValidatorContext context;

  @BeforeEach
  void setUp() {
    validator = new NomeCompletoValidator();
    // Mock do contexto - não usaremos na validação simples
    context = null;
  }

  @Test
  @DisplayName("Deve aceitar nomes válidos com duas partes")
  void deveAceitarNomesValidosComDuasPartes() {
    assertTrue(validator.isValid("João Silva", context));
    assertTrue(validator.isValid("Maria Santos", context));
    assertTrue(validator.isValid("Carlos Oliveira", context));
    assertTrue(validator.isValid("Ana Costa", context));
  }

  @Test
  @DisplayName("Deve aceitar nomes com mais de duas partes")
  void deveAceitarNomesComMaisPartes() {
    assertTrue(validator.isValid("Maria de Oliveira Costa", context));
    assertTrue(validator.isValid("Carlos Alberto Silva Santos", context));
    assertTrue(validator.isValid("João Paulo da Silva", context));
  }

  @ParameterizedTest
  @ValueSource(strings = {"João", "Maria", "Carlos", "Ana", "Pedro"})
  @DisplayName("Deve rejeitar nomes com apenas uma parte")
  void deveRejeitarNomesComUmaParte(String nome) {
    assertFalse(validator.isValid(nome, context), "Nome '" + nome + "' deveria ser inválido");
  }

  @ParameterizedTest
  @ValueSource(strings = {"João123", "Maria@Silva", "Carlos#Santos", "Ana$Costa"})
  @DisplayName("Deve rejeitar nomes com caracteres especiais ou números")
  void deveRejeitarNomesComCaracteresEspeciais(String nome) {
    assertFalse(validator.isValid(nome, context), "Nome '" + nome + "' deveria ser inválido");
  }

  @Test
  @DisplayName("Deve rejeitar valores nulos e vazios")
  void deveRejeitarValoresNulosEVazios() {
    assertFalse(validator.isValid(null, context));
    assertFalse(validator.isValid("", context));
    assertFalse(validator.isValid("   ", context));
  }

  @Test
  @DisplayName("Deve rejeitar nomes com espaços inadequados")
  void deveRejeitarNomesComEspacosInadequados() {
    assertFalse(validator.isValid("  João Silva", context));
    assertFalse(validator.isValid("João Silva  ", context));
    assertFalse(validator.isValid("João  Silva", context)); // espaço duplo no meio
  }

  @Test
  @DisplayName("Deve aceitar nomes com caracteres especiais portugueses válidos")
  void deveAceitarNomesComCaracteresPortugueses() {
    assertTrue(validator.isValid("João Álvares", context));
    assertTrue(validator.isValid("Maria José", context));
    assertTrue(validator.isValid("Antônio Carlos", context));
    assertTrue(validator.isValid("Francisco Ângelo", context));
    assertTrue(validator.isValid("Cecília Çaravelli", context));
  }

  @Test
  @DisplayName("Deve rejeitar nomes com partes vazias")
  void deveRejeitarNomesComPartesVazias() {
    assertFalse(validator.isValid("João ", context));
    assertFalse(validator.isValid(" Silva", context));
  }
}
