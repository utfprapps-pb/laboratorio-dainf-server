package br.com.utfpr.gerenciamento.server.controller;

import static br.com.utfpr.gerenciamento.server.enumeration.UserRole.ROLE_ADMINISTRADOR_NAME;
import static br.com.utfpr.gerenciamento.server.enumeration.UserRole.ROLE_LABORATORISTA_NAME;

import br.com.utfpr.gerenciamento.server.dto.EmprestimoResponseDto;
import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.model.filter.EmprestimoFilter;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import br.com.utfpr.gerenciamento.server.service.EmprestimoService;
import br.com.utfpr.gerenciamento.server.util.DateUtil;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsável pelos endpoints de Empréstimo.
 *
 * <p>Endpoints disponíveis:
 *
 * <ul>
 *   <li>GET /emprestimo - Lista empréstimos abertos
 *   <li>GET /emprestimo/{id} - Busca empréstimo por ID
 *   <li>POST /emprestimo/save-emprestimo - Salva novo empréstimo
 *   <li>POST /emprestimo/save-devolucao - Salva devolução
 *   <li>POST /emprestimo/filter - Filtra empréstimos
 *   <li>GET /emprestimo/find-all-by-username/{username} - Busca empréstimos por usuário
 *   <li>GET /emprestimo/find-by-item/{itemId} - Busca empréstimos por item
 *   <li>GET /emprestimo/change-prazo-devolucao - Altera prazo de devolução
 *   <li>GET /emprestimo/page - Paginação de empréstimos
 * </ul>
 */
@RestController
@RequestMapping("emprestimo")
public class EmprestimoController extends CrudController<Emprestimo, Long, EmprestimoResponseDto> {

  private final EmprestimoService emprestimoService;

  public EmprestimoController(EmprestimoService emprestimoService) {
    this.emprestimoService = emprestimoService;
  }

  @Override
  protected CrudService<Emprestimo, Long, EmprestimoResponseDto> getService() {
    return emprestimoService;
  }

  @Override
  public List<EmprestimoResponseDto> findAll() {
    return this.emprestimoService.findAllEmprestimosAbertos();
  }

  @PostMapping("save-emprestimo")
  public EmprestimoResponseDto save(
      @RequestBody Emprestimo emprestimo, @RequestParam("idReserva") Long idReserva) {
    return emprestimoService.processEmprestimo(emprestimo, idReserva);
  }

  @PostMapping("save-devolucao")
  public EmprestimoResponseDto saveDevolucao(@RequestBody Emprestimo emprestimo) {
    return emprestimoService.processDevolucao(emprestimo);
  }

  @Override
  public void preSave(Emprestimo object) {
    emprestimoService.prepareEmprestimo(object);
  }

  @Override
  public void postSave(Emprestimo object) {
    emprestimoService.finalizeEmprestimo(object);
  }

  @Override
  public void postDelete(Emprestimo object) {
    emprestimoService.cleanupAfterDelete(object);
  }

  @PostMapping("filter")
  public List<EmprestimoResponseDto> filter(@RequestBody EmprestimoFilter emprestimoFilter) {
    return emprestimoService.filter(emprestimoFilter);
  }

  @PreAuthorize(
      "#username == authentication.name or hasAnyAuthority('"
          + ROLE_LABORATORISTA_NAME
          + "', '"
          + ROLE_ADMINISTRADOR_NAME
          + "')")
  @GetMapping("find-all-by-username/{username}")
  public List<EmprestimoResponseDto> findAllByUsuarioEmprestimo(
      @PathVariable("username") String username) {
    return emprestimoService.findAllUsuarioEmprestimo(username);
  }

  @PreAuthorize(
      "hasAnyAuthority('" + ROLE_LABORATORISTA_NAME + "', '" + ROLE_ADMINISTRADOR_NAME + "')")
  @GetMapping("find-by-item/{itemId}")
  public List<EmprestimoResponseDto> findByItemId(@PathVariable("itemId") Long itemId) {
    return emprestimoService.findAllByItemId(itemId);
  }

  @GetMapping("change-prazo-devolucao")
  public void changePrazoDevolucao(
      @RequestParam("id") Long id, @RequestParam("novaData") String novaData) {
    emprestimoService.changePrazoDevolucao(id, DateUtil.parseStringToLocalDate(novaData));
  }

  /**
   * Paginação otimizada de empréstimos com JOIN FETCH e cache.
   *
   * <p><b>Associações fetched via :</b>
   *
   * <ul>
   *   <li>{@code usuarioEmprestimo} - Usuário que fez o empréstimo (LEFT JOIN FETCH)
   *   <li>{@code usuarioEmprestimo.permissoes} - Permissões do usuário empréstimo (LEFT JOIN FETCH)
   *   <li>{@code usuarioResponsavel} - Usuário responsável pela liberação (LEFT JOIN FETCH)
   *   <li>{@code usuarioResponsavel.permissoes} - Permissões do responsável (LEFT JOIN FETCH)
   *   <li>{@code emprestimoItem} - Itens do empréstimo (LEFT JOIN FETCH)
   * </ul>
   *
   * <p><b>NÃO fetched:</b> {@code emprestimoDevolucaoItem} (usa @BatchSize para evitar
   * MultipleBagFetchException)
   *
   * <p><b>Cache:</b> 5 minutos TTL com cache key estável (filter + pageable), resolvendo problema
   * de Specification com equals/hashCode instável.
   *
   * <p>Esta estratégia previne N+1 queries mantendo performance ideal com DISTINCT e evitando
   * cartesian product ao fetch apenas uma collection @OneToMany.
   *
   * <p>TODO: Padronizar demais findAllPaged depois
   *
   * @param page Número da página (0-indexed)
   * @param size Tamanho da página
   * @param filter Filtro opcional (busca textual em todos os campos)
   * @param order Campo de ordenação (padrão: "id")
   * @param asc Direção da ordenação (true = ASC, false = DESC, padrão: ASC)
   * @return Página de entidades {@link Emprestimo} otimizada com associações carregadas e cache
   */
  @GetMapping("page")
  @Override
  public Page<EmprestimoResponseDto> findAllPaged(
      @RequestParam("page") int page,
      @RequestParam("size") int size,
      @RequestParam(required = false) String filter,
      @RequestParam(required = false) String order,
      @RequestParam(required = false) Boolean asc) {

    // Configura ordenação
    Sort sort = Sort.by("id");
    if (order != null && asc != null) {
      sort = Sort.by(asc ? Sort.Direction.ASC : Sort.Direction.DESC, order);
    }
    PageRequest pageRequest = PageRequest.of(page, size, sort);

    // Usa novo método com cache key estável
    return emprestimoService.findAllPagedWithTextFilter(filter, pageRequest);
  }
}
