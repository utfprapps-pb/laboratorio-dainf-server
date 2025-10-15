package br.com.utfpr.gerenciamento.server.repository;

import static org.junit.jupiter.api.Assertions.*;

import br.com.utfpr.gerenciamento.server.model.Permissao;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class UsuarioRepositoryTest {

  @Autowired private UsuarioRepository usuarioRepository;

  @Autowired private TestEntityManager entityManager;

  private Permissao permissaoAluno;
  private Permissao permissaoProfessor;

  @BeforeEach
  void setUp() {
    // Criar permissões
    permissaoAluno = new Permissao();
    permissaoAluno.setNome("ROLE_ALUNO");
    permissaoAluno = entityManager.persist(permissaoAluno);

    permissaoProfessor = new Permissao();
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
    String query = "%João%";
    Pageable pageable = PageRequest.of(0, 10);

    // When
    Page<Usuario> resultado = usuarioRepository.findByNomeLikeIgnoreCase(query, pageable);

    // Then
    assertNotNull(resultado);
    assertEquals(1, resultado.getTotalElements());
    assertEquals(1, resultado.getContent().size());
    assertEquals("João Silva", resultado.getContent().get(0).getNome());
  }

  @Test
  void findByNomeLikeIgnoreCase_DeveFuncionarComCaseInsensitive() {
    // Given
    String query = "%joão%"; // lowercase
    Pageable pageable = PageRequest.of(0, 10);

    // When
    Page<Usuario> resultado = usuarioRepository.findByNomeLikeIgnoreCase(query, pageable);

    // Then
    assertNotNull(resultado);
    assertEquals(1, resultado.getTotalElements());
    assertEquals("João Silva", resultado.getContent().get(0).getNome());
  }

  @Test
  void findByNomeLikeIgnoreCase_DeveRetornarPaginaVaziaQuandoNaoEncontrar() {
    // Given
    String query = "%NaoExiste%";
    Pageable pageable = PageRequest.of(0, 10);

    // When
    Page<Usuario> resultado = usuarioRepository.findByNomeLikeIgnoreCase(query, pageable);

    // Then
    assertNotNull(resultado);
    assertEquals(0, resultado.getTotalElements());
    assertTrue(resultado.getContent().isEmpty());
  }

  @Test
  void findByNomeLikeIgnoreCase_DeveRespeitarTamanhoDaPagina() {
    // Given
    String query = "%a%"; // Deve encontrar todos (João, Maria, Pedro)
    Pageable pageable = PageRequest.of(0, 2); // Página de tamanho 2

    // When
    Page<Usuario> resultado = usuarioRepository.findByNomeLikeIgnoreCase(query, pageable);

    // Then
    assertNotNull(resultado);
    assertEquals(3, resultado.getTotalElements()); // Total de 3
    assertEquals(2, resultado.getContent().size()); // Mas retorna apenas 2
    assertEquals(2, resultado.getTotalPages()); // 2 páginas no total
  }

  @Test
  void findByNomeLikeIgnoreCase_DevePermitirNavegacaoEntrePaginas() {
    // Given
    String query = "%a%";
    Pageable primeiraPage = PageRequest.of(0, 2);
    Pageable segundaPage = PageRequest.of(1, 2);

    // When
    Page<Usuario> resultadoPrimeira =
        usuarioRepository.findByNomeLikeIgnoreCase(query, primeiraPage);
    Page<Usuario> resultadoSegunda = usuarioRepository.findByNomeLikeIgnoreCase(query, segundaPage);

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
    String query = "%Silva%";
    Pageable pageable = PageRequest.of(0, 10);

    // When
    Page<Usuario> resultado = usuarioRepository.findByNomeLikeIgnoreCase(query, pageable);

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
}
