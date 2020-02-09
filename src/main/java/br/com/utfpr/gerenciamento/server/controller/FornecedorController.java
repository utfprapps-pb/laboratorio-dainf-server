package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.model.Fornecedor;
import br.com.utfpr.gerenciamento.server.service.FornecedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("fornecedor")
public class FornecedorController {

    @Autowired
    private FornecedorService fornecedorService;

    @GetMapping
    public List<Fornecedor> findAll() {
        return fornecedorService.findAll();
    }

    @GetMapping("{id}")
    public Fornecedor findOne(@PathVariable("id") Long id) {
        return fornecedorService.findOne(id);
    }

    @PostMapping
    public Fornecedor save(@RequestBody Fornecedor fornecedor) {
        return fornecedorService.save(fornecedor);
    }
}
