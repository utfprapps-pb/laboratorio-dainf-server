package br.com.utfpr.gerenciamento.server.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.security.PreconditionRequiredAuthenticationException;
import br.com.utfpr.gerenciamento.server.util.TraceIdUtil;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import java.time.Instant;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

/**
 * Testes unitários para o GlobalExceptionHandler.
 *
 * <p>Valida que todas as exceções são tratadas corretamente no formato RFC 9457 (Problem Details).
 */
class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new GlobalExceptionHandler();
  }

  @AfterEach
  void cleanup() {
    TraceIdUtil.clear();
  }

  // ==================== Testes de Estrutura RFC 9457 ====================

  @Nested
  @DisplayName("Estrutura RFC 9457")
  class EstruturaRfc9457Tests {

    @Test
    @DisplayName("ProblemDetail deve conter traceId")
    void deveConterTraceId() {
      ProblemDetail result = handler.handleAccessDenied(new AccessDeniedException("test"));

      assertNotNull(result.getProperties());
      assertNotNull(result.getProperties().get("traceId"));
    }

    @Test
    @DisplayName("ProblemDetail deve conter timestamp")
    void deveConterTimestamp() {
      ProblemDetail result = handler.handleAccessDenied(new AccessDeniedException("test"));

      assertNotNull(result.getProperties());
      String timestamp = (String) result.getProperties().get("timestamp");
      assertNotNull(timestamp);
      assertDoesNotThrow(() -> Instant.parse(timestamp));
    }

    @Test
    @DisplayName("TraceId deve ser UUID válido")
    void traceIdDeveSerUuidValido() {
      ProblemDetail result = handler.handleAccessDenied(new AccessDeniedException("test"));

      String traceId = (String) result.getProperties().get("traceId");
      assertDoesNotThrow(() -> java.util.UUID.fromString(traceId));
    }
  }

  // ==================== Testes de Autenticação/Autorização ====================

  @Nested
  @DisplayName("Exceções de Autenticação/Autorização")
  class AutenticacaoAutorizacaoTests {

    @Test
    @DisplayName("PreconditionRequiredAuthenticationException deve retornar 428")
    void handlePreconditionRequired() {
      var ex = new PreconditionRequiredAuthenticationException("Nada consta pendente");

      ProblemDetail result = handler.handlePreconditionRequired(ex);

      assertEquals(428, result.getStatus());
      assertEquals("Precondição requerida", result.getTitle());
      assertEquals("Nada consta pendente", result.getDetail());
      assertTrue(result.getType().toString().contains("/errors/precondicao-requerida"));
    }

    @Test
    @DisplayName("AccessDeniedException deve retornar 403")
    void handleAccessDenied() {
      var ex = new AccessDeniedException("Sem permissão");

      ProblemDetail result = handler.handleAccessDenied(ex);

      assertEquals(HttpStatus.FORBIDDEN.value(), result.getStatus());
      assertEquals("Acesso negado", result.getTitle());
      assertTrue(result.getType().toString().contains("/errors/acesso-negado"));
    }

    @Test
    @DisplayName("DisabledException deve retornar 403")
    void handleDisabled() {
      var ex = new DisabledException("Conta desabilitada");

      ProblemDetail result = handler.handleDisabled(ex);

      assertEquals(HttpStatus.FORBIDDEN.value(), result.getStatus());
      assertEquals("Conta desabilitada", result.getTitle());
      assertEquals("Conta desabilitada", result.getDetail());
      assertTrue(result.getType().toString().contains("/errors/conta-desabilitada"));
    }

    @Test
    @DisplayName("BadCredentialsException deve retornar 401")
    void handleBadCredentials() {
      var ex = new BadCredentialsException("Usuário inválido");

      ProblemDetail result = handler.handleBadCredentials(ex);

      assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getStatus());
      assertEquals("Credenciais inválidas", result.getTitle());
      assertEquals("Usuário ou senha incorretos.", result.getDetail());
      assertTrue(result.getType().toString().contains("/errors/credenciais-invalidas"));
    }

    @Test
    @DisplayName("AuthenticationException genérica deve retornar 401")
    void handleAuthenticationException() {
      var ex = new InsufficientAuthenticationException("Autenticação insuficiente");

      ProblemDetail result = handler.handleAuthenticationException(ex);

      assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getStatus());
      assertEquals("Falha na autenticação", result.getTitle());
      assertTrue(result.getType().toString().contains("/errors/autenticacao-falhou"));
    }
  }

  // ==================== Testes de JWT ====================

  @Nested
  @DisplayName("Exceções JWT")
  class JwtTests {

    @Test
    @DisplayName("TokenExpiredException deve retornar 401")
    void handleTokenExpired() {
      var ex = new TokenExpiredException("Token expirou", Instant.now());

      ProblemDetail result = handler.handleTokenExpired(ex);

      assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getStatus());
      assertEquals("Token expirado", result.getTitle());
      assertEquals("Sua sessão expirou. Por favor, faça login novamente.", result.getDetail());
      assertTrue(result.getType().toString().contains("/errors/token-expirado"));
    }

    @Test
    @DisplayName("JWTVerificationException deve retornar 401")
    void handleJWTVerification() {
      var ex = new JWTVerificationException("Token inválido");

      ProblemDetail result = handler.handleJWTVerification(ex);

      assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getStatus());
      assertEquals("Token inválido", result.getTitle());
      assertEquals("Token de autenticação inválido.", result.getDetail());
      assertTrue(result.getType().toString().contains("/errors/token-invalido"));
    }
  }

  // ==================== Testes de Validação ====================

  @Nested
  @DisplayName("Exceções de Validação")
  class ValidacaoTests {

    @Test
    @DisplayName("MethodArgumentNotValidException deve retornar 400 com erros por campo")
    void handleMethodArgumentNotValid() {
      BindingResult bindingResult = mock(BindingResult.class);
      FieldError fieldError1 = new FieldError("objeto", "nome", "Nome é obrigatório");
      FieldError fieldError2 = new FieldError("objeto", "email", "Email inválido");
      when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError1, fieldError2));

      MethodParameter methodParameter = mock(MethodParameter.class);
      MethodArgumentNotValidException ex =
          new MethodArgumentNotValidException(methodParameter, bindingResult);

      ResponseEntity<Object> response =
          handler.handleMethodArgumentNotValid(
              ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, mock(WebRequest.class));

      assertNotNull(response);
      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

      ProblemDetail problemDetail = (ProblemDetail) response.getBody();
      assertNotNull(problemDetail);
      assertEquals("Erro de validação", problemDetail.getTitle());
      assertTrue(problemDetail.getType().toString().contains("/errors/validacao"));

      @SuppressWarnings("unchecked")
      var errors = (java.util.Map<String, String>) problemDetail.getProperties().get("errors");
      assertNotNull(errors);
      assertEquals(2, errors.size());
      assertEquals("Nome é obrigatório", errors.get("nome"));
      assertEquals("Email inválido", errors.get("email"));
    }

    @Test
    @DisplayName("MethodArgumentNotValidException deve incluir traceId e timestamp")
    void handleMethodArgumentNotValidComTraceIdTimestamp() {
      BindingResult bindingResult = mock(BindingResult.class);
      when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of());

      MethodParameter methodParameter = mock(MethodParameter.class);
      MethodArgumentNotValidException ex =
          new MethodArgumentNotValidException(methodParameter, bindingResult);

      ResponseEntity<Object> response =
          handler.handleMethodArgumentNotValid(
              ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, mock(WebRequest.class));

      ProblemDetail problemDetail = (ProblemDetail) response.getBody();
      assertNotNull(problemDetail.getProperties().get("traceId"));
      assertNotNull(problemDetail.getProperties().get("timestamp"));
    }
  }

  // ==================== Testes de Argumentos Inválidos ====================

  @Nested
  @DisplayName("Exceções de Argumentos Inválidos")
  class ArgumentosInvalidosTests {

    @Test
    @DisplayName("IllegalArgumentException deve retornar 400")
    void handleIllegalArgument() {
      var ex = new IllegalArgumentException("Argumento não pode ser nulo");

      ProblemDetail result = handler.handleIllegalArgument(ex);

      assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatus());
      assertEquals("Argumento inválido", result.getTitle());
      assertEquals("Parâmetro inválido.", result.getDetail());
      assertTrue(result.getType().toString().contains("/errors/argumento-invalido"));
    }

    @Test
    @DisplayName("IndexOutOfBoundsException deve retornar 400")
    void handleIndexOutOfBounds() {
      var ex = new IndexOutOfBoundsException("Index 5 out of bounds");

      ProblemDetail result = handler.handleIndexOutOfBounds(ex);

      assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatus());
      assertEquals("Índice fora dos limites", result.getTitle());
      assertTrue(result.getType().toString().contains("/errors/indice-invalido"));
    }

    @Test
    @DisplayName("NoSuchElementException deve retornar 404")
    void handleNoSuchElement() {
      var ex = new NoSuchElementException("Elemento não existe");

      ProblemDetail result = handler.handleNoSuchElement(ex);

      assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatus());
      assertEquals("Elemento não encontrado", result.getTitle());
      assertTrue(result.getType().toString().contains("/errors/elemento-nao-encontrado"));
    }
  }

  // ==================== Testes de Exceção Genérica ====================

  @Nested
  @DisplayName("Exceção Genérica (Fallback)")
  class ExcecaoGenericaTests {

    @Test
    @DisplayName("Exception genérica deve retornar 500")
    void handleGenericException() {
      var ex = new RuntimeException("Erro inesperado");

      ProblemDetail result = handler.handleGenericException(ex);

      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getStatus());
      assertEquals("Erro interno do servidor", result.getTitle());
      assertEquals(
          "Ocorreu um erro inesperado. Por favor, tente novamente mais tarde.", result.getDetail());
      assertTrue(result.getType().toString().contains("/errors/erro-interno"));
    }

    @Test
    @DisplayName("NullPointerException deve retornar 500")
    void handleNullPointerException() {
      var ex = new NullPointerException("null");

      ProblemDetail result = handler.handleGenericException(ex);

      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getStatus());
    }
  }
}
