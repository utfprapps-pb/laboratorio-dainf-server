package br.com.utfpr.gerenciamento.server.specification;

import br.com.utfpr.gerenciamento.server.enumeration.EmprestimoStatus;
import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.model.filter.DateRange;
import br.com.utfpr.gerenciamento.server.model.filter.EmprestimoFilter;
import jakarta.persistence.criteria.*;
import java.time.LocalDate;
import java.util.Objects;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications para consultas de Emprestimo usando Criteria API.
 *
 * <p>Esta classe substitui a implementação manual JDBC (EmprestimoFilterRepositoryImpl) eliminando
 * o problema N+1 por JOIN FETCH adequados.
 *
 * <p>Benefícios da migração: - Elimina ~200 queries → 1 query (melhoria de 90-95%) - Type-safe com
 * Criteria API - Aproveita cache de primeiro nível do Hibernate - Manutenibilidade melhorada
 *
 * @author Rodrigo Izidoro
 * @since 2025-10-07
 */
public class EmprestimoSpecifications {

  static final String DATA_DEVOLUCAO = "dataDevolucao";
  static final String PRAZO_DEVOLUCAO = "prazoDevolucao";
  static final String DATA_EMPRESTIMO = "dataEmprestimo";
  static final String USERNAME = "username";
  static final String USUARIO_RESPONSAVEL = "usuarioResponsavel";
  static final String USUARIO_EMPRESTIMO = "usuarioEmprestimo";

  private EmprestimoSpecifications() {}

  /**
   * Cria Specification completa a partir de EmprestimoFilter.
   *
   * <p>Utiliza JOIN FETCH para carregar usuarioEmprestimo e permissoes em uma única query,
   * eliminando N+1.
   *
   * @param filter Filtro com critérios de busca (pode ser null)
   * @return Specification configurada com fetches e predicados
   */
  public static Specification<Emprestimo> fromFilter(EmprestimoFilter filter) {
    return fromFilter(filter, false);
  }

  /**
   * Cria Specification otimizada para paginação com JOIN FETCH completo.
   *
   * <p>Versão estendida que carrega também emprestimoItem e emprestimoDevolucaoItem para eliminar
   * N+1 durante conversão para DTO em paginação.
   *
   * @param filter Filtro com critérios de busca (pode ser null)
   * @param fetchCollections Se true, carrega collections (emprestimoItem, emprestimoDevolucaoItem)
   * @return Specification configurada com fetches completos
   */
  public static Specification<Emprestimo> fromFilter(
      EmprestimoFilter filter, boolean fetchCollections) {
    return (root, query, cb) -> {
      // Previne duplicação de resultados em queries de count
      if (Objects.requireNonNull(query).getResultType() != Long.class && query.getResultType() != long.class) {
        // Define distinct antes de realizar fetches para evitar duplicação de resultados
        query.distinct(true);

        // JOIN FETCH para usuarioEmprestimo (elimina N+1)
        Fetch<Emprestimo, Usuario> usuarioEmprestimoFetch =
            root.fetch(USUARIO_EMPRESTIMO, JoinType.LEFT);

        // JOIN FETCH para permissoes do usuario (elimina segundo N+1)
        // Nota: Permissoes agora é LAZY, mas precisamos carregar aqui para evitar
        // LazyInitializationException ao serializar para DTO
        usuarioEmprestimoFetch.fetch("permissoes", JoinType.LEFT);

        // JOIN FETCH para usuarioResponsavel (se necessário)
        Fetch<Emprestimo, Usuario> usuarioResponsavelFetch =
            root.fetch(USUARIO_RESPONSAVEL, JoinType.LEFT);
        usuarioResponsavelFetch.fetch("permissoes", JoinType.LEFT);

        // JOIN FETCH para collections (opcional, para paginação)
        if (fetchCollections) {
          // Fetch emprestimoItem e nested item
          Fetch<Object, Object> emprestimoItemFetch = root.fetch("emprestimoItem", JoinType.LEFT);
          emprestimoItemFetch.fetch("item", JoinType.LEFT);

          // Fetch emprestimoDevolucaoItem e nested item
          Fetch<Object, Object> devolucaoItemFetch =
              root.fetch("emprestimoDevolucaoItem", JoinType.LEFT);
          devolucaoItemFetch.fetch("item", JoinType.LEFT);
        }
      }

      return construirPredicado(filter, root, query, cb);
    };
  }

  /**
   * Constrói o predicado WHERE baseado nos filtros fornecidos.
   *
   * @param filter Filtro com critérios (pode ser null)
   * @param root Root da query
   * @param query CriteriaQuery
   * @param cb CriteriaBuilder
   * @return Predicado combinado com AND
   */
  private static Predicate construirPredicado(
      EmprestimoFilter filter, Root<Emprestimo> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

    Predicate predicado = cb.conjunction();

    if (filter == null) {
      return predicado;
    }

    predicado = aplicarFiltroUsuarioEmprestimo(filter, root, cb, predicado);
    predicado = aplicarFiltroUsuarioResponsavel(filter, root, cb, predicado);
    predicado = aplicarFiltroDataEmprestimo(filter, root, cb, predicado);
    predicado = aplicarFiltroStatus(filter, root, cb, predicado);

    return predicado;
  }

  /**
   * Aplica filtro por usuarioEmprestimo (ID ou username).
   *
   * @param filter Filtro de empréstimo
   * @param root Root da query
   * @param cb CriteriaBuilder
   * @param predicado Predicado atual
   * @return Predicado atualizado
   */
  private static Predicate aplicarFiltroUsuarioEmprestimo(
      EmprestimoFilter filter, Root<Emprestimo> root, CriteriaBuilder cb, Predicate predicado) {

    if (filter.getUsuarioEmprestimo() == null) {
      return predicado;
    }

    Join<Emprestimo, Usuario> usuarioJoin = root.join(USUARIO_EMPRESTIMO, JoinType.LEFT);
    return aplicarFiltroUsuario(filter.getUsuarioEmprestimo(), usuarioJoin, cb, predicado);
  }

  /**
   * Aplica filtro por usuarioResponsavel (ID ou username).
   *
   * @param filter Filtro de empréstimo
   * @param root Root da query
   * @param cb CriteriaBuilder
   * @param predicado Predicado atual
   * @return Predicado atualizado
   */
  private static Predicate aplicarFiltroUsuarioResponsavel(
      EmprestimoFilter filter, Root<Emprestimo> root, CriteriaBuilder cb, Predicate predicado) {

    if (filter.getUsuarioResponsavel() == null) {
      return predicado;
    }

    Join<Emprestimo, Usuario> usuarioJoin = root.join(USUARIO_RESPONSAVEL, JoinType.LEFT);
    return aplicarFiltroUsuario(filter.getUsuarioResponsavel(), usuarioJoin, cb, predicado);
  }

  /**
   * Aplica filtro genérico por usuário (ID ou username).
   *
   * @param usuario Filtro de usuário
   * @param usuarioJoin Join de usuario
   * @param cb CriteriaBuilder
   * @param predicado Predicado atual
   * @return Predicado atualizado
   */
  private static Predicate aplicarFiltroUsuario(
      Usuario usuario,
      Join<Emprestimo, Usuario> usuarioJoin,
      CriteriaBuilder cb,
      Predicate predicado) {

    if (usuario.getId() != null) {
      return cb.and(predicado, cb.equal(usuarioJoin.get("id"), usuario.getId()));
    }

    if (usuario.getUsername() != null) {
      return cb.and(predicado, cb.equal(usuarioJoin.get(USERNAME), usuario.getUsername()));
    }

    return predicado;
  }

  /**
   * Aplica filtro por data de empréstimo usando DateRange.
   *
   * @param filter Filtro de empréstimo
   * @param root Root da query
   * @param cb CriteriaBuilder
   * @param predicado Predicado atual
   * @return Predicado atualizado
   */
  private static Predicate aplicarFiltroDataEmprestimo(
      EmprestimoFilter filter, Root<Emprestimo> root, CriteriaBuilder cb, Predicate predicado) {

    DateRange dateRange = filter.getDateRangeEmprestimo();
    if (dateRange == null) {
      return predicado;
    }

    if (dateRange.hasInicio()) {
      predicado =
          cb.and(predicado, cb.greaterThanOrEqualTo(root.get(DATA_EMPRESTIMO), dateRange.inicio()));
    }

    if (dateRange.hasFim()) {
      predicado =
          cb.and(predicado, cb.lessThanOrEqualTo(root.get(DATA_EMPRESTIMO), dateRange.fim()));
    }

    return predicado;
  }

  /**
   * Aplica filtro por status do empréstimo.
   *
   * @param filter Filtro de empréstimo
   * @param root Root da query
   * @param cb CriteriaBuilder
   * @param predicado Predicado atual
   * @return Predicado atualizado
   */
  private static Predicate aplicarFiltroStatus(
      EmprestimoFilter filter, Root<Emprestimo> root, CriteriaBuilder cb, Predicate predicado) {

    if (filter.getStatus() == null || filter.getStatus() == EmprestimoStatus.TODOS) {
      return predicado;
    }

    Predicate statusPredicado = construirPredicadoStatus(filter.getStatus(), root, cb);
    return cb.and(predicado, statusPredicado);
  }

  /**
   * Constrói predicado para filtro de status do empréstimo.
   *
   * <p>Status disponíveis: - ATRASADO: sem data de devolução e prazo vencido - EM_ANDAMENTO: sem
   * data de devolução e prazo não vencido - FINALIZADO: com data de devolução
   *
   * @param status Enum do status (ATRASADO/EM_ANDAMENTO/FINALIZADO)
   * @param root Root da query
   * @param cb CriteriaBuilder
   * @return Predicado para o status especificado
   */
  private static Predicate construirPredicadoStatus(
      EmprestimoStatus status, Root<Emprestimo> root, CriteriaBuilder cb) {

    LocalDate hoje = LocalDate.now();

    return switch (status) {
      case ATRASADO -> // Atrasado: sem devolução e prazo vencido
          cb.and(cb.isNull(root.get(DATA_DEVOLUCAO)), cb.lessThan(root.get(PRAZO_DEVOLUCAO), hoje));

      case EM_ANDAMENTO -> // Em andamento: sem devolução e prazo não vencido
          cb.and(
              cb.isNull(root.get(DATA_DEVOLUCAO)),
              cb.greaterThanOrEqualTo(root.get(PRAZO_DEVOLUCAO), hoje));

      case FINALIZADO -> // Finalizado: tem data de devolução
          cb.isNotNull(root.get(DATA_DEVOLUCAO));

      case TODOS -> cb.conjunction();
    };
  }
}
