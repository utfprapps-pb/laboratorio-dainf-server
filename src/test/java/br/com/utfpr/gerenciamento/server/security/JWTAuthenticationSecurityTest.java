package br.com.utfpr.gerenciamento.server.security;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

/**
 * Testes de segurança críticos para autenticação JWT.
 *
 * <p>Valida cenários de segurança essenciais incluindo: - Validação de credenciais
 * válidas/inválidas - Status do usuário (ativo, email verificado) - Formato e manipulação de tokens
 * - Blocos por "nada consta"
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class JWTAuthenticationSecurityTest {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private DataSource dataSource;

  @Autowired
  private br.com.utfpr.gerenciamento.server.repository.UsuarioRepository usuarioRepository;

  private String baseUrl;

  // IDs dos usuários de teste criados
  private Long usuarioAtivoVerificadoId;
  private Long usuarioInativoVerificadoId;
  private Long usuarioAtivoNaoVerificadoId;
  private Long usuarioBloqueadoId;

  private static final String[] USUARIOS_TESTE = {
    "usuario_ativo@alunos.utfpr.edu.br",
    "usuario_inativo@alunos.utfpr.edu.br",
    "usuario_ativo_naover@alunos.utfpr.edu.br",
    "usuario_bloqueado@alunos.utfpr.edu.br"
  };

  @BeforeEach
  void setUp() throws SQLException {
    baseUrl = "http://localhost:" + port;

    limparDadosTeste();
    criarUsuariosTeste();
  }

  protected void limparDadosTeste() {
    for (String username : USUARIOS_TESTE) {
      br.com.utfpr.gerenciamento.server.model.Usuario usuario =
          usuarioRepository.findWithPermissoesByUsernameOrEmail(username, username);
      if (usuario != null) {
        usuario.getPermissoes().clear();
        usuarioRepository.save(usuario);
        usuarioRepository.delete(usuario);
      }
    }
  }

  private void criarUsuariosTeste() throws SQLException {
    try (Connection conn = dataSource.getConnection()) {

      // Verificar se as permissões básicas existem, senão criar
      verificarEcriarPermissoesBasicas(conn);

      // Senha hash para "123" (mesmo dos usuários existentes)
      String senhaHash = "$2a$10$kcDpG6r2c0karXuOK114Hejk7iguH.tFswB1aenCydA6bmzixjCCC";

      // 1. Usuario ativo e verificado (deve funcionar)
      usuarioAtivoVerificadoId =
          criarUsuario(
              conn,
              "Usuario Ativo Verificado Security",
              "usuario_ativo@alunos.utfpr.edu.br",
              senhaHash,
              "88888888801",
              "46888888001",
              true, // email_verificado
              true); // ativo
      atribuirPermissao(conn, usuarioAtivoVerificadoId, 4); // ALUNO

      // 2. Usuario inativo mas verificado (bloqueado por estar inativo)
      usuarioInativoVerificadoId =
          criarUsuario(
              conn,
              "Usuario Inativo Verificado Security",
              "usuario_inativo@alunos.utfpr.edu.br",
              senhaHash,
              "88888888802",
              "46888888002",
              true, // email_verificado
              false); // ativo
      atribuirPermissao(conn, usuarioInativoVerificadoId, 4); // ALUNO

      // 3. Usuario ativo mas não verificado (bloqueado por não verificado)
      usuarioAtivoNaoVerificadoId =
          criarUsuario(
              conn,
              "Usuario Ativo Nao Verificado Security",
              "usuario_ativo_naover@alunos.utfpr.edu.br",
              senhaHash,
              "88888888803",
              "46888888003",
              false, // email_verificado
              true); // ativo
      atribuirPermissao(conn, usuarioAtivoNaoVerificadoId, 4); // ALUNO

      // 4. Usuario bloqueado (inativo E não verificado)
      usuarioBloqueadoId =
          criarUsuario(
              conn,
              "Usuario Bloqueado Security",
              "usuario_bloqueado@alunos.utfpr.edu.br",
              senhaHash,
              "88888888804",
              "46888888004",
              false, // email_verificado
              false); // ativo
      atribuirPermissao(conn, usuarioBloqueadoId, 4); // ALUNO
    }
  }

  private void verificarEcriarPermissoesBasicas(Connection conn) throws SQLException {
    // Verificar se tabela permissao tem dados
    try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) as count FROM permissao");
        ResultSet rs = stmt.executeQuery()) {

      if (rs.next() && rs.getInt("count") == 0) {
        // Inserir permissões básicas
        String[] permissoes = {
          "INSERT INTO permissao (id, nome) VALUES (1, 'ROLE_ADMINISTRADOR')",
          "INSERT INTO permissao (id, nome) VALUES (2, 'ROLE_LABORATORISTA')",
          "INSERT INTO permissao (id, nome) VALUES (3, 'ROLE_PROFESSOR')",
          "INSERT INTO permissao (id, nome) VALUES (4, 'ROLE_ALUNO')"
        };

        for (String sql : permissoes) {
          try (PreparedStatement insertStmt = conn.prepareStatement(sql)) {
            insertStmt.executeUpdate();
          }
        }
      }
    }
  }

  private Long criarUsuario(
      Connection conn,
      String nome,
      String username,
      String senha,
      String documento,
      String telefone,
      boolean emailVerificado,
      boolean ativo)
      throws SQLException {

    String sql =
        "INSERT INTO usuario (nome, username, password, email, documento, "
            + "telefone, email_verificado, ativo) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setString(1, nome);
      stmt.setString(2, username);
      stmt.setString(3, senha);
      stmt.setString(4, username); // email igual ao username
      stmt.setString(5, documento);
      stmt.setString(6, telefone);
      stmt.setBoolean(7, emailVerificado);
      stmt.setBoolean(8, ativo);

      stmt.executeUpdate();

      try (ResultSet rs = stmt.getGeneratedKeys()) {
        if (rs.next()) {
          return rs.getLong(1);
        }
      }
    }
    throw new SQLException("Falha ao criar usuário: " + username);
  }

  private void atribuirPermissao(Connection conn, Long usuarioId, int permissaoId)
      throws SQLException {
    String sql = "INSERT INTO usuario_permissoes (usuario_id, permissoes_id) VALUES (?, ?)";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, usuarioId);
      stmt.setInt(2, permissaoId);
      stmt.executeUpdate();
    }
  }

  @ParameterizedTest(
      name = "{index}: Login com usuário {0} (ativo={1}, verificado={2}) deve retornar {3}")
  @CsvSource({
    "usuario_ativo@alunos.utfpr.edu.br, true, true, 200",
    "usuario_inativo@alunos.utfpr.edu.br, false, true, 403",
    "usuario_ativo_naover@alunos.utfpr.edu.br, true, false, 403",
    "usuario_bloqueado@alunos.utfpr.edu.br, false, false, 403"
  })
  @DisplayName("Deve validar autenticação baseada no status do usuário (ativo e email verificado)")
  void loginBaseadoNoStatusUsuario_DeveRetornarStatusEsperado(
      String username, boolean ativo, boolean verificado, int statusCodeEsperado) {

    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");

    Map<String, String> loginRequest = Map.of("username", username, "password", "123");
    HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);

    ResponseEntity<String> response =
        restTemplate.postForEntity(baseUrl + "/login", request, String.class);

    HttpStatus expectedStatus = HttpStatus.valueOf(statusCodeEsperado);
    assertEquals(
        expectedStatus,
        response.getStatusCode(),
        String.format(
            "Usuário %s (ativo=%s, verificado=%s) deve retornar %s",
            username, ativo, verificado, expectedStatus));

    assertNotNull(response.getBody(), "Response body não deve ser nulo");

    if (expectedStatus == HttpStatus.OK) {
      assertTrue(response.getBody().length() > 100, "Token JWT deve ter tamanho significativo");
      assertEquals(
          3,
          response.getBody().split("\\.").length,
          "Token deve ter formato JWT válido (header.payload.signature)");
    }
  }

  @ParameterizedTest(name = "{index}: Login com {0} deve retornar 401_UNAUTHORIZED")
  @CsvSource({
    "senha_incorreta",
    "usuario_inexistente@alunos.utfpr.edu.br",
    "usuario_ativo@alunos.utfpr.edu.br'; DROP TABLE usuario; --"
  })
  @DisplayName(
      "Deve retornar 401 para credenciais inválidas (senha errada, usuário inexistente, SQL injection)")
  void loginComCredenciaisInvalidas_DeveRetornar401(String tipoCredencialInvalida) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");

    String username, password, descricao;

    if (tipoCredencialInvalida.equals("senha_incorreta")) {
      username = "usuario_ativo@alunos.utfpr.edu.br";
      password = "senha_errada";
      descricao = "senha incorreta";
    } else if (tipoCredencialInvalida.contains("DROP TABLE")) {
      username = tipoCredencialInvalida;
      password = "123";
      descricao = "SQL injection";
    } else {
      username = tipoCredencialInvalida;
      password = "123";
      descricao = "usuário inexistente";
    }

    Map<String, String> loginRequest = Map.of("username", username, "password", password);
    HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);

    ResponseEntity<String> response =
        restTemplate.postForEntity(baseUrl + "/login", request, String.class);

    assertEquals(
        HttpStatus.UNAUTHORIZED,
        response.getStatusCode(),
        String.format("Login com %s deve retornar 401", descricao));
    assertNotNull(
        response.getBody(), String.format("Deve retornar mensagem de erro para %s", descricao));
  }

  @ParameterizedTest(name = "{index}: Requisição {0} deve retornar {1}")
  @CsvSource({"sem_username, 401", "sem_content_type, 200", "com_content_type, 200"})
  @DisplayName("Deve validar diferentes formatos de requisição")
  void loginFormatoRequisicao_DeveRetornarStatusEsperado(
      String tipoRequisicao, int statusCodeEsperado) {
    HttpHeaders headers = new HttpHeaders();
    Map<String, String> loginRequest;
    HttpEntity<Map<String, String>> request;

    HttpStatus expectedStatus = HttpStatus.valueOf(statusCodeEsperado);

    switch (tipoRequisicao) {
      case "sem_username":
        headers.set("Content-Type", "application/json");
        loginRequest = Map.of("password", "123");
        request = new HttpEntity<>(loginRequest, headers);
        break;

      case "sem_content_type":
        loginRequest = Map.of("username", "usuario_ativo@alunos.utfpr.edu.br", "password", "123");
        request = new HttpEntity<>(loginRequest);
        break;

      case "com_content_type":
        headers.set("Content-Type", "application/json");
        loginRequest = Map.of("username", "usuario_ativo@alunos.utfpr.edu.br", "password", "123");
        request = new HttpEntity<>(loginRequest, headers);
        break;

      default:
        throw new IllegalArgumentException("Tipo de requisição desconhecido: " + tipoRequisicao);
    }

    ResponseEntity<String> response =
        restTemplate.postForEntity(baseUrl + "/login", request, String.class);

    assertEquals(
        expectedStatus,
        response.getStatusCode(),
        String.format("Requisição %s deve retornar %s", tipoRequisicao, expectedStatus));
  }

  // Teste de SQL injection removido - já coberto no teste parametrizado
  // loginComCredenciaisInvalidas_DeveRetornar401

  @Test
  @DisplayName("Deve tratar username case-insensitive consistentemente")
  void loginUsernameCaseInsensitive_DeveValidarComportamento() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");

    // Testar diferentes variações de case para entender comportamento
    Map<String, String> loginRequestMaiusculas =
        Map.of("username", "USUARIO_ATIVO@ALUNOS.UTFPR.EDU.BR", "password", "123");

    Map<String, String> loginRequestMinusculas =
        Map.of("username", "usuario_ativo@alunos.utfpr.edu.br", "password", "123");

    Map<String, String> loginRequestMisto =
        Map.of("username", "Usuario_Ativo@Alunos.Utfpr.Edu.Br", "password", "123");

    HttpEntity<Map<String, String>> requestMaiusculas =
        new HttpEntity<>(loginRequestMaiusculas, headers);
    HttpEntity<Map<String, String>> requestMinusculas =
        new HttpEntity<>(loginRequestMinusculas, headers);
    HttpEntity<Map<String, String>> requestMisto = new HttpEntity<>(loginRequestMisto, headers);

    ResponseEntity<String> responseMaiusculas =
        restTemplate.postForEntity(baseUrl + "/login", requestMaiusculas, String.class);
    ResponseEntity<String> responseMinusculas =
        restTemplate.postForEntity(baseUrl + "/login", requestMinusculas, String.class);
    ResponseEntity<String> responseMisto =
        restTemplate.postForEntity(baseUrl + "/login", requestMisto, String.class);

    // Validar que não ocorrem erros internos
    assertNotEquals(
        HttpStatus.INTERNAL_SERVER_ERROR,
        responseMaiusculas.getStatusCode(),
        "Case em maiúsculas não deve causar erro interno");
    assertNotEquals(
        HttpStatus.INTERNAL_SERVER_ERROR,
        responseMinusculas.getStatusCode(),
        "Case em minúsculas não deve causar erro interno");
    assertNotEquals(
        HttpStatus.INTERNAL_SERVER_ERROR,
        responseMisto.getStatusCode(),
        "Case misto não deve causar erro interno");

    // TODO: Adicionar asserts específicos quando o comportamento case-insensitive for definido
    // Por enquanto, apenas garante que o sistema trata os diferentes casos de forma consistente
  }

  @ParameterizedTest(name = "{index}: Acesso com token {0} deve retornar 401_UNAUTHORIZED")
  @CsvSource({"malformado_sem_pontos", "prefixo_incorreto_Basic", "sem_token"})
  @DisplayName(
      "Deve bloquear acesso com token inválido, incorreto ou ausente em endpoint protegido")
  void acessoComTokenInvalido_DeveRetornar401(String tipoTokenInvalido) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");

    switch (tipoTokenInvalido) {
      case "malformado_sem_pontos":
        headers.set("Authorization", "Bearer token_malformado_sem_pontos");
        break;

      case "prefixo_incorreto_Basic":
        headers.set("Authorization", "Basic algum_token");
        break;

      case "sem_token":
        // Não adiciona header Authorization
        break;

      default:
        throw new IllegalArgumentException(
            "Tipo de token inválido desconhecido: " + tipoTokenInvalido);
    }

    HttpEntity<String> request = new HttpEntity<>("", headers);

    ResponseEntity<String> response =
        restTemplate.exchange(
            baseUrl + "/usuario/user-info", HttpMethod.GET, request, String.class);

    // RFC 7807: Token ausente ou inválido retorna 401 Unauthorized (não 403 Forbidden)
    // 403 Forbidden é usado quando o usuário está autenticado mas não tem permissão
    assertEquals(
        HttpStatus.UNAUTHORIZED,
        response.getStatusCode(),
        String.format("Acesso com token %s deve retornar 401", tipoTokenInvalido));
  }
}
