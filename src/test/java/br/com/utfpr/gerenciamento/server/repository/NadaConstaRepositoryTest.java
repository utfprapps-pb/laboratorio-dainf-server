package br.com.utfpr.gerenciamento.server.repository;

import static org.junit.jupiter.api.Assertions.*;

import br.com.utfpr.gerenciamento.server.enumeration.NadaConstaStatus;
import br.com.utfpr.gerenciamento.server.model.NadaConsta;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import java.time.LocalDateTime;
import java.util.List;
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
class NadaConstaRepositoryTest {

  @Autowired private NadaConstaRepository nadaConstaRepository;
  @Autowired private TestEntityManager entityManager;

  private Usuario usuario;
  private Usuario usuario2;

  @BeforeEach
  void setUp() {
    usuario =
        Usuario.builder()
            .nome("João Teste")
            .username("joaoteste")
            .email("joao@nada.com")
            .password("senha123")
            .telefone("41999999001")
            .build();
    usuario = entityManager.persist(usuario);

    usuario2 =
        Usuario.builder()
            .nome("Maria Teste")
            .username("mariateste")
            .email("maria@nada.com")
            .password("senha123")
            .telefone("41999999002")
            .build();
    usuario2 = entityManager.persist(usuario2);

    NadaConsta nc1 = NadaConsta.builder().usuario(usuario).status(NadaConstaStatus.PENDING).build();
    entityManager.persist(nc1);

    NadaConsta nc2 =
        NadaConsta.builder()
            .usuario(usuario)
            .status(NadaConstaStatus.COMPLETED)
            .sendAt(LocalDateTime.now())
            .build();
    entityManager.persist(nc2);

    NadaConsta nc3 = NadaConsta.builder().usuario(usuario2).status(NadaConstaStatus.FAILED).build();
    entityManager.persist(nc3);

    entityManager.flush();
    entityManager.clear();
  }

  @Test
  void persistirNadaConsta_DeveSalvarCamposObrigatorios() {
    NadaConsta nc = NadaConsta.builder().usuario(usuario).status(NadaConstaStatus.PENDING).build();
    NadaConsta salvo = nadaConstaRepository.save(nc);
    assertNotNull(salvo.getId());
    assertEquals(NadaConstaStatus.PENDING, salvo.getStatus());
    assertEquals(usuario.getId(), salvo.getUsuario().getId());
  }

  @Test
  void findAllByUsuario_DeveRetornarSomenteDoUsuario() {
    List<NadaConsta> lista = nadaConstaRepository.findAllByUsuario(usuario);
    assertEquals(2, lista.size());
    assertTrue(lista.stream().allMatch(nc -> nc.getUsuario().getId().equals(usuario.getId())));
  }

  @Test
  void findAllByUsuario_DeveRetornarVazioParaUsuarioSemNadaConsta() {
    Usuario novo =
        Usuario.builder()
            .nome("Novo")
            .username("novo")
            .email("novo@nada.com")
            .password("senha123")
            .telefone("41999999003")
            .build();
    novo = entityManager.persist(novo);
    List<NadaConsta> lista = nadaConstaRepository.findAllByUsuario(novo);
    assertTrue(lista.isEmpty());
  }

  @Test
  void findAll_DeveRetornarTodosNadaConstaPaginados() {
    Pageable pageable = PageRequest.of(0, 2);
    Page<NadaConsta> pagina = nadaConstaRepository.findAll(pageable);
    assertEquals(2, pagina.getContent().size());
    assertEquals(3, pagina.getTotalElements());
  }

  @Test
  void findAll_DeveRetornarPaginaVazia() {
    Pageable pageable = PageRequest.of(2, 2);
    Page<NadaConsta> pagina = nadaConstaRepository.findAll(pageable);
    assertTrue(pagina.getContent().isEmpty());
    assertEquals(3, pagina.getTotalElements());
  }

  @Test
  void statusPadrao_DeveSerPending() {
    NadaConsta nc = new NadaConsta();
    assertEquals(NadaConstaStatus.PENDING, nc.getStatus());
  }

  @Test
  void podePersistirComSendAtENull() {
    NadaConsta nc =
        NadaConsta.builder().usuario(usuario).status(NadaConstaStatus.PENDING).sendAt(null).build();
    NadaConsta salvo = nadaConstaRepository.save(nc);
    assertNull(salvo.getSendAt());
  }
}
