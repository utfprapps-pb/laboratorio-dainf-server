package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.model.Compra;
import br.com.utfpr.gerenciamento.server.service.CompraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("compra")
public class CompraController {

    @Autowired
    private CompraService compraService;

    @GetMapping
    public List<Compra> findAll() {
        return compraService.findAll();
    }

    @GetMapping("{id}")
    public Compra findOne(@PathVariable("id") Long id) {
        return compraService.findOne(id);
    }

    @PostMapping
    public Compra save(@RequestBody Compra compra) {
        return compraService.save(compra);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable("id") Long id) {
        compraService.delete(id);
    }
}
