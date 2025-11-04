package br.com.utfpr.gerenciamento.server.repository.specification;

import static org.junit.jupiter.api.Assertions.*;

import br.com.utfpr.gerenciamento.server.enumeration.UserRole;
import br.com.utfpr.gerenciamento.server.model.Permissao;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.repository.UsuarioRepository;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

/** Testes de integração para UsuarioSpecifications usando banco H2 em memória. */
@DataJpaTest
@ActiveProfiles("test")
class UsuarioSpecificationsTest {

  @Autowired private TestEntityManager entityManager;

  @Autowired private UsuarioRepository usuarioRepository;

  @BeforeEach
  void setUp() {
    // Criar permissões
    Permissao permissaoProfessor = criarPermissao(UserRole.PROFESSOR);
    Permissao permissaoAluno = criarPermissao(UserRole.ALUNO);
    Permissao permissaoAdmin = criarPermissao(UserRole.ADMINISTRADOR);

    // Criar usuários de teste (dados inseridos no DB, sem necessidade de armazenar referências)
    criarUsuario("João Silva", "joao@utfpr.edu.br", "12345678901", permissaoProfessor);
    criarUsuario("Maria Santos", "maria@aluno.com", "98765432109", permissaoAluno);
    criarUsuario("Carlos Admin", "carlos@utfpr.edu.br", "11111111111", permissaoAdmin);

    entityManager.flush();
    entityManager.clear();
  }

  @Test
  void hasAnyRole_DeveRetornarUsuariosComRoleEspecifica() {
    // Given
    Specification<Usuario> spec = UsuarioSpecifications.hasAnyRole(UserRole.PROFESSOR);

    // When
    List<Usuario> resultados = usuarioRepository.findAll(spec);

    // Then
    assertEquals(1, resultados.size());
    assertTrue(resultados.stream().anyMatch(u -> u.getNome().equals("João Silva")));
  }

  @Test
  void hasAnyRole_DeveRetornarUsuariosComMultiplasRoles() {
    // Given
    Specification<Usuario> spec =
        UsuarioSpecifications.hasAnyRole(UserRole.PROFESSOR, UserRole.ALUNO);

    // When
    List<Usuario> resultados = usuarioRepository.findAll(spec);

    // Then
    assertEquals(2, resultados.size());
    assertTrue(resultados.stream().anyMatch(u -> u.getNome().equals("João Silva")));
    assertTrue(resultados.stream().anyMatch(u -> u.getNome().equals("Maria Santos")));
  }

  @ParameterizedTest
  @MethodSource("rolesInvalidasProvider")
  void hasAnyRole_DeveLancarExcecaoComParametrosInvalidos(UserRole[] roles) {
    // When/Then
    assertThrows(IllegalArgumentException.class, () -> UsuarioSpecifications.hasAnyRole(roles));
  }

  private static Stream<Arguments> rolesInvalidasProvider() {
    return Stream.of(Arguments.of((Object) null), Arguments.of((Object) new UserRole[0]));
  }

  @ParameterizedTest
  @MethodSource("buscasPorCampoProvider")
  void searchByText_DeveBuscarPorDiferentesCampos(String textoConsulta, String nomeEsperado) {
    // Given
    Specification<Usuario> spec = UsuarioSpecifications.searchByText(textoConsulta);

    // When
    List<Usuario> resultados = usuarioRepository.findAll(spec);

    // Then
    assertEquals(1, resultados.size());
    assertEquals(nomeEsperado, resultados.getFirst().getNome());
  }

  private static Stream<Arguments> buscasPorCampoProvider() {
    return Stream.of(
        Arguments.of("João", "João Silva"), // Busca por nome
        Arguments.of("maria@aluno.com", "Maria Santos"), // Busca por username
        Arguments.of("12345678901", "João Silva"), // Busca por documento
        Arguments.of("jOãO", "João Silva")); // Case-insensitive
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "\t", "\n"})
  void searchByText_DeveLancarExcecaoComTextoInvalido(String texto) {
    // When/Then
    assertThrows(IllegalArgumentException.class, () -> UsuarioSpecifications.searchByText(texto));
  }

  @Test
  void searchByTextWithRoles_DeveBuscarComTextoERoles() {
    // Given
    Specification<Usuario> spec =
        UsuarioSpecifications.searchByTextWithRoles("João", UserRole.PROFESSOR);

    // When
    List<Usuario> resultados = usuarioRepository.findAll(spec);

    // Then
    assertEquals(1, resultados.size());
    assertEquals("João Silva", resultados.getFirst().getNome());
  }

  @Test
  void searchByTextWithRoles_DeveRetornarTodosUsuariosComRolesQuandoTextoNulo() {
    // Given
    Specification<Usuario> spec =
        UsuarioSpecifications.searchByTextWithRoles(null, UserRole.PROFESSOR, UserRole.ALUNO);

    // When
    List<Usuario> resultados = usuarioRepository.findAll(spec);

    // Then
    assertEquals(2, resultados.size());
  }

  @Test
  void searchByTextWithRoles_DeveRetornarTodosUsuariosComRolesQuandoTextoVazio() {
    // Given
    Specification<Usuario> spec =
        UsuarioSpecifications.searchByTextWithRoles("", UserRole.PROFESSOR);

    // When
    List<Usuario> resultados = usuarioRepository.findAll(spec);

    // Then
    assertEquals(1, resultados.size());
    assertEquals("João Silva", resultados.getFirst().getNome());
  }

  @Test
  void searchByTextWithRoles_DeveRetornarVazioQuandoNenhumUsuarioCorresponde() {
    // Given
    Specification<Usuario> spec =
        UsuarioSpecifications.searchByTextWithRoles("Inexistente", UserRole.PROFESSOR);

    // When
    List<Usuario> resultados = usuarioRepository.findAll(spec);

    // Then
    assertTrue(resultados.isEmpty());
  }

  @ParameterizedTest
  @MethodSource("rolesInvalidasProvider")
  void searchByTextWithRoles_DeveLancarExcecaoComRolesInvalidas(UserRole[] roles) {
    // When/Then
    assertThrows(
        IllegalArgumentException.class,
        () -> UsuarioSpecifications.searchByTextWithRoles("João", roles));
  }

  @Test
  void searchByTextAndRoles_DeveCombinarTextoERoles() {
    // Given
    Specification<Usuario> spec =
        UsuarioSpecifications.searchByTextAndRoles("Silva", UserRole.PROFESSOR);

    // When
    List<Usuario> resultados = usuarioRepository.findAll(spec);

    // Then
    assertEquals(1, resultados.size());
    assertEquals("João Silva", resultados.getFirst().getNome());
  }

  @ParameterizedTest
  @MethodSource("parametrosInvalidosSearchByTextAndRolesProvider")
  void searchByTextAndRoles_DeveLancarExcecaoComParametrosInvalidos(
      String texto, UserRole[] roles) {
    // When/Then
    assertThrows(
        IllegalArgumentException.class,
        () -> UsuarioSpecifications.searchByTextAndRoles(texto, roles));
  }

  private static Stream<Arguments> parametrosInvalidosSearchByTextAndRolesProvider() {
    return Stream.of(
        Arguments.of(null, new UserRole[] {UserRole.PROFESSOR}),
        Arguments.of("", new UserRole[] {UserRole.PROFESSOR}),
        Arguments.of("   ", new UserRole[] {UserRole.PROFESSOR}),
        Arguments.of("João", null),
        Arguments.of("João", new UserRole[0]));
  }

  @Test
  void distinctResults_DeveRetornarResultadosDistintos() {
    // Given: Criar usuário com múltiplas permissões para testar DISTINCT
    Permissao professor = buscarPermissaoPorRole(UserRole.PROFESSOR);
    Permissao aluno = buscarPermissaoPorRole(UserRole.ALUNO);

    Usuario multiRole = new Usuario();
    multiRole.setNome("Multi Role User");
    multiRole.setEmail("multi@test.com");
    multiRole.setUsername("multi@test.com");
    multiRole.setDocumento("99999999999");
    multiRole.setTelefone("(00) 00000-0000");
    multiRole.setPassword("$2a$10$encodedPassword");
    multiRole.setEmailVerificado(true);
    multiRole.setAtivo(true);
    multiRole.setPermissoes(new HashSet<>());
    multiRole.getPermissoes().add(professor);
    multiRole.getPermissoes().add(aluno);
    entityManager.persist(multiRole);
    entityManager.flush();
    entityManager.clear();

    Specification<Usuario> spec =
        UsuarioSpecifications.distinctResults()
            .and(UsuarioSpecifications.hasAnyRole(UserRole.PROFESSOR, UserRole.ALUNO));

    // When
    List<Usuario> resultados = usuarioRepository.findAll(spec);

    // Then: Deve retornar cada usuário apenas uma vez, mesmo com múltiplas roles
    long countMultiRole =
        resultados.stream().filter(u -> u.getNome().equals("Multi Role User")).count();
    assertEquals(1, countMultiRole, "Usuário com múltiplas roles deve aparecer apenas uma vez");
  }

  @Test
  void combinarMultiplasSpecifications_DevePermitirComposicao() {
    // Given: Combinar múltiplas specifications
    Specification<Usuario> spec =
        UsuarioSpecifications.distinctResults()
            .and(UsuarioSpecifications.searchByText("utfpr"))
            .and(UsuarioSpecifications.hasAnyRole(UserRole.PROFESSOR));

    // When
    List<Usuario> resultados = usuarioRepository.findAll(spec);

    // Then
    assertEquals(1, resultados.size());
    assertEquals("João Silva", resultados.getFirst().getNome());
  }

  // Métodos auxiliares

  private Permissao criarPermissao(UserRole role) {
    Permissao permissao = new Permissao();
    permissao.setNome(role.getAuthority());
    return entityManager.persist(permissao);
  }

  private Usuario criarUsuario(String nome, String email, String documento, Permissao permissao) {
    Usuario usuario = new Usuario();
    usuario.setNome(nome);
    usuario.setEmail(email);
    usuario.setUsername(email);
    usuario.setDocumento(documento);
    usuario.setTelefone("(00) 00000-0000");
    usuario.setPassword("$2a$10$encodedPassword");
    usuario.setEmailVerificado(true);
    usuario.setAtivo(true);
    usuario.setPermissoes(new HashSet<>());
    usuario.getPermissoes().add(permissao);
    return entityManager.persist(usuario);
  }

  private Permissao buscarPermissaoPorRole(UserRole role) {
    return entityManager
        .getEntityManager()
        .createQuery("SELECT p FROM Permissao p WHERE p.nome = :nome", Permissao.class)
        .setParameter("nome", role.getAuthority())
        .getSingleResult();
  }
}
