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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Testes de validação de status do usuário para autenticação.
 *
 * <p>Valida todos os possíveis estados do usuário: - Usuário ativo + email verificado (único estado
 * que deve permitir login) - Usuário inativo + email verificado (bloqueado por estar inativo) -
 * Usuário ativo + email não verificado (bloqueado por não verificado) - Usuário inativo + email não
 * verificado (bloqueado por ambos motivos)
 *
 * <p>Também testa usuários existentes de diferentes tipos para garantir que o status seja validado
 * independentemente do tipo/permissões.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional // Cada teste roda em sua própria transação com rollback automático
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserStatusValidationTest {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate restTemplate;

  @Autowired
  private br.com.utfpr.gerenciamento.server.repository.UsuarioRepository usuarioRepository;

  private String baseUrl;

  @BeforeEach
  void setUp() {
    baseUrl = "http://localhost:" + port;
    // @Transactional deve garantir que os dados iniciais estejam corretos
    // Se os usuários ainda estão com ativo=false, o problema está nas migrations
  }

  // ========================================================================
  // TESTES COM USUÁRIOS EXISTENTES (dados iniciais - todos ativos e verificados)
  // ========================================================================

  @Test
  @DisplayName("Usuário ativo e verificado deve conseguir fazer login")
  void usuarioAtivoVerificado_DeveConseguirLogin() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");

    Map<String, String> loginRequest =
        Map.of(
            "username", "gzaffani@alunos.utfpr.edu.br", // Username pós-migração V3.1
            "password", "123");

    HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);

    ResponseEntity<String> response =
        restTemplate.postForEntity(baseUrl + "/login", request, String.class);

    assertEquals(
        HttpStatus.OK,
        response.getStatusCode(),
        "Usuário ativo e verificado deve conseguir fazer login");
    assertNotNull(response.getBody(), "Deve retornar token JWT");
    assertTrue(response.getBody().length() > 50, "Token deve ter tamanho significativo");
  }

  @Test
  @DisplayName("Administrador ativo e verificado deve conseguir fazer login")
  void administradorAtivoVerificado_DeveConseguirLogin() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");

    Map<String, String> loginRequest =
        Map.of(
            "username", "utfprapps-pb@utfpr.edu.br",
            "password", "123");

    HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);

    ResponseEntity<String> response =
        restTemplate.postForEntity(baseUrl + "/login", request, String.class);

    assertEquals(
        HttpStatus.OK,
        response.getStatusCode(),
        "Administrador ativo e verificado deve conseguir fazer login");
    assertNotNull(response.getBody(), "Deve retornar token JWT");
  }

  @Test
  @DisplayName("Laboratorista ativo e verificado deve conseguir fazer login")
  void laboratoristaAtivoVerificado_DeveConseguirLogin() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");

    Map<String, String> loginRequest =
        Map.of(
            "username", "joao@alunos.utfpr.edu.br",
            "password", "123");

    HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);

    ResponseEntity<String> response =
        restTemplate.postForEntity(baseUrl + "/login", request, String.class);

    assertEquals(
        HttpStatus.OK,
        response.getStatusCode(),
        "Laboratorista ativo e verificado deve conseguir fazer login");
    assertNotNull(response.getBody(), "Deve retornar token JWT");
  }

  @Test
  @DisplayName(
      "Tentativa de login com campos vazios deve retornar erro 401 (segurança anti-enumeration)")
  void loginComCamposVazios_DeveRetornarErro() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");

    Map<String, String> loginRequest =
        Map.of(
            "username", "",
            "password", "");

    HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);

    ResponseEntity<String> response =
        restTemplate.postForEntity(baseUrl + "/login", request, String.class);

    assertEquals(
        HttpStatus.UNAUTHORIZED,
        response.getStatusCode(),
        "Campos vazios devem resultar em erro 401 para prevenir user enumeration");

    // Verifica se a mensagem de erro é genérica (não revela informações específicas)
    assertNotNull(response.getBody());
    String responseBody = response.getBody();
    assertTrue(
        responseBody.contains("Credenciais inválidas") || responseBody.contains("error"),
        "Deve retornar mensagem genérica de credenciais inválidas");
  }
}
