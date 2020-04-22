package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.model.Compra;
import br.com.utfpr.gerenciamento.server.service.CompraService;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import br.com.utfpr.gerenciamento.server.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("compra")
public class CompraController extends CrudController<Compra, Long> {

    @Autowired
    private CompraService compraService;
    @Autowired
    private ItemService itemService;

    @Override
    protected CrudService<Compra, Long> getService() {
        return compraService;
    }

    @Override
    public void postSave(Compra object) {
        object.getCompraItem().stream().forEach(compraItem ->
                itemService.aumentaSaldoItem(compraItem.getItem().getId(), compraItem.getQtde())
        );
    }

    @Override
    public void postDelete(Compra object) {
        object.getCompraItem().stream().forEach(compraItem ->
                itemService.diminuiSaldoItem(compraItem.getItem().getId(), compraItem.getQtde())
        );
    }
}
