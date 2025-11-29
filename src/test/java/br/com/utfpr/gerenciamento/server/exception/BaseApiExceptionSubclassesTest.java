package br.com.utfpr.gerenciamento.server.exception;

import static org.junit.jupiter.api.Assertions.*;

import br.com.utfpr.gerenciamento.server.util.TraceIdUtil;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.web.ErrorResponseException;

/**
 * Testes parametrizados para todas as subclasses de BaseApiException.
 *
 * <p>Valida que todas as excecoes seguem o padrao RFC 9457 com: - Status HTTP correto - Title
 * correto - Type URI correto - Propriedades traceId e timestamp
 */
class BaseApiExceptionSubclassesTest {

  private static final String TEST_MESSAGE = "Mensagem de teste";

  @AfterEach
  void cleanup() {
    TraceIdUtil.clear();
  }

  @ParameterizedTest(name = "{0} - Status {1}")
  @MethodSource("exceptionProvider")
  @DisplayName("Excecao deve conter propriedades RFC 9457")
  void deveConterPropriedadesRfc9457(
      String exceptionName,
      int expectedStatus,
      String expectedTitle,
      String expectedTypeUri,
      Supplier<BaseApiException> exceptionSupplier) {

    // When
    BaseApiException exception = exceptionSupplier.get();
    var problemDetail = exception.getBody();

    // Then
    assertNotNull(problemDetail, "ProblemDetail nao deve ser nulo");
    assertEquals(expectedStatus, problemDetail.getStatus(), "Status deve ser " + expectedStatus);
    assertEquals(expectedTitle, problemDetail.getTitle(), "Title deve ser correto");
    assertTrue(
        problemDetail.getType().toString().contains(expectedTypeUri),
        "Type URI deve conter " + expectedTypeUri);
    assertEquals(TEST_MESSAGE, problemDetail.getDetail(), "Detail deve ser a mensagem passada");
    assertNotNull(problemDetail.getProperties().get("traceId"), "TraceId deve estar presente");
    assertNotNull(problemDetail.getProperties().get("timestamp"), "Timestamp deve estar presente");
  }

  @ParameterizedTest(name = "{0} deve estender ErrorResponseException")
  @MethodSource("exceptionProvider")
  @DisplayName("Excecao deve estender ErrorResponseException")
  void deveEstenderErrorResponseException(
      String exceptionName,
      int expectedStatus,
      String expectedTitle,
      String expectedTypeUri,
      Supplier<BaseApiException> exceptionSupplier) {

    // When
    BaseApiException exception = exceptionSupplier.get();

    // Then
    assertInstanceOf(ErrorResponseException.class, exception);
    assertInstanceOf(RuntimeException.class, exception);
  }

  @ParameterizedTest(name = "{0} - TraceId deve ser UUID valido")
  @MethodSource("exceptionProvider")
  @DisplayName("TraceId deve ser UUID valido")
  void traceIdDeveSerUuidValido(
      String exceptionName,
      int expectedStatus,
      String expectedTitle,
      String expectedTypeUri,
      Supplier<BaseApiException> exceptionSupplier) {

    // When
    BaseApiException exception = exceptionSupplier.get();
    String traceId = (String) exception.getBody().getProperties().get("traceId");

    // Then
    assertDoesNotThrow(() -> java.util.UUID.fromString(traceId), "TraceId deve ser um UUID valido");
  }

  static Stream<Arguments> exceptionProvider() {
    return Stream.of(
        Arguments.of(
            "EntityNotFoundException",
            404,
            "Recurso não encontrado",
            "/errors/entidade-nao-encontrada",
            (Supplier<BaseApiException>) () -> new EntityNotFoundException(TEST_MESSAGE)),
        Arguments.of(
            "ArquivoException",
            400,
            "Erro no arquivo",
            "/errors/arquivo-invalido",
            (Supplier<BaseApiException>) () -> new ArquivoException(TEST_MESSAGE)),
        Arguments.of(
            "EmailException",
            503,
            "Servico de email indisponivel",
            "/errors/email-indisponivel",
            (Supplier<BaseApiException>) () -> new EmailException(TEST_MESSAGE)),
        Arguments.of(
            "InvalidPasswordException",
            400,
            "Senha invalida",
            "/errors/senha-invalida",
            (Supplier<BaseApiException>) () -> new InvalidPasswordException(TEST_MESSAGE)),
        Arguments.of(
            "NadaConstaException",
            422,
            "Nada consta pendente",
            "/errors/nada-consta",
            (Supplier<BaseApiException>) () -> new NadaConstaException(TEST_MESSAGE)),
        Arguments.of(
            "RecoverCodeInvalidException",
            400,
            "Codigo de recuperacao invalido",
            "/errors/codigo-recuperacao-invalido",
            (Supplier<BaseApiException>) () -> new RecoverCodeInvalidException(TEST_MESSAGE)),
        Arguments.of(
            "SaldoInsuficienteException",
            422,
            "Saldo insuficiente",
            "/errors/saldo-insuficiente",
            (Supplier<BaseApiException>) () -> new SaldoInsuficienteException(TEST_MESSAGE)));
  }
}
