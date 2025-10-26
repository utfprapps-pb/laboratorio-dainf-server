package br.com.utfpr.gerenciamento.server.controller;

import static br.com.utfpr.gerenciamento.server.enumeration.UserRole.ROLE_ADMINISTRADOR_NAME;
import static br.com.utfpr.gerenciamento.server.enumeration.UserRole.ROLE_LABORATORISTA_NAME;

import br.com.utfpr.gerenciamento.server.dto.EmprestimoResponseDto;
import br.com.utfpr.gerenciamento.server.enumeration.TipoItem;
import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.model.filter.EmprestimoFilter;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import br.com.utfpr.gerenciamento.server.service.EmprestimoService;
import br.com.utfpr.gerenciamento.server.service.ItemService;
import br.com.utfpr.gerenciamento.server.util.DateUtil;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("emprestimo")
public class EmprestimoController extends CrudController<Emprestimo, Long> {

  private final EmprestimoService emprestimoService;
  private final ItemService itemService;

  public EmprestimoController(EmprestimoService emprestimoService, ItemService itemService) {
    this.emprestimoService = emprestimoService;
    this.itemService = itemService;
  }

  @Override
  protected CrudService<Emprestimo, Long> getService() {
    return emprestimoService;
  }

  @Override
  public List<Emprestimo> findAll() {
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
    object
        .getEmprestimoItem()
        .forEach(
            saidaItem -> {
              if (saidaItem.getItem().getTipoItem() == TipoItem.C) {
                itemService.diminuiSaldoItem(
                    saidaItem.getItem().getId(), saidaItem.getQtde(), true);
              }
            });
    emprestimoService.sendEmailConfirmacaoEmprestimo(object);
    emprestimoService.finalizeEmprestimo(object);
  }

  @Override
  public void postDelete(Emprestimo object) {
    emprestimoService.cleanupAfterDelete(object);
  }

  @PostMapping("filter")
  public List<EmprestimoResponseDto> filter(@RequestBody EmprestimoFilter emprestimoFilter) {
    return emprestimoService.filter(emprestimoFilter).stream()
        .map(emprestimoService::convertToDto)
        .toList();
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
    return emprestimoService.findAllUsuarioEmprestimo(username).stream()
        .map(emprestimoService::convertToDto)
        .toList();
  }

  @GetMapping("change-prazo-devolucao")
  public void changePrazoDevolucao(
      @RequestParam("id") Long id, @RequestParam("novaData") String novaData) {
    emprestimoService.changePrazoDevolucao(id, DateUtil.parseStringToLocalDate(novaData));
  }

  /**
   * Paginação otimizada de empréstimos com JOIN FETCH e cache.
   *
   * <p><b>Associações fetched via {@link EmprestimoSpecifications#withFetchCollections()}:</b>
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
  @Override
  public Page<Emprestimo> findAllPaged(
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
