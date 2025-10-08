package br.com.utfpr.gerenciamento.server.fixture;

import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.model.EmprestimoItem;
import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.model.Permissao;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Fixture para criação de dados de teste para entidade Emprestimo. Fornece métodos convenientes
 * para construir cenários comuns de empréstimo.
 *
 * <p>Uso recomendado em testes de integração (@DataJpaTest):
 *
 * <pre>
 * EmprestimoFixture fixture = new EmprestimoFixture();
 * Permissao permissao = fixture.criarPermissao("ALUNO");
 * Usuario aluno = fixture.criarUsuario("aluno@utfpr.edu.br", "João Silva", permissao);
 * Item laptop = fixture.criarItem("Laptop Dell", "Notebook para desenvolvimento");
 *
 * Emprestimo emprestimo = fixture.criarEmprestimoAtrasado(aluno, responsavel, laptop);
 * emprestimoRepository.save(emprestimo);
 * </pre>
 *
 * @author Rodrigo Izidoro
 * @since 2025-10-07
 */
public class EmprestimoFixture {

  /** Cria permissão com nome especificado. */
  public Permissao criarPermissao(String nome) {
    Permissao permissao = new Permissao();
    permissao.setNome(nome);
    return permissao;
  }

  /** Cria usuário com permissão associada e dados válidos. */
  public Usuario criarUsuario(String email, String nome, Permissao permissao) {
    Usuario usuario = new Usuario();
    usuario.setUsername(email);
    usuario.setNome(nome);
    usuario.setEmail(email);
    usuario.setEmailVerificado(true);
    usuario.setPassword("senha123");
    usuario.setTelefone("41999999999");
    usuario.setPermissoes(new HashSet<>());
    usuario.getPermissoes().add(permissao);
    return usuario;
  }

  /** Cria item com dados básicos. */
  public Item criarItem(String nome, String descricao) {
    Item item = new Item();
    item.setNome(nome);
    item.setDescricao(descricao);
    item.setQtdeMinima(BigDecimal.ONE);
    return item;
  }

  /**
   * Cria empréstimo ATRASADO (prazo vencido, sem devolução).
   *
   * <p>Características:
   *
   * <ul>
   *   <li>Data empréstimo: 10 dias atrás
   *   <li>Prazo devolução: 3 dias atrás (VENCIDO)
   *   <li>Data devolução: null (NÃO DEVOLVIDO)
   * </ul>
   */
  public Emprestimo criarEmprestimoAtrasado(
      Usuario usuarioEmprestimo, Usuario usuarioResponsavel, Item item) {
    Emprestimo emprestimo = new Emprestimo();
    emprestimo.setUsuarioEmprestimo(usuarioEmprestimo);
    emprestimo.setUsuarioResponsavel(usuarioResponsavel);
    emprestimo.setDataEmprestimo(LocalDate.now().minusDays(10));
    emprestimo.setPrazoDevolucao(LocalDate.now().minusDays(3)); // VENCIDO
    emprestimo.setDataDevolucao(null); // NÃO DEVOLVIDO
    emprestimo.setObservacao("Empréstimo atrasado");
    emprestimo.setEmprestimoItem(criarEmprestimoItem(emprestimo, item));
    return emprestimo;
  }

  /**
   * Cria empréstimo PENDENTE (prazo vigente, sem devolução).
   *
   * <p>Características:
   *
   * <ul>
   *   <li>Data empréstimo: 2 dias atrás
   *   <li>Prazo devolução: 5 dias no futuro (VIGENTE)
   *   <li>Data devolução: null (EM ANDAMENTO)
   * </ul>
   */
  public Emprestimo criarEmprestimoPendente(
      Usuario usuarioEmprestimo, Usuario usuarioResponsavel, Item item) {
    Emprestimo emprestimo = new Emprestimo();
    emprestimo.setUsuarioEmprestimo(usuarioEmprestimo);
    emprestimo.setUsuarioResponsavel(usuarioResponsavel);
    emprestimo.setDataEmprestimo(LocalDate.now().minusDays(2));
    emprestimo.setPrazoDevolucao(LocalDate.now().plusDays(5)); // VIGENTE
    emprestimo.setDataDevolucao(null); // EM ANDAMENTO
    emprestimo.setObservacao("Empréstimo em andamento");
    emprestimo.setEmprestimoItem(criarEmprestimoItem(emprestimo, item));
    return emprestimo;
  }

  /**
   * Cria empréstimo FINALIZADO (devolvido no prazo).
   *
   * <p>Características:
   *
   * <ul>
   *   <li>Data empréstimo: 7 dias atrás
   *   <li>Prazo devolução: 2 dias atrás
   *   <li>Data devolução: 1 dia atrás (DEVOLVIDO)
   * </ul>
   */
  public Emprestimo criarEmprestimoFinalizado(
      Usuario usuarioEmprestimo, Usuario usuarioResponsavel, Item item) {
    Emprestimo emprestimo = new Emprestimo();
    emprestimo.setUsuarioEmprestimo(usuarioEmprestimo);
    emprestimo.setUsuarioResponsavel(usuarioResponsavel);
    emprestimo.setDataEmprestimo(LocalDate.now().minusDays(7));
    emprestimo.setPrazoDevolucao(LocalDate.now().minusDays(2));
    emprestimo.setDataDevolucao(LocalDate.now().minusDays(1)); // DEVOLVIDO
    emprestimo.setObservacao("Empréstimo finalizado");
    emprestimo.setEmprestimoItem(criarEmprestimoItem(emprestimo, item));
    return emprestimo;
  }

  /**
   * Cria empréstimo com datas customizadas. Permite cenários de teste específicos com controle
   * total sobre timeline.
   */
  public Emprestimo criarEmprestimoCustom(
      Usuario usuarioEmprestimo,
      Usuario usuarioResponsavel,
      Item item,
      LocalDate dataEmprestimo,
      LocalDate prazoDevolucao,
      LocalDate dataDevolucao) {
    Emprestimo emprestimo = new Emprestimo();
    emprestimo.setUsuarioEmprestimo(usuarioEmprestimo);
    emprestimo.setUsuarioResponsavel(usuarioResponsavel);
    emprestimo.setDataEmprestimo(dataEmprestimo);
    emprestimo.setPrazoDevolucao(prazoDevolucao);
    emprestimo.setDataDevolucao(dataDevolucao);
    emprestimo.setEmprestimoItem(criarEmprestimoItem(emprestimo, item));
    return emprestimo;
  }

  /**
   * Helper method para criar item de empréstimo. Necessário pois Emprestimo tem validação @NotNull
   * em emprestimoItem.
   *
   * <p>IMPORTANTE: Não persiste o EmprestimoItem - será persistido em cascata com Emprestimo.
   */
  private List<EmprestimoItem> criarEmprestimoItem(Emprestimo emprestimo, Item item) {
    EmprestimoItem emprestimoItem = new EmprestimoItem();
    emprestimoItem.setEmprestimo(emprestimo);
    emprestimoItem.setItem(item);
    emprestimoItem.setQtde(BigDecimal.ONE);

    List<EmprestimoItem> items = new ArrayList<>();
    items.add(emprestimoItem);
    return items;
  }
}
