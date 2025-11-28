package br.com.utfpr.gerenciamento.server.controller;

import static br.com.utfpr.gerenciamento.server.enumeration.UserRole.ROLE_ADMINISTRADOR_NAME;
import static br.com.utfpr.gerenciamento.server.enumeration.UserRole.ROLE_ALUNO_NAME;
import static br.com.utfpr.gerenciamento.server.enumeration.UserRole.ROLE_LABORATORISTA_NAME;
import static br.com.utfpr.gerenciamento.server.enumeration.UserRole.ROLE_PROFESSOR_NAME;

import br.com.utfpr.gerenciamento.server.dto.EmprestimoResponseDto;
import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.model.filter.EmprestimoFilter;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import br.com.utfpr.gerenciamento.server.service.EmprestimoService;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import br.com.utfpr.gerenciamento.server.util.DateUtil;
import br.com.utfpr.gerenciamento.server.util.SecurityUtils;
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

  public static final String PREFIXO_ROLE = "ROLE_";
  private final EmprestimoService emprestimoService;
  private final UsuarioService usuarioService;

  public EmprestimoController(EmprestimoService emprestimoService, UsuarioService usuarioService) {
    this.emprestimoService = emprestimoService;
    this.usuarioService = usuarioService;
  }

  @Override
  protected CrudService<Emprestimo, Long, EmprestimoResponseDto> getService() {
    return emprestimoService;
  }

  /**
   * Lista todos os empréstimos abertos.
   *
   * <p>Alunos e professores veem apenas seus próprios empréstimos abertos. Administradores e
   * laboratoristas veem todos os empréstimos abertos do sistema.
   *
   * @return Lista de empréstimos abertos conforme a role do usuário autenticado
   */
  @Override
  public List<EmprestimoResponseDto> findAll() {
    String username = SecurityUtils.getAuthenticatedUsername();
    List<String> userRoles = SecurityUtils.getAuthenticatedUserRoles();

    if (userRoles.contains(PREFIXO_ROLE + ROLE_ALUNO_NAME)
        || userRoles.contains(PREFIXO_ROLE + ROLE_PROFESSOR_NAME)) {
      return this.emprestimoService.findAllEmprestimosAbertosByUsuario(username);
    }

    return this.emprestimoService.findAllEmprestimosAbertos();
  }

  /**
   * Processa criação/edição de empréstimo com toda lógica de negócio.
   *
   * @param emprestimo Empréstimo a ser processado
   * @param idReserva ID da reserva a finalizar (0 se não houver)
   * @return DTO do empréstimo salvo
   */
  @PostMapping("save-emprestimo")
  public EmprestimoResponseDto save(
      @RequestBody Emprestimo emprestimo, @RequestParam("idReserva") Long idReserva) {
    return emprestimoService.processEmprestimo(emprestimo, idReserva);
  }

  /**
   * Processa devolução de empréstimo com toda lógica de negócio.
   *
   * @param emprestimo Empréstimo com dados de devolução
   * @return DTO do empréstimo atualizado
   */
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

  /**
   * Filtra empréstimos com base nos critérios informados.
   *
   * <p>Alunos e professores veem apenas seus próprios empréstimos. Administradores e laboratoristas
   * veem todos os empréstimos do sistema.
   *
   * @param emprestimoFilter Critérios de filtragem
   * @return Lista de empréstimos filtrados conforme a role do usuário autenticado
   */
  @PostMapping("filter")
  public List<EmprestimoResponseDto> filter(@RequestBody EmprestimoFilter emprestimoFilter) {
    String username = SecurityUtils.getAuthenticatedUsername();
    List<String> userRoles = SecurityUtils.getAuthenticatedUserRoles();

    if (userRoles.contains(PREFIXO_ROLE + ROLE_ALUNO_NAME)
        || userRoles.contains(PREFIXO_ROLE + ROLE_PROFESSOR_NAME)) {
      Usuario usuario = usuarioService.toEntity(usuarioService.findByUsername(username));
      emprestimoFilter.setUsuarioEmprestimo(usuario);
    }

    return emprestimoService.filter(emprestimoFilter);
  }

  /**
   * Lista todos os empréstimos de um usuário específico.
   *
   * <p>Endpoint mantido para compatibilidade. Acesso controlado por WebSecurity: - Usuário só pode
   * ver seus próprios empréstimos - Admin/Laboratorista podem ver todos os empréstimos
   *
   * @param username Username do usuário a consultar
   * @return Lista de empréstimos do usuário especificado
   */
  @GetMapping("find-all-by-username/{username}")
  @PreAuthorize(
      "authentication.name == #username || hasAnyRole('"
          + ROLE_LABORATORISTA_NAME
          + "', '"
          + ROLE_ADMINISTRADOR_NAME
          + "')")
  public List<EmprestimoResponseDto> findAllByUsuarioEmprestimo(
      @PathVariable("username") String username) {
    return emprestimoService.findAllUsuarioEmprestimo(username);
  }

  /**
   * Altera o prazo de devolução de um empréstimo.
   *
   * <p>Endpoint administrativo para alterar prazos de devolução. Acesso controlado por WebSecurity
   * (requer LABORATORISTA ou ADMINISTRADOR).
   *
   * @param id ID do empréstimo
   * @param novaData Nova data de devolução (formato: dd/MM/yyyy)
   */
  @GetMapping("change-prazo-devolucao")
  public void changePrazoDevolucao(
      @RequestParam("id") Long id, @RequestParam("novaData") String novaData) {
    emprestimoService.changePrazoDevolucao(id, DateUtil.parseStringToLocalDate(novaData));
  }

  @PreAuthorize(
      "hasAnyAuthority('" + ROLE_LABORATORISTA_NAME + "', '" + ROLE_ADMINISTRADOR_NAME + "')")
  @GetMapping("find-by-item/{itemId}")
  public List<EmprestimoResponseDto> findByItemId(@PathVariable("itemId") Long itemId) {
    return emprestimoService.findAllByItemId(itemId);
  }

  /**
   * Lista paginada de empréstimos com filtro textual.
   *
   * <p>Alunos e professores veem apenas seus próprios empréstimos. Administradores e laboratoristas
   * veem todos os empréstimos do sistema.
   *
   * @param page Número da página (0-indexed)
   * @param size Tamanho da página
   * @param filter Filtro opcional (busca textual em todos os campos)
   * @param order Campo de ordenação (padrão: "id")
   * @param asc Direção da ordenação (true = ASC, false = DESC, padrão: ASC)
   * @return Página de empréstimos conforme a role do usuário autenticado
   */
  @GetMapping("page")
  @Override
  public Page<EmprestimoResponseDto> findAllPaged(
      @RequestParam("page") int page,
      @RequestParam("size") int size,
      @RequestParam(required = false) String filter,
      @RequestParam(required = false) String order,
      @RequestParam(required = false) Boolean asc) {

    Sort sort = Sort.by("id");
    if (order != null && asc != null) {
      sort = Sort.by(asc ? Sort.Direction.ASC : Sort.Direction.DESC, order);
    }
    PageRequest pageRequest = PageRequest.of(page, size, sort);

    String username = SecurityUtils.getAuthenticatedUsername();
    List<String> userRoles = SecurityUtils.getAuthenticatedUserRoles();

    if (userRoles.contains(PREFIXO_ROLE + ROLE_ALUNO_NAME)
        || userRoles.contains(PREFIXO_ROLE + ROLE_PROFESSOR_NAME)) {
      return emprestimoService.findAllPagedByUserWithTextFilter(filter, pageRequest, username);
    }

    return emprestimoService.findAllPagedWithTextFilter(filter, pageRequest);
  }
}
