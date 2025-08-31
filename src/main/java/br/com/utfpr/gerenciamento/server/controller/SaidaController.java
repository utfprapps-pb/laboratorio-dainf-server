package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.model.Saida;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import br.com.utfpr.gerenciamento.server.service.ItemService;
import br.com.utfpr.gerenciamento.server.service.SaidaService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("saida")
public class SaidaController extends CrudController<Saida, Long> {

  private final SaidaService saidaService;
  private final ItemService itemService;

  public SaidaController(SaidaService saidaService, ItemService itemService) {
    this.saidaService = saidaService;
    this.itemService = itemService;
  }

  @Override
  protected CrudService<Saida, Long> getService() {
    return saidaService;
  }

  @Override
  public void preSave(Saida object) {
    // se está editando, ele retorna o saldo de todos os itens, para depois baixar novamente com os
    // valores atualizados
    if (object.getId() != null) {
      Saida old = saidaService.findOne(object.getId());
      old.getSaidaItem().stream()
          .forEach(
              saidaItem -> {
                itemService.aumentaSaldoItem(saidaItem.getItem().getId(), saidaItem.getQtde());
              });
    }
    object.getSaidaItem().stream()
        .forEach(
            saidaItem -> {
              if (saidaItem.getItem() != null) {
                itemService.saldoItemIsValid(
                    itemService.getSaldoItem(saidaItem.getItem().getId()), saidaItem.getQtde());
              }
            });
  }

  @Override
  public void postSave(Saida object) {
    object.getSaidaItem().stream()
        .forEach(
            saidaItem -> {
              itemService.diminuiSaldoItem(saidaItem.getItem().getId(), saidaItem.getQtde(), true);
            });
  }

  @Override
  public void postDelete(Saida object) {
    object.getSaidaItem().stream()
        .forEach(
            saidaItem -> {
              itemService.aumentaSaldoItem(saidaItem.getItem().getId(), saidaItem.getQtde());
            });
  }
}
