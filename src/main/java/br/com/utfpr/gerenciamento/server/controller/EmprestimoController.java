package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.dto.EmprestimoResponseDto;
import br.com.utfpr.gerenciamento.server.ennumeation.StatusDevolucao;
import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.model.EmprestimoDevolucaoItem;
import br.com.utfpr.gerenciamento.server.model.filter.EmprestimoFilter;
import br.com.utfpr.gerenciamento.server.service.*;
import br.com.utfpr.gerenciamento.server.util.DateUtil;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("emprestimo")
public class EmprestimoController extends CrudController<Emprestimo, Long> {

  private final EmprestimoService emprestimoService;
  private final ItemService itemService;
  private final SaidaService saidaService;
  private final ReservaService reservaService;

  public EmprestimoController(
      EmprestimoService emprestimoService,
      ItemService itemService,
      SaidaService saidaService,
      ReservaService reservaService) {
    this.emprestimoService = emprestimoService;
    this.itemService = itemService;
    this.saidaService = saidaService;
    this.reservaService = reservaService;
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
    preSave(emprestimo);
    Emprestimo toReturn = getService().save(emprestimo);
    postSave(emprestimo);
    if (idReserva != 0) reservaService.finalizarReserva(idReserva);

    return emprestimoService.convertToDto(toReturn);
  }

  @PostMapping("save-devolucao")
  public EmprestimoResponseDto saveDevolucao(@RequestBody Emprestimo emprestimo) {

    boolean isPendente =
        emprestimo.getEmprestimoDevolucaoItem().stream()
            .anyMatch(empDevItem -> empDevItem.getStatusDevolucao().equals(StatusDevolucao.P));
    if (!isPendente) {
      emprestimo.setDataDevolucao(LocalDate.now());
    }

    Emprestimo toReturn = emprestimoService.save(emprestimo);
    emprestimo.getEmprestimoDevolucaoItem().stream()
        .filter(empDevItem -> empDevItem.getStatusDevolucao().equals(StatusDevolucao.D))
        .forEach(
            devItem -> itemService.aumentaSaldoItem(devItem.getItem().getId(), devItem.getQtde()));

    List<EmprestimoDevolucaoItem> listItensToSaida =
        emprestimo.getEmprestimoDevolucaoItem().stream()
            .filter(empDevItem -> empDevItem.getStatusDevolucao().equals(StatusDevolucao.S))
            .collect(Collectors.toList());

    if (!listItensToSaida.isEmpty()) {
      saidaService.createSaidaByDevolucaoEmprestimo(listItensToSaida);
    }
    emprestimoService.sendEmailConfirmacaoDevolucao(emprestimo);
    return emprestimoService.convertToDto(toReturn);
  }

  @Override
  public void preSave(Emprestimo object) {
    // se está editando, ele retorna o saldo de todos os itens, para depois baixar novamente com os
    // valores atualizados
    if (object.getId() != null) {
      Emprestimo old = emprestimoService.findOne(object.getId());
      old.getEmprestimoItem().stream()
          .forEach(
              empItem -> {
                itemService.aumentaSaldoItem(empItem.getItem().getId(), empItem.getQtde());
              });
    }
    object.getEmprestimoItem().stream()
        .forEach(
            saidaItem -> {
              if (saidaItem.getItem() != null) {
                itemService.saldoItemIsValid(
                    itemService.getSaldoItem(saidaItem.getItem().getId()), saidaItem.getQtde());
              }
            });
    object.setEmprestimoDevolucaoItem(
        emprestimoService.createEmprestimoItemDevolucao(object.getEmprestimoItem()));

    // caso tiver apenas materiais permanentes no empréstimo, será setado a data de devolução, para
    // finalizar o empréstimo
    // if (object.getEmprestimoDevolucaoItem().size() <= 0) {
      // object.setDataDevolucao(LocalDate.now());
    // }
  }

  @Override
  public void postSave(Emprestimo object) {
    object.getEmprestimoItem().stream()
        .forEach(
            saidaItem ->
                itemService.diminuiSaldoItem(
                    saidaItem.getItem().getId(), saidaItem.getQtde(), true));
    emprestimoService.sendEmailConfirmacaoEmprestimo(object);
  }

  @Override
  public void postDelete(Emprestimo object) {
    object.getEmprestimoItem().stream()
        .forEach(
            saidaItem ->
                itemService.aumentaSaldoItem(saidaItem.getItem().getId(), saidaItem.getQtde()));
    saidaService.deleteSaidaByEmprestimo(object.getId());
  }

  @PostMapping("filter")
  public List<EmprestimoResponseDto> filter(@RequestBody EmprestimoFilter emprestimoFilter) {
    return emprestimoService.filter(emprestimoFilter)
            .stream()
            .map(emprestimoService::convertToDto)
            .toList();
  }

  @GetMapping("find-all-by-username/{username}")
  public List<EmprestimoResponseDto> findAllByUsuarioEmprestimo(@PathVariable("username") String username) {
    return emprestimoService.findAllUsuarioEmprestimo(username)
            .stream()
            .map(emprestimoService::convertToDto)
            .toList();
  }

  @GetMapping("change-prazo-devolucao")
  public void changePrazoDevolucao(
      @RequestParam("id") Long id, @RequestParam("novaData") String novaData) {
    emprestimoService.changePrazoDevolucao(id, DateUtil.parseStringToLocalDate(novaData));
  }
}
