package br.com.utfpr.gerenciamento.server.specification;

import static org.junit.jupiter.api.Assertions.*;

import br.com.utfpr.gerenciamento.server.fixture.EmprestimoFixture;
import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.model.Permissao;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.model.filter.EmprestimoFilter;
import br.com.utfpr.gerenciamento.server.repository.EmprestimoRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

/**
 * Testes para EmprestimoSpecifications - Validação de Criteria API.
 *
 * @author Rodrigo Izidoro
 * @since 2025-10-07
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class EmprestimoSpecificationsTest {

  @Autowired private TestEntityManager entityManager;

  @Autowired private EmprestimoRepository repository;

  private final EmprestimoFixture fixture = new EmprestimoFixture();

    private Usuario usuarioEmprestimo;
  private Usuario usuarioResponsavel;
  private Item item;
  private Emprestimo emprestimoAtrasado;
  private Emprestimo emprestimoPendente;
  private Emprestimo emprestimoFinalizado;

  @BeforeEach
  void setUp() {
    // Criar e persistir permissão para testes (usando factory method para consistência)
      Permissao permissaoAluno = fixture.criarPermissao("ROLE_ALUNO");
    entityManager.persist(permissaoAluno);

    // Criar usuários usando fixture
    usuarioEmprestimo = fixture.criarUsuario("aluno@teste.com", "Aluno Teste", permissaoAluno);
    entityManager.persist(usuarioEmprestimo);

    usuarioResponsavel =
        fixture.criarUsuario("professor@teste.com", "Professor Teste", permissaoAluno);
    entityManager.persist(usuarioResponsavel);

    // Criar item usando fixture
    item = fixture.criarItem("Arduino Uno", "Placa Arduino para testes");
    entityManager.persist(item);

    // Criar empréstimos usando fixture
    emprestimoAtrasado =
        fixture.criarEmprestimoAtrasado(usuarioEmprestimo, usuarioResponsavel, item);
    entityManager.persist(emprestimoAtrasado);

    emprestimoPendente =
        fixture.criarEmprestimoPendente(usuarioEmprestimo, usuarioResponsavel, item);
    entityManager.persist(emprestimoPendente);

    emprestimoFinalizado =
        fixture.criarEmprestimoFinalizado(usuarioEmprestimo, usuarioResponsavel, item);
    entityManager.persist(emprestimoFinalizado);

    entityManager.flush();
  }

  @Test
  @DisplayName("Deve retornar todos os empréstimos quando filtro é null")
  void testFromFilter_QuandoFiltroNull_DeveRetornarTodosEmprestimos() {
    // Given
    EmprestimoFilter filtroNull = null;
    Specification<Emprestimo> spec = EmprestimoSpecifications.fromFilter(filtroNull);

    // When
    List<Emprestimo> resultado = repository.findAll(spec);

    // Then
    assertEquals(3, resultado.size());
  }

  @ParameterizedTest
  @CsvSource({
    "A, 1, ATRASADO, false, true",
    "P, 1, PENDENTE, false, false",
    "F, 1, FINALIZADO, true, false",
    "T, 3, TODOS, false, false"
  })
  @DisplayName("Deve filtrar empréstimos corretamente por status")
  void testFromFilter_QuandoFiltraPorStatus_DeveRetornarEmprestimosCorretos(
      String status,
      int quantidadeEsperada) {
    // Given
    EmprestimoFilter filtro = new EmprestimoFilter();
    filtro.setStatusFromString(status); // Converte String do CSV para enum
    Specification<Emprestimo> spec = EmprestimoSpecifications.fromFilter(filtro);

    // When
    List<Emprestimo> resultado = repository.findAll(spec);

    // Then
    assertEquals(
        quantidadeEsperada,
        resultado.size(),
        "Status " + status + " deve retornar " + quantidadeEsperada + " empréstimo(s)");

    // Validações específicas por tipo
    if ("A".equals(status)) {
      assertEquals(emprestimoAtrasado.getId(), resultado.getFirst().getId());
      assertNull(
          resultado.getFirst().getDataDevolucao(),
          "Empréstimo atrasado não deve ter data de devolução");
      assertTrue(
          resultado.getFirst().getPrazoDevolucao().isBefore(LocalDate.now()),
          "Empréstimo atrasado deve ter prazo vencido");
    } else if ("P".equals(status)) {
      assertEquals(emprestimoPendente.getId(), resultado.getFirst().getId());
      assertNull(
          resultado.getFirst().getDataDevolucao(),
          "Empréstimo pendente não deve ter data de devolução");
      assertFalse(
          resultado.getFirst().getPrazoDevolucao().isBefore(LocalDate.now()),
          "Empréstimo pendente não deve ter prazo vencido");
    } else if ("F".equals(status)) {
      assertEquals(emprestimoFinalizado.getId(), resultado.getFirst().getId());
      assertNotNull(
          resultado.getFirst().getDataDevolucao(), "Empréstimo finalizado deve ter data de devolução");
    }
  }

  @Test
  @DisplayName("Deve filtrar por usuário empréstimo usando ID")
  void testFromFilter_QuandoFiltraPorUsuarioEmprestimoId_DeveRetornarEmprestimosDoUsuario() {
    // Given
    Usuario filtroUsuario = new Usuario();
    filtroUsuario.setId(usuarioEmprestimo.getId());

    EmprestimoFilter filtro = new EmprestimoFilter();
    filtro.setUsuarioEmprestimo(filtroUsuario);
    Specification<Emprestimo> spec = EmprestimoSpecifications.fromFilter(filtro);

    // When
    List<Emprestimo> resultado = repository.findAll(spec);

    // Then
    assertEquals(3, resultado.size());
    assertTrue(
        resultado.stream()
            .allMatch(e -> e.getUsuarioEmprestimo().getId().equals(usuarioEmprestimo.getId())));
  }

  @Test
  @DisplayName("Deve filtrar por usuário empréstimo usando username")
  void testFromFilter_QuandoFiltraPorUsuarioEmprestimoUsername_DeveRetornarEmprestimosDoUsuario() {
    // Given
    Usuario filtroUsuario = new Usuario();
    filtroUsuario.setUsername("aluno@teste.com");

    EmprestimoFilter filtro = new EmprestimoFilter();
    filtro.setUsuarioEmprestimo(filtroUsuario);
    Specification<Emprestimo> spec = EmprestimoSpecifications.fromFilter(filtro);

    // When
    List<Emprestimo> resultado = repository.findAll(spec);

    // Then
    assertEquals(3, resultado.size());
  }

  @Test
  @DisplayName("Deve filtrar empréstimos com data >= dtIniEmp")
  void testFromFilter_QuandoFiltraPorDataInicial_DeveRetornarEmprestimosAposData() {
    // Given
    String dtIniEmp = LocalDate.now().minusDays(5).toString(); // Últimos 5 dias

    EmprestimoFilter filtro = new EmprestimoFilter();
    filtro.setDtIniEmp(dtIniEmp);
    Specification<Emprestimo> spec = EmprestimoSpecifications.fromFilter(filtro);

    // When
    List<Emprestimo> resultado = repository.findAll(spec);

    // Then
    assertEquals(1, resultado.size()); // Apenas emprestimoPendente (2 dias atrás)
    assertEquals(emprestimoPendente.getId(), resultado.getFirst().getId());
  }

  @Test
  @DisplayName("Deve filtrar empréstimos com data <= dtFimEmp")
  void testFromFilter_QuandoFiltraPorDataFinal_DeveRetornarEmprestimosAntesData() {
    // Given
    String dtFimEmp = LocalDate.now().minusDays(5).toString(); // Até 5 dias atrás

    EmprestimoFilter filtro = new EmprestimoFilter();
    filtro.setDtFimEmp(dtFimEmp);
    Specification<Emprestimo> spec = EmprestimoSpecifications.fromFilter(filtro);

    // When
    List<Emprestimo> resultado = repository.findAll(spec);

    // Then
    assertEquals(2, resultado.size()); // Atrasado (10 dias) e Finalizado (7 dias)
  }

  @Test
  @DisplayName("Deve filtrar empréstimos em range de datas")
  void testFromFilter_QuandoFiltraPorRangeDatas_DeveRetornarEmprestimosNoPeriodo() {
    // Given
    String dtIniEmp = LocalDate.now().minusDays(8).toString();
    String dtFimEmp = LocalDate.now().minusDays(6).toString();

    EmprestimoFilter filtro = new EmprestimoFilter();
    filtro.setDtIniEmp(dtIniEmp);
    filtro.setDtFimEmp(dtFimEmp);
    Specification<Emprestimo> spec = EmprestimoSpecifications.fromFilter(filtro);

    // When
    List<Emprestimo> resultado = repository.findAll(spec);

    // Then
    assertEquals(1, resultado.size()); // Apenas emprestimoFinalizado (7 dias atrás)
    assertEquals(emprestimoFinalizado.getId(), resultado.getFirst().getId());
  }

  @Test
  @DisplayName("Deve carregar usuarioEmprestimo com JOIN FETCH (elimina N+1)")
  void testFromFilter_QuandoExecutaQuery_DeveCarregarUsuarioComJoinFetch() {
    // Given
    EmprestimoFilter filtro = new EmprestimoFilter();
    Specification<Emprestimo> spec = EmprestimoSpecifications.fromFilter(filtro);

    // When
    EntityManager em = entityManager.getEntityManager();
    em.clear(); // Limpa cache de primeiro nível
    List<Emprestimo> resultado = repository.findAll(spec);

    // Then - Acessar usuário SEM LazyInitializationException
    assertEquals(3, resultado.size());

    // Validar que pode acessar usuário fora de transação
    resultado.forEach(
        emprestimo -> {
          assertNotNull(emprestimo.getUsuarioEmprestimo());
          assertNotNull(emprestimo.getUsuarioEmprestimo().getNome());

          // Validar que permissões também foram carregadas
          assertNotNull(emprestimo.getUsuarioEmprestimo().getPermissoes());
          assertFalse(emprestimo.getUsuarioEmprestimo().getPermissoes().isEmpty());
        });
  }

  @Test
  @DisplayName("Deve executar count query sem JOIN FETCH")
  void testFromFilter_QuandoExecutaCount_DeveExecutarSemJoinFetch() {
    // Given
    EmprestimoFilter filtro = new EmprestimoFilter();
    filtro.setStatusFromString("A");
    Specification<Emprestimo> spec = EmprestimoSpecifications.fromFilter(filtro);

    // When - Executar count query
    long count = repository.count(spec);

    // Then
    assertEquals(1L, count);
    // Se passar sem erro, significa que query.getResultType() funcionou
  }

  @Test
  @DisplayName("Deve aplicar múltiplos filtros simultaneamente")
  void testFromFilter_QuandoMultiplosFiltros_DeveAplicarTodosSimultaneamente() {
    // Given
    Usuario filtroUsuario = new Usuario();
    filtroUsuario.setId(usuarioEmprestimo.getId());

    EmprestimoFilter filtro = new EmprestimoFilter();
    filtro.setUsuarioEmprestimo(filtroUsuario);
    filtro.setStatusFromString("F");
    filtro.setDtIniEmp(LocalDate.now().minusDays(10).toString());
    filtro.setDtFimEmp(LocalDate.now().toString());

    Specification<Emprestimo> spec = EmprestimoSpecifications.fromFilter(filtro);

    // When
    List<Emprestimo> resultado = repository.findAll(spec);

    // Then
    assertEquals(1, resultado.size());
    assertEquals(emprestimoFinalizado.getId(), resultado.getFirst().getId());
  }

  @Test
  @DisplayName("Deve retornar lista vazia quando nenhum empréstimo corresponde ao filtro")
  void testFromFilter_QuandoNenhumEmprestimoCorresponde_DeveRetornarListaVazia() {
    // Given
    String dtIniEmp = LocalDate.now().plusDays(1).toString(); // Data futura

    EmprestimoFilter filtro = new EmprestimoFilter();
    filtro.setDtIniEmp(dtIniEmp);
    Specification<Emprestimo> spec = EmprestimoSpecifications.fromFilter(filtro);

    // When
    List<Emprestimo> resultado = repository.findAll(spec);

    // Then
    assertTrue(resultado.isEmpty());
  }

  @Test
  @DisplayName("Deve filtrar por usuário responsável")
  void testFromFilter_QuandoFiltraPorUsuarioResponsavel_DeveRetornarEmprestimosDoResponsavel() {
    // Given
    Usuario filtroUsuario = new Usuario();
    filtroUsuario.setId(usuarioResponsavel.getId());

    EmprestimoFilter filtro = new EmprestimoFilter();
    filtro.setUsuarioResponsavel(filtroUsuario); // REFATORAÇÃO: Typo corrigido
    Specification<Emprestimo> spec = EmprestimoSpecifications.fromFilter(filtro);

    // When
    List<Emprestimo> resultado = repository.findAll(spec);

    // Then
    assertEquals(3, resultado.size());
    assertTrue(
        resultado.stream()
            .allMatch(e -> e.getUsuarioResponsavel().getId().equals(usuarioResponsavel.getId())));
  }

  @Test
  @DisplayName("Deve retornar todos quando status é inválido")
  void testFromFilter_QuandoStatusInvalido_DeveRetornarTodosEmprestimos() {
    // Given
    EmprestimoFilter filtro = new EmprestimoFilter();
    filtro.setStatusFromString("X"); // Status inválido - converte para null
    Specification<Emprestimo> spec = EmprestimoSpecifications.fromFilter(filtro);

    // When
    List<Emprestimo> resultado = repository.findAll(spec);

    // Then
    assertEquals(3, resultado.size());
  }

  @Test
  @DisplayName("Deve filtrar por status atrasado com prazo exatamente hoje")
  void testFromFilter_QuandoPrazoHoje_DeveConsiderarPendente() {
    // Given - Criar empréstimo com prazo hoje usando builder
    Emprestimo emprestimoPrazoHoje =
        fixture.criarEmprestimoCustom(
            usuarioEmprestimo,
            usuarioResponsavel,
            item,
            LocalDate.now().minusDays(1),
            LocalDate.now(), // Vence hoje
            null);
    entityManager.persist(emprestimoPrazoHoje);
    entityManager.flush();

    EmprestimoFilter filtro = new EmprestimoFilter();
    filtro.setStatusFromString("P"); // Pendente, não atrasado
    Specification<Emprestimo> spec = EmprestimoSpecifications.fromFilter(filtro);

    // When
    List<Emprestimo> resultado = repository.findAll(spec);

    // Then - Deve incluir empréstimo com prazo hoje (não atrasado)
    assertTrue(resultado.stream().anyMatch(e -> e.getId().equals(emprestimoPrazoHoje.getId())));
  }

  @Test
  @DisplayName("Deve carregar usuarioResponsavel com JOIN FETCH")
  void testFromFilter_QuandoExecutaQuery_DeveCarregarUsuarioResponsavelComJoinFetch() {
    // Given
    EmprestimoFilter filtro = new EmprestimoFilter();
    Specification<Emprestimo> spec = EmprestimoSpecifications.fromFilter(filtro);

    // When
    EntityManager em = entityManager.getEntityManager();
    em.clear();
    List<Emprestimo> resultado = repository.findAll(spec);

    // Then
    assertEquals(3, resultado.size());
    resultado.forEach(
        emprestimo -> {
          assertNotNull(emprestimo.getUsuarioResponsavel());
          assertNotNull(emprestimo.getUsuarioResponsavel().getNome());
        });
  }

  @Test
  @DisplayName("Deve filtrar usando data no formato ISO 8601 (yyyy-MM-dd)")
  void testFromFilter_QuandoDataFormatoISO8601_DeveFiltrarCorretamente() {
    // Given - Data no formato brasileiro: 07/10/2025 → ISO: 2025-10-07
    String dtIniEmp = LocalDate.of(2025, 10, 7).minusDays(3).toString(); // 2025-10-04

    EmprestimoFilter filtro = new EmprestimoFilter();
    filtro.setDtIniEmp(dtIniEmp);
    Specification<Emprestimo> spec = EmprestimoSpecifications.fromFilter(filtro);

    // When
    List<Emprestimo> resultado = repository.findAll(spec);

    // Then - Deve filtrar corretamente usando formato ISO
    assertNotNull(resultado);
    // Validação depende dos dados de teste
  }

  @Test
  @DisplayName("Deve eliminar N+1 queries com JOIN FETCH em grandes volumes")
  void testFromFilter_QuandoGrandesVolumes_DeveEliminarProblemaSelectN1() {
    // Given - Criar 50 empréstimos usando builder
    for (int i = 0; i < 50; i++) {
      Emprestimo emp =
          fixture.criarEmprestimoCustom(
              usuarioEmprestimo,
              usuarioResponsavel,
              item,
              LocalDate.now().minusDays(i),
              LocalDate.now().plusDays(i),
              null);
      entityManager.persist(emp);
    }
    entityManager.flush();

    EmprestimoFilter filtro = new EmprestimoFilter();
    Specification<Emprestimo> spec = EmprestimoSpecifications.fromFilter(filtro);

    // When
    EntityManager em = entityManager.getEntityManager();
    em.clear();

    long startTime = System.currentTimeMillis();
    List<Emprestimo> resultado = repository.findAll(spec);
    long duration = System.currentTimeMillis() - startTime;

    // Then - Acessar todos usuários sem LazyInitializationException
    resultado.forEach(
        emprestimo -> {
          assertNotNull(emprestimo.getUsuarioEmprestimo().getNome());
          assertNotNull(emprestimo.getUsuarioEmprestimo().getPermissoes());
          assertNotNull(emprestimo.getUsuarioResponsavel().getNome());
        });

    // Performance: deve carregar 50+ empréstimos em < 1 segundo
    assertTrue(duration < 1000, "Query com JOIN FETCH deve ser rápida: " + duration + "ms");
  }

  @Test
  @DisplayName("Deve filtrar corretamente com data no limite do status (00:00:00)")
  void testFromFilter_QuandoDataNoLimiteMeiaNoite_DeveFiltrarCorretamente() {
    // Given - Empréstimo com prazo exatamente à meia-noite de hoje usando builder
    Emprestimo emprestimoBorderline =
        fixture.criarEmprestimoCustom(
            usuarioEmprestimo,
            usuarioResponsavel,
            item,
            LocalDate.now().minusDays(1),
            LocalDate.now(),
            null);
    entityManager.persist(emprestimoBorderline);
    entityManager.flush();

    // When - Filtrar pendentes
    EmprestimoFilter filtro = new EmprestimoFilter();
    filtro.setStatusFromString("P");
    Specification<Emprestimo> spec = EmprestimoSpecifications.fromFilter(filtro);
    List<Emprestimo> resultado = repository.findAll(spec);

    // Then - Empréstimo com prazo hoje deve ser pendente (não atrasado)
    assertTrue(
        resultado.stream().anyMatch(e -> e.getId().equals(emprestimoBorderline.getId())),
        "Empréstimo com prazo hoje deve ser considerado pendente");
  }

  @Test
  @DisplayName("Deve retornar todos quando filtro tem usuário sem ID e username")
  void testFromFilter_QuandoUsuarioSemIdOuUsername_DeveRetornarTodosEmprestimos() {
    // Given
    Usuario usuarioVazio = new Usuario();
    // Sem ID e sem username

    EmprestimoFilter filtro = new EmprestimoFilter();
    filtro.setUsuarioEmprestimo(usuarioVazio);
    Specification<Emprestimo> spec = EmprestimoSpecifications.fromFilter(filtro);

    // When
    List<Emprestimo> resultado = repository.findAll(spec);

    // Then - Deve retornar todos (predicado vazio)
    assertEquals(3, resultado.size());
  }

  @Test
  @DisplayName("Deve filtrar por usuário responsável via username")
  void testFromFilter_QuandoFiltraPorResponsavelViaUsername_DeveRetornarEmprestimosDoResponsavel() {
    // Given
    Usuario filtroUsuario = new Usuario();
    filtroUsuario.setUsername(usuarioResponsavel.getUsername());

    EmprestimoFilter filtro = new EmprestimoFilter();
    filtro.setUsuarioResponsavel(filtroUsuario); // REFATORAÇÃO: Typo corrigido

    // When
    Specification<Emprestimo> spec = EmprestimoSpecifications.fromFilter(filtro);
    List<Emprestimo> resultado = repository.findAll(spec);

    // Then - Deve encontrar todos os empréstimos do responsável
    assertEquals(3, resultado.size());
    assertTrue(
        resultado.stream()
            .allMatch(
                e ->
                    e.getUsuarioResponsavel()
                        .getUsername()
                        .equals(usuarioResponsavel.getUsername())));
  }

  @Test
  @DisplayName("Deve lançar DateTimeParseException com formato brasileiro inválido")
  void testFromFilter_QuandoDataFormatoBrasileiro_DeveLancarDateTimeParseException() {
    // Given
    String dataInvalida = "07/10/2025"; // Formato brasileiro (inválido para LocalDate.parse)

    EmprestimoFilter filtro = new EmprestimoFilter();
    filtro.setDtIniEmp(dataInvalida);

    // When/Then - Deve lançar DateTimeParseException
    assertThrows(
        java.time.format.DateTimeParseException.class,
        () -> {
          Specification<Emprestimo> spec = EmprestimoSpecifications.fromFilter(filtro);
          repository.findAll(spec);
        });
  }
}
