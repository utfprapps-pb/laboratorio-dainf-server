package br.com.utfpr.gerenciamento.server.controller;

import static br.com.utfpr.gerenciamento.server.enumeration.UserRole.ROLE_ALUNO_NAME;
import static br.com.utfpr.gerenciamento.server.enumeration.UserRole.ROLE_PROFESSOR_NAME;

import br.com.utfpr.gerenciamento.server.dto.ReservaResponseDto;
import br.com.utfpr.gerenciamento.server.model.Reserva;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import br.com.utfpr.gerenciamento.server.service.ReservaService;
import br.com.utfpr.gerenciamento.server.util.SecurityUtils;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("reserva")
public class ReservaController extends CrudController<Reserva, Long, ReservaResponseDto> {

  public static final String PREFIXO_ROLE = "ROLE_";
  private final ReservaService reservaService;

  public ReservaController(ReservaService reservaService) {
    this.reservaService = reservaService;
  }

  @Override
  protected CrudService<Reserva, Long, ReservaResponseDto> getService() {
    return reservaService;
  }

  /**
   * Lista todas as reservas do sistema.
   *
   * <p>Alunos e professores veem apenas suas próprias reservas. Administradores e laboratoristas
   * veem todas as reservas do sistema.
   *
   * @return Lista de reservas conforme a role do usuário autenticado
   */
  @Override
  public List<ReservaResponseDto> findAll() {
    List<String> userRoles = SecurityUtils.getAuthenticatedUserRoles();

    if (userRoles.contains(PREFIXO_ROLE + ROLE_ALUNO_NAME)
        || userRoles.contains(PREFIXO_ROLE + ROLE_PROFESSOR_NAME)) {
      return reservaService.findAllByAuthenticatedUser();
    }

    return super.findAll();
  }

  /**
   * Lista todas as reservas do usuário autenticado.
   *
   * <p>Endpoint específico para o usuário autenticado visualizar suas próprias reservas.
   *
   * @return Lista de reservas do usuário autenticado
   */
  @GetMapping("find-all-by-authenticated-user")
  public List<ReservaResponseDto> findAllByAuthenticatedUser() {
    return reservaService.findAllByAuthenticatedUser();
  }

  /**
   * Lista todas as reservas de um item específico.
   *
   * <p>Endpoint administrativo para consulta de reservas por item. Acesso controlado por
   * WebSecurity (requer LABORATORISTA ou ADMINISTRADOR).
   *
   * @param idItem ID do item a consultar reservas
   * @return Lista de reservas do item especificado
   */
  @GetMapping("find-all-by-item/{idItem}")
  public List<ReservaResponseDto> findAllByIdItem(@PathVariable("idItem") Long idItem) {
    return reservaService.findAllByIdItem(idItem);
  }

  /**
   * Lista paginada de reservas com filtro textual usando DTO simplificado.
   *
   * <p>Alunos e professores veem apenas suas próprias reservas. Administradores e laboratoristas
   * veem todas as reservas do sistema.
   *
   * <p><b>Otimização:</b> Retorna apenas campos necessários para listagem via projeção SQL.
   *
   * @param page Número da página (0-indexed)
   * @param size Tamanho da página
   * @param filter Filtro opcional (busca textual em todos os campos)
   * @param order Campo de ordenação (padrão: "id")
   * @param asc Direção da ordenação (true = ASC, false = DESC, padrão: ASC)
   * @return Página de reservas simplificadas conforme a role do usuário autenticado
   */
  @Override
  @GetMapping("page")
  @SuppressWarnings("unchecked")
  public Page<ReservaResponseDto> findAllPaged(
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
      return (Page<ReservaResponseDto>)
          (Page<?>) reservaService.findAllPagedListByUser(filter, pageRequest, username);
    }

    return (Page<ReservaResponseDto>)
        (Page<?>) reservaService.findAllPagedList(filter, pageRequest);
  }

  @Override
  public void postSave(Reserva object) {
    reservaService.sendEmailConfirmacaoReserva(object);
  }
}
