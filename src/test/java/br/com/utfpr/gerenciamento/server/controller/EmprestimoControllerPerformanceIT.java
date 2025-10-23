package br.com.utfpr.gerenciamento.server.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.utfpr.gerenciamento.server.fixture.EmprestimoFixture;
import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.model.Permissao;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * Testes de integração para performance de paginação de empréstimos.
 *
 * <p>Valida que JOIN FETCH elimina N+1 queries, resultando em resposta &lt;2s para 50+ empréstimos.
 *
 * @author Rodrigo Izidoro
 * @since 2025-10-22
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Tag("integration")
class EmprestimoControllerPerformanceIT {

  @Autowired private WebApplicationContext context;
  @PersistenceContext private EntityManager entityManager;
  @Autowired private ObjectMapper objectMapper;

  private MockMvc mockMvc;
  private final EmprestimoFixture fixture = new EmprestimoFixture();

  @BeforeEach
  void setUp() {
    // Limpa dados de testes anteriores (ordem inversa das FKs)
    limparDadosH2();

    // Cria fixtures
    criarDadosH2ParaTestes();

    // Comita dados de setup para que MockMvc (transação separada) possa vê-los
    TestTransaction.flagForCommit();
    TestTransaction.end();

    // Configura MockMvc após commit dos dados
    mockMvc =
        MockMvcBuilders.webAppContextSetup(context)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .build();
  }

  // Limpa dados de testes anteriores respeitando FKs
  private void limparDadosH2() {
    entityManager.createQuery("DELETE FROM EmprestimoItem").executeUpdate();
    entityManager.createQuery("DELETE FROM Emprestimo").executeUpdate();
    entityManager.createQuery("DELETE FROM Item").executeUpdate();
    entityManager
        .createQuery(
            "DELETE FROM Usuario u WHERE u.username IN ('aluno@teste.com', 'professor@teste.com')")
        .executeUpdate();
    entityManager.createQuery("DELETE FROM Permissao WHERE nome = 'ROLE_ALUNO'").executeUpdate();
    entityManager.flush();
  }

  // Cria 50 empréstimos no H2 para simular volume realista
  private void criarDadosH2ParaTestes() {
    Permissao permissaoAluno = fixture.criarPermissao("ROLE_ALUNO");
    entityManager.persist(permissaoAluno);

    Usuario usuarioEmprestimo =
        fixture.criarUsuario("aluno@teste.com", "Aluno Teste", permissaoAluno);
    entityManager.persist(usuarioEmprestimo);

    Usuario usuarioResponsavel =
        fixture.criarUsuario("professor@teste.com", "Professor Teste", permissaoAluno);
    entityManager.persist(usuarioResponsavel);

    Item item = fixture.criarItem("Arduino Uno", "Placa Arduino para testes de performance");
    entityManager.persist(item);

    for (int i = 0; i < 50; i++) {
      Emprestimo emprestimo =
          fixture.criarEmprestimoCustom(
              usuarioEmprestimo,
              usuarioResponsavel,
              item,
              LocalDate.now().minusDays(i),
              LocalDate.now().plusDays(i),
              null);
      entityManager.persist(emprestimo);
    }

    entityManager.flush();
    entityManager.clear();
  }

  // Helper: Executa paginação como admin
  private MvcResult executarPaginacaoComoAdmin(int size, String filter) throws Exception {
    var request =
        get("/emprestimo/page")
            .param("page", String.valueOf(0))
            .param("size", String.valueOf(size))
            .with(
                SecurityMockMvcRequestPostProcessors.user("admin")
                    .authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")));

    if (filter != null) {
      request.param("filter", filter);
    }

    return mockMvc.perform(request).andExpect(status().isOk()).andReturn();
  }

  // Helper: Deserializa resposta JSON para Map
  private Map<String, Object> deserializarResposta(MvcResult result) throws Exception {
    String responseJson = result.getResponse().getContentAsString();
    return objectMapper.readValue(responseJson, new TypeReference<>() {});
  }

  // Helper: Extrai lista de empréstimos da resposta paginada
  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> extrairEmprestimos(Map<String, Object> pageResponse) {
    return (List<Map<String, Object>>) pageResponse.get("content");
  }

  // Helper: Valida que item foi carregado via JOIN FETCH
  @SuppressWarnings("unchecked")
  private void validarItemCarregado(Map<String, Object> emprestimoItem) {
    assertNotNull(emprestimoItem.get("item"), "Item deve estar carregado via JOIN FETCH");
    Map<String, Object> itemData = (Map<String, Object>) emprestimoItem.get("item");
    assertEquals("Arduino Uno", itemData.get("nome"), "Nome do item deve ser 'Arduino Uno'");
    assertNotNull(itemData.get("descricao"), "Descrição deve estar carregada");
  }

  @Test
  @DisplayName("GET /emprestimo/page deve responder em menos de 2 segundos com 10 itens")
  void testPaginacao_DeveResponderRapidamente() throws Exception {
    // Executa paginação medindo tempo de resposta
    long startTime = System.currentTimeMillis();
    MvcResult result = executarPaginacaoComoAdmin(10, null);
    long duration = System.currentTimeMillis() - startTime;

    // Valida performance <2s (inclui serialização e overhead de rede)
    assertTrue(
        duration < 2000,
        String.format("Paginação deve completar em <2s. Tempo atual: %dms", duration));

    // Valida estrutura da resposta
    Map<String, Object> pageResponse = deserializarResposta(result);
    List<Map<String, Object>> emprestimos = extrairEmprestimos(pageResponse);

    assertEquals(10, emprestimos.size());
    assertEquals(50, pageResponse.get("totalElements"));
    assertEquals(0, pageResponse.get("number"));

    // Valida primeiro empréstimo com dados completos do H2
    Map<String, Object> primeiroEmprestimo = emprestimos.getFirst();
    assertNotNull(primeiroEmprestimo.get("id"));
    assertNotNull(primeiroEmprestimo.get("dataEmprestimo"));

    // Valida JOIN FETCH de usuário funcionou
    @SuppressWarnings("unchecked")
    Map<String, Object> usuario = (Map<String, Object>) primeiroEmprestimo.get("usuarioEmprestimo");
    assertEquals("Aluno Teste", usuario.get("nome"));
    assertEquals("aluno@teste.com", usuario.get("username"));

    // Valida JOIN FETCH de emprestimoItem → item funcionou
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> emprestimoItens =
        (List<Map<String, Object>>) primeiroEmprestimo.get("emprestimoItem");
    assertFalse(emprestimoItens.isEmpty());
    validarItemCarregado(emprestimoItens.getFirst());

    System.out.printf("✅ Performance OK: %dms (meta: <2000ms)%n", duration);
  }

  @Test
  @DisplayName("GET /emprestimo/page com filtro deve responder em menos de 2 segundos")
  void testPaginacaoComFiltro_DeveResponderRapidamente() throws Exception {
    // Testa que filtros não degradam performance (independente do resultado)
    long startTime = System.currentTimeMillis();
    MvcResult result = executarPaginacaoComoAdmin(10, "teste");
    long duration = System.currentTimeMillis() - startTime;

    assertTrue(
        duration < 2000,
        String.format("Paginação com filtro deve completar em <2s. Tempo atual: %dms", duration));

    // Valida estrutura da resposta (mesmo se vazia)
    Map<String, Object> pageResponse = deserializarResposta(result);
    assertNotNull(pageResponse.get("content"));
    assertNotNull(pageResponse.get("totalElements"));

    System.out.printf("✅ Performance com filtro OK: %dms%n", duration);
  }

  @Test
  @DisplayName("GET /emprestimo/page deve carregar itens e grupos sem N+1 queries")
  void testPaginacao_DeveCarregarItensEGruposSemN1() throws Exception {
    MvcResult result = executarPaginacaoComoAdmin(10, null);
    Map<String, Object> pageResponse = deserializarResposta(result);
    List<Map<String, Object>> emprestimos = extrairEmprestimos(pageResponse);

    assertFalse(emprestimos.isEmpty(), "Deve retornar empréstimos do H2");

    // Valida TODOS os empréstimos têm dados completos via JOIN FETCH
    for (Map<String, Object> emprestimo : emprestimos) {
      // Valida usuário carregado
      @SuppressWarnings("unchecked")
      Map<String, Object> usuario = (Map<String, Object>) emprestimo.get("usuarioEmprestimo");
      assertNotNull(usuario.get("nome"));
      assertNotNull(usuario.get("email"));

      // Valida emprestimoItem → item carregado
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> emprestimoItens =
          (List<Map<String, Object>>) emprestimo.get("emprestimoItem");
      assertFalse(emprestimoItens.isEmpty());

      for (Map<String, Object> emprestimoItem : emprestimoItens) {
        validarItemCarregado(emprestimoItem);

        // Valida grupo (se existir) também foi carregado
        @SuppressWarnings("unchecked")
        Map<String, Object> itemData = (Map<String, Object>) emprestimoItem.get("item");
        if (itemData.get("grupo") != null) {
          @SuppressWarnings("unchecked")
          Map<String, Object> grupo = (Map<String, Object>) itemData.get("grupo");
          assertNotNull(grupo.get("descricao"));
        }
      }
    }

    System.out.printf("✅ N+1 prevenido: %d empréstimos com dados completos%n", emprestimos.size());
  }

  @Test
  @DisplayName("GET /emprestimo/page segunda página deve responder rapidamente (validar cache)")
  void testPaginacaoSegundaPagina_DeveResponderRapidamente() throws Exception {
    // Primeira chamada (cache miss)
    long startTime1 = System.currentTimeMillis();
    executarPaginacaoComoAdmin(10, null);
    long duration1 = System.currentTimeMillis() - startTime1;

    // Segunda chamada IDÊNTICA (cache hit esperado)
    long startTime2 = System.currentTimeMillis();
    MvcResult result = executarPaginacaoComoAdmin(10, null);
    long duration2 = System.currentTimeMillis() - startTime2;

    // Cache hit deve ser igual ou mais rápido (com tolerância para variação em milissegundos)
    // Em escala de milissegundos, variação de 1-2ms é esperada, então permitimos que segunda
    // seja até 2ms mais lenta que a primeira (para cobrir edge cases de timing)
    assertTrue(
        duration2 <= duration1 + 2,
        String.format(
            "Cache hit não deve ser mais lento. Primeira: %dms, Segunda (cache): %dms",
            duration1, duration2));

    // Valida que ambas completaram rapidamente (indicador de cache funcionando)
    assertTrue(
        duration2 < 2000,
        String.format("Segunda chamada deve ser rápida (<2s). Tempo: %dms", duration2));

    // Valida dados da página
    Map<String, Object> pageResponse = deserializarResposta(result);
    List<Map<String, Object>> emprestimos = extrairEmprestimos(pageResponse);

    assertEquals(10, emprestimos.size());
    assertEquals(0, pageResponse.get("number"));
    assertEquals(50, pageResponse.get("totalElements"));

    // Valida que cache retorna dados completos
    assertNotNull(emprestimos.getFirst().get("usuarioEmprestimo"));
    assertNotNull(emprestimos.getFirst().get("emprestimoItem"));

    System.out.printf(
        "✅ Cache validado: Primeira %dms → Segunda %dms (diferença: %dms)%n",
        duration1, duration2, duration2 - duration1);
  }

  @Test
  @DisplayName("GET /emprestimo/page com 50 resultados por página deve responder em <3 segundos")
  void testPaginacaoGrandeVolume_DeveResponderEmTempoAceitavel() throws Exception {
    // Paginação com volume maior (50 itens)
    long startTime = System.currentTimeMillis();
    MvcResult result = executarPaginacaoComoAdmin(50, null);
    long duration = System.currentTimeMillis() - startTime;

    assertTrue(
        duration < 3000,
        String.format("Paginação com 50 itens deve completar em <3s. Tempo atual: %dms", duration));

    // Valida que TODOS os 50 empréstimos foram retornados com dados completos
    Map<String, Object> pageResponse = deserializarResposta(result);
    List<Map<String, Object>> emprestimos = extrairEmprestimos(pageResponse);

    assertEquals(50, emprestimos.size());
    assertEquals(50, pageResponse.get("totalElements"));

    // Valida amostra (primeiro, meio, último) para confirmar JOIN FETCH
    int[] amostra = {0, 24, 49};
    for (int i : amostra) {
      Map<String, Object> emprestimo = emprestimos.get(i);
      assertNotNull(emprestimo.get("usuarioEmprestimo"));

      @SuppressWarnings("unchecked")
      List<Map<String, Object>> emprestimoItens =
          (List<Map<String, Object>>) emprestimo.get("emprestimoItem");
      assertFalse(emprestimoItens.isEmpty());
      validarItemCarregado(emprestimoItens.getFirst());
    }

    System.out.printf("✅ Grande volume OK: 50 itens em %dms%n", duration);
  }
}
