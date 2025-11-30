package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.dto.CompraResponseDTO;
import br.com.utfpr.gerenciamento.server.model.Compra;
import br.com.utfpr.gerenciamento.server.service.CompraService;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import br.com.utfpr.gerenciamento.server.service.ItemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("compra")
public class CompraController extends CrudController<Compra, Long, CompraResponseDTO> {

  private final CompraService compraService;
  private final ItemService itemService;

  public CompraController(CompraService compraService, ItemService itemService) {
    this.compraService = compraService;
    this.itemService = itemService;
  }

  @Override
  protected CrudService<Compra, Long, CompraResponseDTO> getService() {
    return compraService;
  }

  /**
   * Lista paginada de compras com filtro textual usando DTO simplificado.
   *
   * <p><b>Otimização:</b> Retorna apenas campos necessários para listagem via projeção SQL.
   *
   * @param page Número da página (0-indexed)
   * @param size Tamanho da página
   * @param filter Filtro opcional (busca textual em todos os campos)
   * @param order Campo de ordenação (padrão: "id")
   * @param asc Direção da ordenação (true = ASC, false = DESC, padrão: ASC)
   * @return Página de compras simplificadas
   */
  @Override
  @GetMapping("page")
  @SuppressWarnings("unchecked")
  public Page<CompraResponseDTO> findAllPaged(
      @RequestParam("page") int page,
      @RequestParam("size") int size,
      @RequestParam(required = false) String filter,
      @RequestParam(required = false) String order,
      @RequestParam(required = false) Boolean asc) {
    PageRequest pageRequest = PageRequest.of(page, size);
    if (order != null && asc != null) {
      pageRequest =
          PageRequest.of(page, size, asc ? Sort.Direction.ASC : Sort.Direction.DESC, order);
    }
    return (Page<CompraResponseDTO>) (Page<?>) compraService.findAllPagedList(filter, pageRequest);
  }

  @Override
  public void preSave(Compra object) {
    if (object.getId() != null) {
      // remove o saldo antigo do item
      Compra compraOld = compraService.toEntity(compraService.findOne(object.getId()));
      if (compraOld.getCompraItem() != null) {
        compraOld.getCompraItem().stream()
            .forEach(
                compraItem ->
                    itemService.diminuiSaldoItem(
                        compraItem.getItem().getId(), compraItem.getQtde(), false));
      }
    }
  }

  @Override
  public void postSave(Compra object) {
    // aumenta o novo saldo do item
    if (object.getCompraItem() != null) {
      object.getCompraItem().stream()
          .forEach(
              compraItem ->
                  itemService.aumentaSaldoItem(compraItem.getItem().getId(), compraItem.getQtde()));
    }
  }

  @Override
  public void postDelete(Compra object) {
    if (object.getCompraItem() != null) {
      object.getCompraItem().stream()
          .forEach(
              compraItem ->
                  itemService.diminuiSaldoItem(
                      compraItem.getItem().getId(), compraItem.getQtde(), true));
    }
  }
}
