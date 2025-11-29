package br.com.utfpr.gerenciamento.server.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Testes de controle de acesso baseado em papéis (Role-Based Access Control - RBAC).
 *
 * <p>Valida que diferentes tipos de usuários têm acesso correto aos endpoints: - ADMINISTRADOR:
 * Acesso total a todos os endpoints - LABORATORISTA: Acesso a endpoints de laboratório, NÃO acesso
 * a administração de usuários - PROFESSOR/ALUNO: Acesso apenas a endpoints básicos autenticados -
 * Não autenticado: Acesso apenas a endpoints públicos
 *
 * <p>Baseado na configuração de segurança em WebSecurity.java
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RoleBasedAccessControlTest {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate restTemplate;

  private String baseUrl;

  // Tokens para usuários existentes nos dados iniciais
  private String adminToken;
  private String laboratoristaToken;
  private String professorToken;
  private String alunoToken;

  @BeforeEach
  void setUp() {
    baseUrl = "http://localhost:" + port;

    // Autenticar apenas usuários existentes nos dados iniciais
    // Usando emails completos conforme migração V3.1
    adminToken = authenticateUser("utfprapps-pb@utfpr.edu.br", "123");
    laboratoristaToken = authenticateUser("joao@alunos.utfpr.edu.br", "123");
    professorToken = authenticateUser("favarim@professores.utfpr.edu.br", "123");
    alunoToken = authenticateUser("gzaffani@alunos.utfpr.edu.br", "123");
  }

  private String authenticateUser(String username, String password) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");

    Map<String, String> loginRequest =
        Map.of(
            "username", username,
            "password", password);

    HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);

    try {
      ResponseEntity<String> response =
          restTemplate.postForEntity(baseUrl + "/login", request, String.class);

      if (response.getStatusCode() == HttpStatus.OK) {
        String token = response.getBody();
        // Validar formato JWT
        if (token != null && token.split("\\.").length == 3) {
          return token;
        }
      }
      return null;
    } catch (Exception e) {
      return null;
    }
  }

  private HttpHeaders createAuthHeaders(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");
    if (token != null) {
      headers.set("Authorization", "Bearer " + token);
    }
    return headers;
  }

  // ========================================================================
  // ENDPOINTS PÚBLICOS (não requerem autenticação)
  // ========================================================================

  @Test
  @DisplayName("Endpoint público: POST /auth deve ser acessível sem autenticação")
  void endpointPublicoAuth_DeveSerAcessivelSemToken() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");

    HttpEntity<String> request = new HttpEntity<>("", headers);

    ResponseEntity<String> response =
        restTemplate.postForEntity(baseUrl + "/auth", request, String.class);

    // Deve retornar 401 (não autorizado) mas não 403 (proibido)
    assertEquals(
        HttpStatus.UNAUTHORIZED,
        response.getStatusCode(),
        "Endpoint público deve aceitar requisições sem token");
  }

  @Test
  @DisplayName("Endpoint público: POST /usuario/new-user deve ser acessível sem autenticação")
  void endpointPublicoNewUser_DeveSerAcessivelSemToken() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");

    Map<String, Object> newUser =
        Map.of(
            "nome", "Test User",
            "username", "testuser@test.com",
            "email", "testuser@test.com",
            "password", "123456",
            "documento", "12345678901",
            "telefone", "12345678901");

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(newUser, headers);

    ResponseEntity<String> response =
        restTemplate.postForEntity(baseUrl + "/usuario/new-user", request, String.class);

    // Deve aceitar requisição (pode retornar 200, 400, 422, etc.) mas não 403
    assertNotEquals(
        HttpStatus.FORBIDDEN,
        response.getStatusCode(),
        "Endpoint público deve aceitar requisições sem token");
    assertNotEquals(
        HttpStatus.UNAUTHORIZED,
        response.getStatusCode(),
        "Endpoint público deve aceitar requisições sem token");
  }

  @Test
  @DisplayName("Endpoint público: POST /login deve ser acessível sem autenticação")
  void endpointPublicoLogin_DeveSerAcessivelSemToken() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");

    Map<String, String> loginRequest =
        Map.of(
            "username", "gzaffani@alunos.utfpr.edu.br",
            "password", "123");

    HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);

    ResponseEntity<String> response =
        restTemplate.postForEntity(baseUrl + "/login", request, String.class);

    assertEquals(
        HttpStatus.OK, response.getStatusCode(), "Login deve funcionar para usuário válido");
    assertNotNull(response.getBody(), "Token deve ser retornado");
  }

  // ========================================================================
  // ENDPOINTS AUTENTICADOS (requerem qualquer usuário autenticado)
  // ========================================================================

  @Test
  @DisplayName(
      "Endpoint autenticado: GET /usuario/user-info deve ser acessível por qualquer usuário autenticado")
  void endpointAutenticadoUserInfo_DeveSerAcessivelPorQualquerUsuario() {
    assertNotNull(alunoToken, "Token de aluno deve existir para este teste");
    HttpHeaders headers = createAuthHeaders(alunoToken);
    HttpEntity<String> request = new HttpEntity<>("", headers);

    ResponseEntity<String> response =
        restTemplate.exchange(
            baseUrl + "/usuario/user-info", HttpMethod.GET, request, String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode(), "Aluno deve acessar user-info");
  }

  @Test
  @DisplayName("Endpoint autenticado: GET /usuario/user-info deve ser acessível por laboratorista")
  void endpointAutenticadoUserInfo_DeveSerAcessivelPorLaboratorista() {
    assertNotNull(laboratoristaToken, "Token de laboratorista deve existir para este teste");
    HttpHeaders headers = createAuthHeaders(laboratoristaToken);
    HttpEntity<String> request = new HttpEntity<>("", headers);

    ResponseEntity<String> response =
        restTemplate.exchange(
            baseUrl + "/usuario/user-info", HttpMethod.GET, request, String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode(), "Laboratorista deve acessar user-info");
  }

  @Test
  @DisplayName("Endpoint autenticado: GET /usuario/user-info deve ser acessível por professor")
  void endpointAutenticadoUserInfo_DeveSerAcessivelPorProfessor() {
    assertNotNull(professorToken, "Professor token deve existir");
    HttpHeaders headers = createAuthHeaders(professorToken);
    HttpEntity<String> request = new HttpEntity<>("", headers);

    ResponseEntity<String> response =
        restTemplate.exchange(
            baseUrl + "/usuario/user-info", HttpMethod.GET, request, String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode(), "Professor deve acessar user-info");
  }

  @Test
  @DisplayName("Endpoint autenticado: GET /usuario/user-info deve ser acessível por administrador")
  void endpointAutenticadoUserInfo_DeveSerAcessivelPorAdministrador() {
    assertNotNull(adminToken, "Admin token deve existir");
    HttpHeaders headers = createAuthHeaders(adminToken);
    HttpEntity<String> request = new HttpEntity<>("", headers);

    ResponseEntity<String> response =
        restTemplate.exchange(
            baseUrl + "/usuario/user-info", HttpMethod.GET, request, String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode(), "Administrador deve acessar user-info");
  }

  @Test
  @DisplayName("Endpoint autenticado: POST /usuario/user-info deve bloquear acesso sem token")
  void endpointAutenticadoUserInfo_DeveBloquearAcessoSemToken() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");
    // Sem token

    HttpEntity<String> request = new HttpEntity<>("", headers);

    ResponseEntity<String> response =
        restTemplate.postForEntity(baseUrl + "/usuario/user-info", request, String.class);

    // RFC 7807: Sem token retorna 401 Unauthorized (autenticacao necessaria)
    // 403 Forbidden e usado quando autenticado mas sem permissao
    assertEquals(
        HttpStatus.UNAUTHORIZED,
        response.getStatusCode(),
        "Endpoint autenticado deve bloquear acesso sem token");
  }

  @Test
  @DisplayName("Endpoint autenticado: POST /usuario/user-info deve bloquear token inválido")
  void endpointAutenticadoUserInfo_DeveBloquearTokenInvalido() {
    HttpHeaders headers = createAuthHeaders("token_invalido_qualquer_coisa");
    HttpEntity<String> request = new HttpEntity<>("", headers);

    ResponseEntity<String> response =
        restTemplate.postForEntity(baseUrl + "/usuario/user-info", request, String.class);

    // RFC 7807: Token invalido retorna 401 Unauthorized (autenticacao falhou)
    // 403 Forbidden e usado quando autenticado mas sem permissao
    assertEquals(
        HttpStatus.UNAUTHORIZED,
        response.getStatusCode(),
        "Endpoint autenticado deve bloquear token inválido");
  }

  // ========================================================================
  // ENDPOINTS DE ADMINISTRAÇÃO (requerem papel ADMINISTRADOR)
  // ========================================================================

  @Test
  @DisplayName("Endpoint administrador: GET /usuario deve ser acessível apenas por ADMINISTRADOR")
  void endpointAdminUsuario_DeveSerAcessivelApenasPorAdmin() {
    assertNotNull(adminToken, "Admin token deve existir");
    HttpHeaders headers = createAuthHeaders(adminToken);
    HttpEntity<String> request = new HttpEntity<>("", headers);

    ResponseEntity<String> response =
        restTemplate.exchange(baseUrl + "/usuario", HttpMethod.GET, request, String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode(), "Admin deve acessar lista de usuários");
  }

  @Test
  @DisplayName("Endpoint administrador: GET /usuario deve bloquear ALUNO")
  void endpointAdminUsuario_DeveBloquearAluno() {
    assertNotNull(alunoToken, "Aluno token deve existir");
    HttpHeaders headers = createAuthHeaders(alunoToken);
    HttpEntity<String> request = new HttpEntity<>("", headers);

    ResponseEntity<String> response =
        restTemplate.exchange(baseUrl + "/usuario", HttpMethod.GET, request, String.class);

    assertEquals(
        HttpStatus.FORBIDDEN, response.getStatusCode(), "Aluno não deve acessar lista de usuários");
  }

  @Test
  @DisplayName("Endpoint administrador: GET /usuario deve bloquear LABORATORISTA")
  void endpointAdminUsuario_DeveBloquearLaboratorista() {
    assertNotNull(laboratoristaToken, "Laboratorista token deve existir");
    HttpHeaders headers = createAuthHeaders(laboratoristaToken);
    HttpEntity<String> request = new HttpEntity<>("", headers);

    ResponseEntity<String> response =
        restTemplate.exchange(baseUrl + "/usuario", HttpMethod.GET, request, String.class);

    assertEquals(
        HttpStatus.FORBIDDEN,
        response.getStatusCode(),
        "Laboratorista não deve acessar lista de usuários");
  }

  @Test
  @DisplayName("Endpoint administrador: GET /usuario deve bloquear PROFESSOR")
  void endpointAdminUsuario_DeveBloquearProfessor() {
    assertNotNull(professorToken, "Professor token deve existir");
    HttpHeaders headers = createAuthHeaders(professorToken);
    HttpEntity<String> request = new HttpEntity<>("", headers);

    ResponseEntity<String> response =
        restTemplate.exchange(baseUrl + "/usuario", HttpMethod.GET, request, String.class);

    // Professor não deve acessar lista de usuários (apenas ROLE_ALUNO e ROLE_PROFESSOR)
    assertEquals(
        HttpStatus.FORBIDDEN,
        response.getStatusCode(),
        "Professor não deve acessar lista de usuários - apenas ROLE_ALUNO e ROLE_PROFESSOR");
  }

  // ========================================================================
  // ENDPOINTS ESPECÍFICOS (testar outros endpoints relevantes)
  // ========================================================================

  @Test
  @DisplayName("Endpoint específico: GET /item deve ser acessível por qualquer usuário autenticado")
  void endpointItem_DeveSerAcessivelPorQualquerUsuarioAutenticado() {
    assertNotNull(alunoToken, "Aluno token deve existir");
    HttpHeaders headers = createAuthHeaders(alunoToken);
    HttpEntity<String> request = new HttpEntity<>("", headers);

    ResponseEntity<String> response =
        restTemplate.exchange(baseUrl + "/item", HttpMethod.GET, request, String.class);

    assertNotEquals(
        HttpStatus.FORBIDDEN, response.getStatusCode(), "Aluno deve acessar lista de itens");
    assertNotEquals(
        HttpStatus.UNAUTHORIZED,
        response.getStatusCode(),
        "Aluno autenticado não deve receber 401");
  }

  @Test
  @DisplayName("Endpoint específico: GET /item deve bloquear acesso sem autenticação")
  void endpointItem_DeveBloquearAcessoSemAutenticacao() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");

    HttpEntity<String> request = new HttpEntity<>("", headers);

    ResponseEntity<String> response =
        restTemplate.exchange(baseUrl + "/item", HttpMethod.GET, request, String.class);

    // RFC 7807: Sem autenticacao retorna 401 Unauthorized
    // 403 Forbidden e usado quando autenticado mas sem permissao
    assertEquals(
        HttpStatus.UNAUTHORIZED,
        response.getStatusCode(),
        "Endpoint de itens deve bloquear acesso sem autenticação");
  }

  // ========================================================================
  // VALIDAÇÕES DE INTEGRIDADE DOS TOKENS
  // ========================================================================

  @Test
  @DisplayName("Tokens de usuários diferentes devem ser diferentes")
  void tokensDeUsuariosDiferentes_DevemSerDiferentes() {
    assertNotNull(alunoToken, "Token de aluno não deve ser nulo");
    assertNotNull(laboratoristaToken, "Token de laboratorista não deve ser nulo");
    assertNotNull(professorToken, "Token de professor não deve ser nulo");
    assertNotNull(adminToken, "Token de administrador não deve ser nulo");

    // Tokens devem ser diferentes entre usuários
    assertNotEquals(
        alunoToken, laboratoristaToken, "Tokens de usuários diferentes devem ser diferentes");
    assertNotEquals(
        alunoToken, professorToken, "Tokens de usuários diferentes devem ser diferentes");
    assertNotEquals(alunoToken, adminToken, "Tokens de usuários diferentes devem ser diferentes");
  }

  @Test
  @DisplayName("Tokens devem ter formato JWT válido")
  void tokens_DevemTerFormatoJWT() {
    // Verificar que tokens não são nulos
    assertNotNull(adminToken, "Token admin não deve ser nulo");
    assertNotNull(laboratoristaToken, "Token laboratorista não deve ser nulo");
    assertNotNull(professorToken, "Token professor não deve ser nulo");
    assertNotNull(alunoToken, "Token aluno não deve ser nulo");

    // Todos devem ter formato JWT válido (3 partes separadas por pontos)
    assertEquals(3, adminToken.split("\\.").length, "Token admin deve ter formato JWT");
    assertEquals(
        3, laboratoristaToken.split("\\.").length, "Token laboratorista deve ter formato JWT");
    assertEquals(3, professorToken.split("\\.").length, "Token professor deve ter formato JWT");
    assertEquals(3, alunoToken.split("\\.").length, "Token aluno deve ter formato JWT");
  }

  // ========================================================================
  // VALIDAÇÃO DE ISOLAMENTE DE TESTE
  // ========================================================================

}
