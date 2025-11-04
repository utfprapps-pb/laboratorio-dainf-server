package br.com.utfpr.gerenciamento.server.repository;

import static org.junit.jupiter.api.Assertions.*;

import br.com.utfpr.gerenciamento.server.enumeration.UserRole;
import br.com.utfpr.gerenciamento.server.model.Permissao;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.repository.specification.UsuarioSpecifications;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UsuarioRepositoryTest {

  @Autowired private UsuarioRepository usuarioRepository;

  @Autowired private TestEntityManager entityManager;

  private Permissao permissaoAluno;

  @BeforeEach
  void setUp() {
    // Criar permissões
    permissaoAluno = new Permissao();
    permissaoAluno.setNome("ROLE_ALUNO");
    permissaoAluno = entityManager.persist(permissaoAluno);

    Permissao permissaoProfessor = new Permissao();
    permissaoProfessor.setNome("ROLE_PROFESSOR");
    permissaoProfessor = entityManager.persist(permissaoProfessor);

    // Criar usuários de teste
    Usuario joao = new Usuario();
    joao.setNome("João Silva");
    joao.setUsername("joao");
    joao.setEmail("joao@test.com");
    joao.setPassword("senha123");
    joao.setTelefone("41999999001");
    Set<Permissao> permissoesJoao = new HashSet<>();
    permissoesJoao.add(permissaoAluno);
    joao.setPermissoes(permissoesJoao);
    entityManager.persist(joao);

    Usuario maria = new Usuario();
    maria.setNome("Maria Santos");
    maria.setUsername("maria");
    maria.setEmail("maria@test.com");
    maria.setPassword("senha123");
    maria.setTelefone("41999999002");
    Set<Permissao> permissoesMaria = new HashSet<>();
    permissoesMaria.add(permissaoProfessor);
    maria.setPermissoes(permissoesMaria);
    entityManager.persist(maria);

    Usuario pedro = new Usuario();
    pedro.setNome("Pedro Oliveira");
    pedro.setUsername("pedro");
    pedro.setEmail("pedro@test.com");
    pedro.setPassword("senha123");
    pedro.setTelefone("41999999003");
    Set<Permissao> permissoesPedro = new HashSet<>();
    permissoesPedro.add(permissaoAluno);
    pedro.setPermissoes(permissoesPedro);
    entityManager.persist(pedro);

    entityManager.flush();
    entityManager.clear();
  }

  @Test
  void findByNomeLikeIgnoreCase_DeveRetornarPaginaComUsuarios() {
    // Given
    Specification<Usuario> spec =
        UsuarioSpecifications.distinctResults().and(UsuarioSpecifications.searchByText("João"));
    Pageable pageable = PageRequest.of(0, 10);

    // When
    Page<Usuario> resultado = usuarioRepository.findAll(spec, pageable);

    // Then
    assertNotNull(resultado);
    assertEquals(1, resultado.getTotalElements());
    assertEquals(1, resultado.getContent().size());
    assertEquals("João Silva", resultado.getContent().getFirst().getNome());
  }

  @Test
  void findByNomeLikeIgnoreCase_DeveFuncionarComCaseInsensitive() {
    // Given
    Specification<Usuario> spec =
        UsuarioSpecifications.distinctResults().and(UsuarioSpecifications.searchByText("joão"));
    Pageable pageable = PageRequest.of(0, 10);

    // When
    Page<Usuario> resultado = usuarioRepository.findAll(spec, pageable);

    // Then
    assertNotNull(resultado);
    assertEquals(1, resultado.getTotalElements());
    assertEquals("João Silva", resultado.getContent().getFirst().getNome());
  }

  @Test
  void findByNomeLikeIgnoreCase_DeveRetornarPaginaVaziaQuandoNaoEncontrar() {
    // Given
    Specification<Usuario> spec =
        UsuarioSpecifications.distinctResults()
            .and(UsuarioSpecifications.searchByText("NaoExiste"));
    Pageable pageable = PageRequest.of(0, 10);

    // When
    Page<Usuario> resultado = usuarioRepository.findAll(spec, pageable);

    // Then
    assertNotNull(resultado);
    assertEquals(0, resultado.getTotalElements());
    assertTrue(resultado.getContent().isEmpty());
  }

  @Test
  void findByNomeLikeIgnoreCase_DeveRespeitarTamanhoDaPagina() {
    // Given
    Specification<Usuario> spec =
        UsuarioSpecifications.distinctResults().and(UsuarioSpecifications.searchByText("a"));
    Pageable pageable = PageRequest.of(0, 2); // Página de tamanho 2

    // When
    Page<Usuario> resultado = usuarioRepository.findAll(spec, pageable);

    // Then
    assertNotNull(resultado);
    assertEquals(3, resultado.getTotalElements()); // Total de 3
    assertEquals(2, resultado.getContent().size()); // Mas retorna apenas 2
    assertEquals(2, resultado.getTotalPages()); // 2 páginas no total
  }

  @Test
  void findByNomeLikeIgnoreCase_DevePermitirNavegacaoEntrePaginas() {
    // Given
    Specification<Usuario> spec =
        UsuarioSpecifications.distinctResults().and(UsuarioSpecifications.searchByText("a"));
    Pageable primeiraPage = PageRequest.of(0, 2);
    Pageable segundaPage = PageRequest.of(1, 2);

    // When
    Page<Usuario> resultadoPrimeira = usuarioRepository.findAll(spec, primeiraPage);
    Page<Usuario> resultadoSegunda = usuarioRepository.findAll(spec, segundaPage);

    // Then
    assertEquals(2, resultadoPrimeira.getContent().size());
    assertEquals(1, resultadoSegunda.getContent().size());
    assertEquals(3, resultadoPrimeira.getTotalElements());
    assertEquals(3, resultadoSegunda.getTotalElements());
  }

  @Test
  void findAll_ComPageable_DeveRetornarTodosUsuariosPaginados() {
    // Given
    Pageable pageable = PageRequest.of(0, 10);

    // When
    Page<Usuario> resultado = usuarioRepository.findAll(pageable);

    // Then
    assertNotNull(resultado);
    assertEquals(3, resultado.getTotalElements());
    assertEquals(3, resultado.getContent().size());
  }

  @Test
  void findAll_ComPageable_DeveRetornarPaginaVazia() {
    // Given
    Pageable pageable = PageRequest.of(10, 10); // Página muito além dos dados

    // When
    Page<Usuario> resultado = usuarioRepository.findAll(pageable);

    // Then
    assertNotNull(resultado);
    assertEquals(3, resultado.getTotalElements()); // Total ainda é 3
    assertEquals(0, resultado.getContent().size()); // Mas conteúdo vazio
  }

  @Test
  void findByNomeLikeIgnoreCase_DeveRetornarInformacoesCorretas() {
    // Given
    Specification<Usuario> spec =
        UsuarioSpecifications.distinctResults().and(UsuarioSpecifications.searchByText("Silva"));
    Pageable pageable = PageRequest.of(0, 10);

    // When
    Page<Usuario> resultado = usuarioRepository.findAll(spec, pageable);

    // Then
    assertNotNull(resultado);
    assertTrue(resultado.hasContent());
    assertFalse(resultado.isEmpty());
    assertEquals(0, resultado.getNumber()); // Número da página
    assertEquals(10, resultado.getSize()); // Tamanho da página
    assertEquals(1, resultado.getTotalPages()); // Total de páginas
    assertTrue(resultado.isFirst()); // É a primeira página
    assertTrue(resultado.isLast()); // É a última página
  }

  @Test
  void findByUsername_DeveRetornarUsuarioCorreto() {
    // Given
    String username = "joao";

    // When
    Usuario resultado = usuarioRepository.findByUsername(username);

    // Then
    assertNotNull(resultado);
    assertEquals("João Silva", resultado.getNome());
    assertEquals("joao@test.com", resultado.getEmail());
  }

  @Test
  void findByEmail_DeveRetornarUsuarioCorreto() {
    // Given
    String email = "maria@test.com";

    // When
    Usuario resultado = usuarioRepository.findByEmail(email);

    // Then
    assertNotNull(resultado);
    assertEquals("Maria Santos", resultado.getNome());
    assertEquals("maria", resultado.getUsername());
  }

  @Test
  void findByUsernameOrEmail_DeveRetornarUsuarioPorUsername() {
    // Given
    String username = "pedro";
    String email = "";

    // When
    Usuario resultado = usuarioRepository.findByUsernameOrEmail(username, email);

    // Then
    assertNotNull(resultado);
    assertEquals("Pedro Oliveira", resultado.getNome());
  }

  @Test
  void findByUsernameOrEmail_DeveRetornarUsuarioPorEmail() {
    // Given
    String username = "";
    String email = "joao@test.com";

    // When
    Usuario resultado = usuarioRepository.findByUsernameOrEmail(username, email);

    // Then
    assertNotNull(resultado);
    assertEquals("João Silva", resultado.getNome());
  }

  @Test
  void persistirUsuario_DeveSalvarCamposNovos() {
    Permissao permissaoAlunoManaged = entityManager.find(Permissao.class, permissaoAluno.getId());
    Usuario usuario =
        Usuario.builder()
            .nome("Teste Novo")
            .username("novouser")
            .email("novo@test.com")
            .password("senha123")
            .telefone("41999999004")
            .documento("12345678900")
            .fotoUrl("http://foto.com/novo.jpg")
            .permissoes(Set.of(permissaoAlunoManaged))
            .build();
    Usuario salvo = usuarioRepository.save(usuario);
    assertNotNull(salvo.getId());
    assertEquals("12345678900", salvo.getDocumento());
    assertEquals("http://foto.com/novo.jpg", salvo.getFotoUrl());
  }

  @Test
  void findByCodigoVerificacao_DeveRetornarUsuarioCorreto() {
    Permissao permissaoAlunoManaged = entityManager.find(Permissao.class, permissaoAluno.getId());
    Usuario usuario =
        Usuario.builder()
            .nome("Verifica Teste")
            .username("verificauser")
            .email("verifica@test.com")
            .password("senha123")
            .telefone("41999999005")
            .documento("98765432100")
            .permissoes(new HashSet<>(Set.of(permissaoAlunoManaged)))
            .build();
    usuario = usuarioRepository.save(usuario);
    usuario.setCodigoVerificacao("COD123");
    usuarioRepository.save(usuario);
    entityManager.flush();
    Usuario resultado = usuarioRepository.findByCodigoVerificacao("COD123");
    assertNotNull(resultado);
    assertEquals("verificauser", resultado.getUsername());
  }

  @Test
  void findWithPermissoesByUsername_DeveCarregarPermissoes() {
    Usuario resultado = usuarioRepository.findWithPermissoesByUsername("joao");
    assertNotNull(resultado);
    assertNotNull(resultado.getPermissoes());
    assertFalse(resultado.getPermissoes().isEmpty());
    assertTrue(resultado.getPermissoes().stream().anyMatch(p -> p.getNome().equals("ROLE_ALUNO")));
  }

  @Test
  void findWithPermissoesByUsernameOrEmail_DeveCarregarPermissoesPorEmail() {
    Usuario resultado = usuarioRepository.findWithPermissoesByUsernameOrEmail("", "maria@test.com");
    assertNotNull(resultado);
    assertNotNull(resultado.getPermissoes());
    assertTrue(
        resultado.getPermissoes().stream().anyMatch(p -> p.getNome().equals("ROLE_PROFESSOR")));
  }

  @Test
  void usuario_DeveImplementarUserDetailsECamposObrigatorios() {
    Usuario usuario = usuarioRepository.findByUsername("joao");
    assertNotNull(usuario);
    assertInstanceOf(UserDetails.class, usuario);
    assertEquals("joao", usuario.getUsername());
    assertNotNull(usuario.getPassword());
    assertNotNull(usuario.getEmail());
    assertNotNull(usuario.getTelefone());
  }

  @Test
  void permissao_DeveImplementarGrantedAuthority() {
    Permissao permissao = permissaoAluno;
    assertNotNull(permissao);
    assertEquals("ROLE_ALUNO", permissao.getAuthority());
  }

  // ========== TESTES DE SPECIFICATIONS (REFATORAÇÃO DE IDS HARDCODED) ==========

  @ParameterizedTest
  @EnumSource(UserRole.class)
  @DisplayName("Deve encontrar usuários por role específica usando Specifications")
  void deveEncontrarUsuariosPorRoleComSpecifications(UserRole role) {
    // Given: Specification para filtrar por role
    Specification<Usuario> spec =
        UsuarioSpecifications.hasAnyRole(role).and(UsuarioSpecifications.distinctResults());

    // When: Busca usuários com a role
    Page<Usuario> resultado = usuarioRepository.findAll(spec, PageRequest.of(0, 20));

    // Then: Todos usuários retornados devem ter a role especificada
    if (!resultado.isEmpty()) {
      resultado.forEach(
          usuario -> {
            Set<String> roleNames =
                usuario.getPermissoes().stream()
                    .map(Permissao::getNome)
                    .collect(Collectors.toSet());

            assertTrue(
                roleNames.contains(role.getAuthority()),
                "Usuario ID "
                    + usuario.getId()
                    + " deveria ter role "
                    + role.getAuthority()
                    + " mas tem: "
                    + roleNames);
          });
    }
  }

  @Test
  @DisplayName("Deve encontrar apenas PROFESSOR e ALUNO (usuários acadêmicos)")
  void deveEncontrarApenasUsuariosAcademicos() {
    // Given: Specification para PROFESSOR + ALUNO
    Specification<Usuario> spec =
        UsuarioSpecifications.distinctResults()
            .and(UsuarioSpecifications.hasAnyRole(UserRole.PROFESSOR, UserRole.ALUNO));

    // When: Busca usuários acadêmicos
    Page<Usuario> resultado = usuarioRepository.findAll(spec, PageRequest.of(0, 20));

    // Then: Apenas usuários com role PROFESSOR ou ALUNO devem ser retornados
    resultado.forEach(
        usuario -> {
          Set<String> roleNames =
              usuario.getPermissoes().stream().map(Permissao::getNome).collect(Collectors.toSet());

          boolean isProfessorOuAluno =
              roleNames.contains(UserRole.PROFESSOR.getAuthority())
                  || roleNames.contains(UserRole.ALUNO.getAuthority());

          assertTrue(
              isProfessorOuAluno,
              "Usuario ID "
                  + usuario.getId()
                  + " não deveria estar em usuários acadêmicos. Roles: "
                  + roleNames);
        });
  }

  @Test
  @DisplayName("Deve encontrar apenas ADMINISTRADOR e LABORATORISTA (usuários de laboratório)")
  void deveEncontrarApenasUsuariosDeLaboratorio() {
    // Given: Specification para ADMINISTRADOR + LABORATORISTA
    Specification<Usuario> spec =
        UsuarioSpecifications.distinctResults()
            .and(UsuarioSpecifications.hasAnyRole(UserRole.ADMINISTRADOR, UserRole.LABORATORISTA));

    // When: Busca usuários de laboratório
    Page<Usuario> resultado = usuarioRepository.findAll(spec, PageRequest.of(0, 20));

    // Then: Apenas usuários com role ADMINISTRADOR ou LABORATORISTA devem ser retornados
    resultado.forEach(
        usuario -> {
          Set<String> roleNames =
              usuario.getPermissoes().stream().map(Permissao::getNome).collect(Collectors.toSet());

          boolean isAdminOuLab =
              roleNames.contains(UserRole.ADMINISTRADOR.getAuthority())
                  || roleNames.contains(UserRole.LABORATORISTA.getAuthority());

          assertTrue(
              isAdminOuLab,
              "Usuario ID "
                  + usuario.getId()
                  + " não deveria estar em usuários de laboratório. Roles: "
                  + roleNames);
        });
  }

  @Test
  @DisplayName("Deve aplicar DISTINCT para evitar duplicatas em JOIN ManyToMany")
  void deveAplicarDistinctEmJoinManyToMany() {
    // Given: Specification com DISTINCT
    Specification<Usuario> spec =
        UsuarioSpecifications.distinctResults()
            .and(UsuarioSpecifications.hasAnyRole(UserRole.ALUNO, UserRole.PROFESSOR));

    // When: Busca com DISTINCT
    Page<Usuario> resultado = usuarioRepository.findAll(spec, PageRequest.of(0, 20));

    // Then: Não deve haver IDs duplicados na lista
    long totalUsuarios = resultado.getTotalElements();
    long idsUnicos = resultado.stream().map(Usuario::getId).distinct().count();

    assertEquals(
        totalUsuarios, idsUnicos, "DISTINCT não está funcionando - há IDs duplicados no resultado");
  }

  @Test
  @DisplayName("Deve carregar permissões via @EntityGraph sem N+1 queries com Specifications")
  void deveCarregarPermissoesViaEntityGraphComSpecifications() {
    // Given: Specification simples
    Specification<Usuario> spec =
        UsuarioSpecifications.hasAnyRole(UserRole.ALUNO, UserRole.PROFESSOR)
            .and(UsuarioSpecifications.distinctResults());

    // When: Busca usuários
    Page<Usuario> resultado = usuarioRepository.findAll(spec, PageRequest.of(0, 5));

    // Then: Permissões devem estar carregadas (não LAZY)
    resultado.forEach(
        usuario -> {
          assertNotNull(usuario.getPermissoes(), "Permissões não deveriam ser null");
          assertFalse(
              usuario.getPermissoes().isEmpty(),
              "Permissões deveriam estar carregadas para Usuario ID " + usuario.getId());
        });
  }

  @ParameterizedTest
  @CsvSource({
    "joão, ALUNO", // Usuário criado no setUp com role ALUNO
    "maria, PROFESSOR" // Usuário criado no setUp com role PROFESSOR
  })
  @DisplayName("Deve filtrar por texto E role específica usando searchByTextAndRoles")
  void deveFiltrarPorTextoERoleComSpecifications(String searchText, UserRole role) {
    // Given: Specification combinando filtro textual e role
    Specification<Usuario> spec =
        UsuarioSpecifications.searchByTextAndRoles(searchText, role)
            .and(UsuarioSpecifications.distinctResults());

    // When: Busca com filtro combinado
    Page<Usuario> resultado = usuarioRepository.findAll(spec, PageRequest.of(0, 20));

    // Then: Deve retornar resultados
    assertFalse(
        resultado.isEmpty(),
        "Deveria retornar resultados para texto='"
            + searchText
            + "' e role="
            + role.getAuthority());

    // Validar que todos resultados contêm o texto buscado
    resultado.forEach(
        usuario -> {
          String upperText = searchText.toUpperCase();
          boolean contemTexto =
              usuario.getNome().toUpperCase().contains(upperText)
                  || usuario.getUsername().toUpperCase().contains(upperText)
                  || (usuario.getDocumento() != null
                      && usuario.getDocumento().toUpperCase().contains(upperText));

          assertTrue(
              contemTexto,
              "Usuario ID "
                  + usuario.getId()
                  + " não contém '"
                  + searchText
                  + "' em campos textuais");

          // Validar que usuário tem a role esperada
          Set<String> roleNames =
              usuario.getPermissoes().stream().map(Permissao::getNome).collect(Collectors.toSet());

          assertTrue(
              roleNames.contains(role.getAuthority()),
              "Usuario ID "
                  + usuario.getId()
                  + " deveria ter role "
                  + role.getAuthority()
                  + " mas tem: "
                  + roleNames);
        });
  }

  // ========== TESTES DE VALIDAÇÃO PARAMETRIZADOS ==========

  private static Stream<Arguments> validacaoArgumentosInvalidos() {
    return Stream.of(
        // hasAnyRole com roles nulas
        Arguments.of(
            "hasAnyRole com roles nulas",
            (Runnable) () -> UsuarioSpecifications.hasAnyRole((UserRole[]) null),
            "Roles não podem ser nulas ou vazias"),
        // hasAnyRole com roles vazias
        Arguments.of(
            "hasAnyRole com roles vazias",
            (Runnable) UsuarioSpecifications::hasAnyRole,
            "Roles não podem ser nulas ou vazias"),
        // searchByTextAndRoles com texto nulo
        Arguments.of(
            "searchByTextAndRoles com texto nulo",
            (Runnable) () -> UsuarioSpecifications.searchByTextAndRoles(null, UserRole.PROFESSOR),
            "Texto de busca não pode ser nulo ou vazio"),
        // searchByTextAndRoles com texto vazio
        Arguments.of(
            "searchByTextAndRoles com texto vazio",
            (Runnable) () -> UsuarioSpecifications.searchByTextAndRoles("", UserRole.PROFESSOR),
            "Texto de busca não pode ser nulo ou vazio"),
        // searchByTextAndRoles com texto blank
        Arguments.of(
            "searchByTextAndRoles com texto blank",
            (Runnable) () -> UsuarioSpecifications.searchByTextAndRoles("   ", UserRole.PROFESSOR),
            "Texto de busca não pode ser nulo ou vazio"),
        // searchByTextAndRoles com roles nulas
        Arguments.of(
            "searchByTextAndRoles com roles nulas",
            (Runnable) () -> UsuarioSpecifications.searchByTextAndRoles("teste", (UserRole[]) null),
            "Roles não podem ser nulas ou vazias"),
        // searchByTextAndRoles com roles vazias
        Arguments.of(
            "searchByTextAndRoles com roles vazias",
            (Runnable) () -> UsuarioSpecifications.searchByTextAndRoles("teste"),
            "Roles não podem ser nulas ou vazias"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("validacaoArgumentosInvalidos")
  @DisplayName("Deve lançar IllegalArgumentException para argumentos inválidos")
  void deveLancarExcecaoParaArgumentosInvalidos(
      String descricao, Runnable operacao, String mensagemEsperada) {
    // When/Then: Tentar executar operação com argumentos inválidos
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, operacao::run);

    assertEquals(mensagemEsperada, exception.getMessage());
  }

  @Test
  @DisplayName("findAll com Specification deve usar @EntityGraph para evitar N+1 queries")
  void findAllWithSpecification_DeveCarregarPermissoesEagerly() {
    // Given: Specification básica
    Specification<Usuario> spec = UsuarioSpecifications.distinctResults();
    Pageable pageable = PageRequest.of(0, 10);

    // When: Buscar usuários com Specification
    Page<Usuario> resultado = usuarioRepository.findAll(spec, pageable);

    // Then: Deve retornar usuários
    assertNotNull(resultado);
    assertFalse(resultado.isEmpty());

    // And: Permissoes devem estar carregadas (sem LazyInitializationException)
    // Acessar permissoes fora da transação (após entityManager.clear) valida eager loading
    entityManager.clear();

    Usuario primeiroUsuario = resultado.getContent().stream().findFirst().orElseThrow();
    assertDoesNotThrow(
        () -> {
          Set<Permissao> permissoes = primeiroUsuario.getPermissoes();
          assertNotNull(permissoes);
          assertFalse(permissoes.isEmpty());
        },
        "Permissoes devem estar carregadas eagerly via @EntityGraph, sem LazyInitializationException");
  }
}
