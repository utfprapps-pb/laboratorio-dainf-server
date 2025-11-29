package br.com.utfpr.gerenciamento.server.security;

import static org.junit.jupiter.api.Assertions.*;

import br.com.utfpr.gerenciamento.server.util.TraceIdUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

/** Testes unitarios para ProblemDetailResponseWriter. */
class ProblemDetailResponseWriterTest {

  private MockHttpServletResponse response;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    response = new MockHttpServletResponse();
    objectMapper = new ObjectMapper();
  }

  @AfterEach
  void cleanup() {
    TraceIdUtil.clear();
  }

  @Test
  void writeProblemDetail_DeveConfigurarStatusCorreto() throws IOException {
    // When
    ProblemDetailResponseWriter.writeProblemDetail(
        response, HttpStatus.BAD_REQUEST, "Titulo", "Detalhe", "/errors/teste");

    // Then
    assertEquals(400, response.getStatus());
  }

  @Test
  void writeProblemDetail_DeveConfigurarContentTypeCorreto() throws IOException {
    // When
    ProblemDetailResponseWriter.writeProblemDetail(
        response, HttpStatus.BAD_REQUEST, "Titulo", "Detalhe", "/errors/teste");

    // Then - Content-Type pode incluir charset
    assertTrue(
        response.getContentType().startsWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE),
        "Content-Type deve ser application/problem+json");
  }

  @Test
  void writeProblemDetail_DeveConfigurarEncodingUtf8() throws IOException {
    // When
    ProblemDetailResponseWriter.writeProblemDetail(
        response, HttpStatus.BAD_REQUEST, "Titulo", "Detalhe", "/errors/teste");

    // Then
    assertEquals("UTF-8", response.getCharacterEncoding());
  }

  @Test
  void writeProblemDetail_DeveEscreverJsonValido() throws IOException {
    // When
    ProblemDetailResponseWriter.writeProblemDetail(
        response, HttpStatus.BAD_REQUEST, "Titulo", "Detalhe", "/errors/teste");

    // Then
    String content = response.getContentAsString();
    assertDoesNotThrow(() -> objectMapper.readTree(content));
  }

  @Test
  void writeProblemDetail_DeveConterPropriedadesRfc9457() throws IOException {
    // When
    ProblemDetailResponseWriter.writeProblemDetail(
        response, HttpStatus.BAD_REQUEST, "Titulo Teste", "Detalhe Teste", "/errors/teste");

    // Then
    JsonNode json = objectMapper.readTree(response.getContentAsString());
    assertEquals("Titulo Teste", json.get("title").asText());
    assertEquals("Detalhe Teste", json.get("detail").asText());
    assertEquals(400, json.get("status").asInt());
    assertEquals("/errors/teste", json.get("type").asText());

    // Propriedades customizadas podem estar no objeto properties ou no nivel raiz
    JsonNode traceIdNode =
        json.has("traceId") ? json.get("traceId") : json.path("properties").get("traceId");
    JsonNode timestampNode =
        json.has("timestamp") ? json.get("timestamp") : json.path("properties").get("timestamp");
    assertNotNull(traceIdNode, "traceId deve estar presente");
    assertNotNull(timestampNode, "timestamp deve estar presente");
  }

  @Test
  void writeUnauthorized_DeveRetornar401() throws IOException {
    // When
    ProblemDetailResponseWriter.writeUnauthorized(response, "Mensagem de erro");

    // Then
    assertEquals(401, response.getStatus());
    JsonNode json = objectMapper.readTree(response.getContentAsString());
    assertEquals("Falha na autenticacao", json.get("title").asText());
    assertEquals("Mensagem de erro", json.get("detail").asText());
  }

  @Test
  void writeBadCredentials_DeveRetornar401ComMensagemPadrao() throws IOException {
    // When
    ProblemDetailResponseWriter.writeBadCredentials(response);

    // Then
    assertEquals(401, response.getStatus());
    JsonNode json = objectMapper.readTree(response.getContentAsString());
    assertEquals("Credenciais invalidas", json.get("title").asText());
    assertEquals("Usuario ou senha incorretos.", json.get("detail").asText());
  }

  @Test
  void writeTokenExpired_DeveRetornar401() throws IOException {
    // When
    ProblemDetailResponseWriter.writeTokenExpired(response);

    // Then
    assertEquals(401, response.getStatus());
    JsonNode json = objectMapper.readTree(response.getContentAsString());
    assertEquals("Token expirado", json.get("title").asText());
    assertTrue(json.get("detail").asText().contains("sessao expirou"));
  }

  @Test
  void writeTokenInvalid_DeveRetornar401() throws IOException {
    // When
    ProblemDetailResponseWriter.writeTokenInvalid(response);

    // Then
    assertEquals(401, response.getStatus());
    JsonNode json = objectMapper.readTree(response.getContentAsString());
    assertEquals("Token invalido", json.get("title").asText());
  }

  @Test
  void writePreconditionRequired_DeveRetornar428() throws IOException {
    // When
    ProblemDetailResponseWriter.writePreconditionRequired(response, "Nada consta pendente");

    // Then
    assertEquals(428, response.getStatus());
    JsonNode json = objectMapper.readTree(response.getContentAsString());
    assertEquals("Precondicao requerida", json.get("title").asText());
    assertEquals("Nada consta pendente", json.get("detail").asText());
  }

  @Test
  void writeForbiddenDisabled_DeveRetornar403() throws IOException {
    // When
    ProblemDetailResponseWriter.writeForbiddenDisabled(response, "Email nao verificado");

    // Then
    assertEquals(403, response.getStatus());
    JsonNode json = objectMapper.readTree(response.getContentAsString());
    assertEquals("Conta desabilitada", json.get("title").asText());
    assertEquals("Email nao verificado", json.get("detail").asText());
  }

  @Test
  void writeAccessDenied_DeveRetornar403() throws IOException {
    // When
    ProblemDetailResponseWriter.writeAccessDenied(response);

    // Then
    assertEquals(403, response.getStatus());
    JsonNode json = objectMapper.readTree(response.getContentAsString());
    assertEquals("Acesso negado", json.get("title").asText());
  }

  @Test
  void writeProblemDetail_TraceIdDeveSerUuidValido() throws IOException {
    // When
    ProblemDetailResponseWriter.writeProblemDetail(
        response, HttpStatus.BAD_REQUEST, "Titulo", "Detalhe", "/errors/teste");

    // Then
    JsonNode json = objectMapper.readTree(response.getContentAsString());
    // Propriedades customizadas podem estar no objeto properties ou no nivel raiz
    JsonNode traceIdNode =
        json.has("traceId") ? json.get("traceId") : json.path("properties").get("traceId");
    assertNotNull(traceIdNode, "traceId deve estar presente");
    String traceId = traceIdNode.asText();
    assertDoesNotThrow(() -> java.util.UUID.fromString(traceId), "TraceId deve ser um UUID valido");
  }
}
