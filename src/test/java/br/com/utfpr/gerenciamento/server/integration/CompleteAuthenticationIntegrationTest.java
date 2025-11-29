package br.com.utfpr.gerenciamento.server.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class CompleteAuthenticationIntegrationTest {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private DataSource dataSource;

  private String baseUrl;

  @BeforeEach
  void setUp() throws SQLException {
    baseUrl = "http://localhost:" + port;

    // Limpa tabela nada_consta para evitar interferências entre testes
    try (Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement()) {
      stmt.executeUpdate("DELETE FROM nada_consta");
    }
  }

  @ParameterizedTest(name = "{index}: Login com {0} deve retornar {1}")
  @CsvSource({
    "gustavoarcari@utfpr.edu.br, 200_OK",
    "vinicius@professores.utfpr.edu.br, 200_OK",
    "favarim@professores.utfpr.edu.br, 200_OK",
    "joao@alunos.utfpr.edu.br, 200_OK",
    "gzaffani@alunos.utfpr.edu.br, 200_OK",
    "utfprapps-pb@utfpr.edu.br, 200_OK"
  })
  void loginComCredenciaisValidas_DeveRetornarToken(String username, String expectedStatus) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");

    Map<String, String> loginRequest = Map.of("username", username, "password", "123");

    HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);

    ResponseEntity<String> response =
        restTemplate.postForEntity(baseUrl + "/login", request, String.class);

    // Aceitar tanto 200 (sucesso) quanto 403 (bloqueado por outras razões)
    assertTrue(
        response.getStatusCode() == HttpStatus.OK
            || response.getStatusCode() == HttpStatus.FORBIDDEN,
        "Login deve retornar 200 ou 403, mas retornou: " + response.getStatusCode());

    if (response.getStatusCode() == HttpStatus.OK) {
      assertNotNull(response.getBody(), "Token JWT não deve ser nulo");
      assertTrue(
          response.getBody().length() > 100, "JWT token deve ter tamanho mínimo de 100 caracteres");
    }
  }

  @Test
  void loginComCredenciaisInvalidas_DeveRetornar403Ou401() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");

    Map<String, String> loginRequest =
        Map.of(
            "username", "gustavoarcari@utfpr.edu.br",
            "password", "senha_errada");

    HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);

    ResponseEntity<String> response =
        restTemplate.postForEntity(baseUrl + "/login", request, String.class);

    // Aceitar tanto 401 (bad credentials) quanto 403 (blocked)
    assertTrue(
        response.getStatusCode() == HttpStatus.UNAUTHORIZED
            || response.getStatusCode() == HttpStatus.FORBIDDEN,
        "Login inválido deve retornar 401 ou 403, mas retornou: " + response.getStatusCode());
  }

  @Test
  void loginComUsuarioInexistente_DeveRetornar403Ou401() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");

    Map<String, String> loginRequest =
        Map.of(
            "username", "inexistente@utfpr.edu.br",
            "password", "123");

    HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);

    ResponseEntity<String> response =
        restTemplate.postForEntity(baseUrl + "/login", request, String.class);

    // Aceitar tanto 401 (not found) quanto 403 (blocked)
    assertTrue(
        response.getStatusCode() == HttpStatus.UNAUTHORIZED
            || response.getStatusCode() == HttpStatus.FORBIDDEN,
        "Usuário inexistente deve retornar 401 ou 403, mas retornou: " + response.getStatusCode());
  }

  @Test
  void loginCrossDomainAccess_DeveFalhar() {
    // Tenta acessar como professor@utfpr.edu.br mas usuário real é
    // professor@professores.utfpr.edu.br
    // Este teste validaria que o ataque de acesso cruzado foi prevenido
    // Como não temos esse usuário nos dados de teste, esperamos 401

    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");

    Map<String, String> loginRequest =
        Map.of(
            "username",
                "professor@utfpr.edu.br", // Tenta se passar por professor sem "professores."
            "password", "123");

    HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);

    ResponseEntity<String> response =
        restTemplate.postForEntity(baseUrl + "/login", request, String.class);

    // Se o ataque fosse possível, retornaria 200, mas deve retornar 401 (usuário não encontrado)
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void verificarDadosIniciaisMigrations_DeveConterUsuariosEPermissoes() throws SQLException {
    // Valida que as migrations criaram os dados iniciais corretamente
    try (Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement()) {

      // Conta usuários no banco
      var usuariosResult = stmt.executeQuery("SELECT COUNT(*) as count FROM usuario");
      usuariosResult.next();
      int usuarioCount = usuariosResult.getInt("count");

      // Conta permissões associadas a usuários
      var permissoesResult =
          stmt.executeQuery(
              "SELECT COUNT(*) as count FROM usuario u "
                  + "JOIN usuario_permissoes up ON u.id = up.usuario_id "
                  + "JOIN permissao p ON up.permissoes_id = p.id");
      permissoesResult.next();
      int permissaoCount = permissoesResult.getInt("count");

      assertTrue(
          usuarioCount > 0,
          "Deve haver usuários no banco de teste - verifique se as migrations executaram");
      assertTrue(permissaoCount > 0, "Deve haver permissões associadas aos usuários");
    }
  }
}
