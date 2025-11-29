package br.com.utfpr.gerenciamento.server.exception;

import br.com.utfpr.gerenciamento.server.security.PreconditionRequiredAuthenticationException;
import br.com.utfpr.gerenciamento.server.util.TraceIdUtil;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import jakarta.annotation.Nullable;
import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Handler global de exceções para o formato RFC 9457 (Problem Details).
 *
 * <p>Este handler trata exceções do framework Spring e de segurança. Exceções de domínio que
 * estendem {@link BaseApiException} são tratadas automaticamente pelo Spring.
 *
 * <p>Responsabilidades:
 *
 * <ul>
 *   <li>Exceções de validação (MethodArgumentNotValidException)
 *   <li>Exceções de segurança (autenticação/autorização)
 *   <li>Exceções JWT (token expirado/inválido)
 *   <li>Fallback para exceções não tratadas
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  private static final String TRACE_ID_PROPERTY = "traceId";
  private static final String TIMESTAMP_PROPERTY = "timestamp";
  private static final String ERRORS_PROPERTY = "errors";

  // ==================== Exceções de Autenticação/Autorização ====================

  /** Precondition Required (Nada Consta pendente) - 428 Precondition Required */
  @ExceptionHandler(PreconditionRequiredAuthenticationException.class)
  public ProblemDetail handlePreconditionRequired(PreconditionRequiredAuthenticationException ex) {
    log.warn("Precondição requerida: {}", ex.getMessage());
    return criarProblemDetail(
        HttpStatus.valueOf(428),
        "Precondição requerida",
        ex.getMessage(),
        URI.create("/errors/precondicao-requerida"));
  }

  /** Acesso negado (403 Forbidden) - usuário autenticado mas sem permissão */
  @ExceptionHandler(AccessDeniedException.class)
  public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
    log.warn("Acesso negado: {}", ex.getMessage());
    return criarProblemDetail(
        HttpStatus.FORBIDDEN,
        "Acesso negado",
        "Você não tem permissão para acessar este recurso.",
        URI.create("/errors/acesso-negado"));
  }

  /** Conta desabilitada (email não verificado) - 403 Forbidden */
  @ExceptionHandler(DisabledException.class)
  public ProblemDetail handleDisabled(DisabledException ex) {
    log.warn("Conta desabilitada: {}", ex.getMessage());
    return criarProblemDetail(
        HttpStatus.FORBIDDEN,
        "Conta desabilitada",
        ex.getMessage(),
        URI.create("/errors/conta-desabilitada"));
  }

  /** Credenciais inválidas - 401 Unauthorized */
  @ExceptionHandler(BadCredentialsException.class)
  public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
    log.warn("Credenciais inválidas");
    return criarProblemDetail(
        HttpStatus.UNAUTHORIZED,
        "Credenciais inválidas",
        "Usuário ou senha incorretos.",
        URI.create("/errors/credenciais-invalidas"));
  }

  /** Outras exceções de autenticação - 401 Unauthorized */
  @ExceptionHandler(AuthenticationException.class)
  public ProblemDetail handleAuthenticationException(AuthenticationException ex) {
    log.warn("Erro de autenticação: {}", ex.getMessage());
    return criarProblemDetail(
        HttpStatus.UNAUTHORIZED,
        "Falha na autenticação",
        ex.getMessage(),
        URI.create("/errors/autenticacao-falhou"));
  }

  // ==================== Exceções JWT ====================

  /** Token JWT expirado - 401 Unauthorized */
  @ExceptionHandler(TokenExpiredException.class)
  public ProblemDetail handleTokenExpired(TokenExpiredException ex) {
    log.warn("Token expirado");
    return criarProblemDetail(
        HttpStatus.UNAUTHORIZED,
        "Token expirado",
        "Sua sessão expirou. Por favor, faça login novamente.",
        URI.create("/errors/token-expirado"));
  }

  /** Token JWT inválido - 401 Unauthorized */
  @ExceptionHandler(JWTVerificationException.class)
  public ProblemDetail handleJWTVerification(JWTVerificationException ex) {
    log.warn("Token inválido: {}", ex.getMessage());
    return criarProblemDetail(
        HttpStatus.UNAUTHORIZED,
        "Token inválido",
        "Token de autenticação inválido.",
        URI.create("/errors/token-invalido"));
  }

  // ==================== Validação ====================

  /** Override do método padrão para adicionar traceId e detalhes por campo. */
  @Override
  protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {

    Map<String, String> fieldErrors = new HashMap<>();
    for (FieldError error : ex.getBindingResult().getFieldErrors()) {
      fieldErrors.put(error.getField(), error.getDefaultMessage());
    }

    ProblemDetail problemDetail =
        criarProblemDetail(
            HttpStatus.BAD_REQUEST,
            "Erro de validação",
            "Um ou mais campos possuem valores inválidos.",
            URI.create("/errors/validacao"));
    problemDetail.setProperty(ERRORS_PROPERTY, fieldErrors);

    log.warn("Erro de validação: {} campos com erro", fieldErrors.size());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
  }

  // ==================== Exceções de Argumentos Inválidos ====================

  /** Argumento inválido - 400 Bad Request */
  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
    log.warn("Argumento inválido: {}", ex.getMessage());
    return criarProblemDetail(
        HttpStatus.BAD_REQUEST,
        "Argumento inválido",
        ex.getMessage(),
        URI.create("/errors/argumento-invalido"));
  }

  /** Índice fora dos limites - 400 Bad Request */
  @ExceptionHandler(IndexOutOfBoundsException.class)
  public ProblemDetail handleIndexOutOfBounds(IndexOutOfBoundsException ex) {
    log.warn("Índice fora dos limites: {}", ex.getMessage());
    return criarProblemDetail(
        HttpStatus.BAD_REQUEST,
        "Índice fora dos limites",
        "O índice solicitado está fora dos limites permitidos.",
        URI.create("/errors/indice-invalido"));
  }

  /** Elemento não encontrado - 404 Not Found */
  @ExceptionHandler(NoSuchElementException.class)
  public ProblemDetail handleNoSuchElement(NoSuchElementException ex) {
    log.warn("Elemento não encontrado: {}", ex.getMessage());
    return criarProblemDetail(
        HttpStatus.NOT_FOUND,
        "Elemento não encontrado",
        "O elemento solicitado não foi encontrado.",
        URI.create("/errors/elemento-nao-encontrado"));
  }

  // ==================== Exceção Genérica (Fallback) ====================

  /** Exceção genérica - 500 Internal Server Error */
  @ExceptionHandler(Exception.class)
  public ProblemDetail handleGenericException(Exception ex) {
    log.error("Erro interno não tratado: {}", ex.getMessage(), ex);
    return criarProblemDetail(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Erro interno do servidor",
        "Ocorreu um erro inesperado. Por favor, tente novamente mais tarde.",
        URI.create("/errors/erro-interno"));
  }

  // ==================== Método Auxiliar ====================

  /** Cria um ProblemDetail padronizado com traceId e timestamp. */
  private ProblemDetail criarProblemDetail(
      HttpStatus status, String title, String detail, URI type) {

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
    problemDetail.setTitle(title);
    problemDetail.setType(type);
    problemDetail.setProperty(TRACE_ID_PROPERTY, TraceIdUtil.getOrCreateTraceId());
    problemDetail.setProperty(TIMESTAMP_PROPERTY, Instant.now().toString());

    return problemDetail;
  }
}
