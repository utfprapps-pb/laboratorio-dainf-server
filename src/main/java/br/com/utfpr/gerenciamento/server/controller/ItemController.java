package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import br.com.utfpr.gerenciamento.server.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
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

    @PostMapping("upload-images")
    public void upload(@RequestParam("idItem") Long idItem,
                       MultipartHttpServletRequest images,
                       HttpServletRequest request) {
        itemService.saveImages(images, request, idItem);
    }
}
