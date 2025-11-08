package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.dto.CompraResponseDTO;
import br.com.utfpr.gerenciamento.server.model.Compra;
import br.com.utfpr.gerenciamento.server.service.CompraService;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import br.com.utfpr.gerenciamento.server.service.ItemService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("compra")
public class CompraController extends CrudController<Compra, Long, CompraResponseDTO> {

  private final CompraService compraService;
  private final ItemService itemService;
  private Compra compraOld;

  public CompraController(CompraService compraService, ItemService itemService) {
    this.compraService = compraService;
    this.itemService = itemService;
  }

  @Override
  protected CrudService<Compra, Long, CompraResponseDTO> getService() {
    return compraService;
  }

  @Override
  public void preSave(Compra object) {
    if (object.getId() != null) {
      // remove o saldo antigo do item
      compraOld = compraService.toEntity(compraService.findOne(object.getId()));
      compraOld.getCompraItem().stream()
          .forEach(
              compraItem ->
                  itemService.diminuiSaldoItem(
                      compraItem.getItem().getId(), compraItem.getQtde(), false));
    }
  }

  @Override
  public void postSave(Compra object) {
    // aumenta o novo saldo do item
    object.getCompraItem().stream()
        .forEach(
            compraItem ->
                itemService.aumentaSaldoItem(compraItem.getItem().getId(), compraItem.getQtde()));
  }

  @Override
  public void postDelete(Compra object) {
    object.getCompraItem().stream()
        .forEach(
            compraItem ->
                itemService.diminuiSaldoItem(
                    compraItem.getItem().getId(), compraItem.getQtde(), true));
  }
}
