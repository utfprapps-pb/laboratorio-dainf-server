package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import br.com.utfpr.gerenciamento.server.service.EmprestimoService;
import br.com.utfpr.gerenciamento.server.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("emprestimo")
public class EmprestimoController extends CrudController<Emprestimo, Long> {

    @Autowired
    private EmprestimoService emprestimoService;
    @Autowired
    private ItemService itemService;

    @Override
    protected CrudService<Emprestimo, Long> getService() {
        return emprestimoService;
    }

    @Override
    public void preSave(Emprestimo object) {
        object.getEmprestimoItem().stream().forEach(saidaItem -> {
            if (saidaItem.getItem() != null) {
                itemService.saldoItemIsValid(
                        itemService.getSaldoItem(saidaItem.getItem().getId()), saidaItem.getQtde()
                );
            }
        });
    }

    @Override
    public void postSave(Emprestimo object) {
        object.getEmprestimoItem().stream().forEach(saidaItem ->
                itemService.diminuiSaldoItem(saidaItem.getItem().getId(), saidaItem.getQtde())
        );
    }

    @Override
    public void postDelete(Emprestimo object) {
        object.getEmprestimoItem().stream().forEach(saidaItem ->
                itemService.aumentaSaldoItem(saidaItem.getItem().getId(), saidaItem.getQtde())
        );
    }
}
