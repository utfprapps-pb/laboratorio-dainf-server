package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.model.ItemImage;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import br.com.utfpr.gerenciamento.server.service.ItemService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("item")
public class ItemController extends CrudController<Item, Long> {

    private final ItemService itemService;
    private List<ItemImage> imagesToCopy;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @Override
    protected CrudService<Item, Long> getService() {
        return itemService;
    }

    @Override
    public void preSave(Item object) {
        if (object.getId() == null && object.getImageItem() != null && !object.getImageItem().isEmpty()) {
            this.imagesToCopy = object.getImageItem();
            object.setImageItem(null);
        }
    }

    @Override
    public void postSave(Item object) {
        if (this.imagesToCopy != null) {
            itemService.copyImagesItem(this.imagesToCopy, object.getId());
        }
        this.imagesToCopy = null;
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
        if (images.getFile("anexos[]") != null) {
            itemService.saveImages(images, request, idItem);
        }
    }

    @GetMapping("imagens/{idItem}")
    public List<ItemImage> findAllImagesByItem(@PathVariable("idItem") Long idItem) {
        return itemService.getImagesItem(idItem);
    }

    @PostMapping("delete-image/{idItem}")
    public void deleteImageItem(@PathVariable("idItem") Long idItem,
                                @RequestBody ItemImage itemImage) {
        itemService.deleteImage(itemImage, idItem);
    }

    @Override
    public Page<Item> findAllPaged(int page, int size, String order, Boolean asc) {
        return super.findAllPaged(page, size, order, asc);
    }

}
