package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.model.Fornecedor;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import br.com.utfpr.gerenciamento.server.service.FornecedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("fornecedor")
public class FornecedorController extends CrudController<Fornecedor, Long> {

    @Autowired
    private FornecedorService fornecedorService;

    @Override
    protected CrudService<Fornecedor, Long> getService() {
        return fornecedorService;
    }

    @GetMapping("/complete")
    public List<Fornecedor> complete(@RequestParam("query") String query) {
        return fornecedorService.completeFornecedor(query);
    }
}
