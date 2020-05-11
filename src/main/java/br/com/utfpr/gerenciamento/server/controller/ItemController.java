package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import br.com.utfpr.gerenciamento.server.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("item")
public class ItemController extends CrudController<Item, Long> {

    @Autowired
    private ItemService itemService;

    @Override
    protected CrudService<Item, Long> getService() {
        return itemService;
    }

    @GetMapping("/complete")
    public List<Item> complete(@RequestParam("query") String query,
                               @RequestParam("hasEstoque") Boolean hasEstoque) {
        return itemService.itemComplete(query, hasEstoque);
    }
}
