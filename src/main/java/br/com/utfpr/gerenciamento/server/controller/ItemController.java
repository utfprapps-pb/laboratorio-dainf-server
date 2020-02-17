package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("item")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping
    public List<Item> findAll() {
        return itemService.findAll();
    }

    @GetMapping("{id}")
    public Item findOne(@PathVariable("id") Long id) {
        return itemService.findOne(id);
    }

    @PostMapping
    public Item save(@RequestBody Item item) {
        return itemService.save(item);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable("id") Long id) {
        itemService.delete(id);
    }
}
