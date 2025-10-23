package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.dto.ItemResponseDto;
import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.model.ItemImage;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import br.com.utfpr.gerenciamento.server.service.ItemService;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

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
    if (object.getId() == null
        && object.getImageItem() != null
        && !object.getImageItem().isEmpty()) {
      this.imagesToCopy = object.getImageItem();
      object.setImageItem(null);
    }
  }

  @Override
  @GetMapping("{id}")
  public Item findone(@PathVariable("id") Long id) {
    Item item = itemService.findOne(id);
    if (item.getTipoItem().name().equals("P")) {
      BigDecimal disponivel = item.getDisponivelEmprestimoCalculado();
      BigDecimal saldo = item.getSaldo();

      // Evita NullPointerException
      if (saldo == null) saldo = BigDecimal.ZERO;
      if (disponivel == null) disponivel = BigDecimal.ZERO;
      item.setDisponivelEmprestimoCalculado(saldo.subtract(disponivel));
    }if(item.getTipoItem().name().equals("C")){
      item.setDisponivelEmprestimoCalculado(item.getSaldo());
    }
    return item;
  }

  @Override
  @GetMapping
  public List<Item> findAll() {
    return getService().findAll(Sort.by("id")).stream()
        .peek(
            item -> {
              if (item.getTipoItem() != null && "P".equals(item.getTipoItem().name())) {
                BigDecimal disponivel =  item.getDisponivelEmprestimoCalculado();
                BigDecimal saldo = item.getSaldo();

                if (saldo == null) saldo = BigDecimal.ZERO;
                if (disponivel == null) disponivel = BigDecimal.ZERO;

                item.setDisponivelEmprestimoCalculado(saldo.subtract(disponivel));
              } else {
                item.setDisponivelEmprestimoCalculado(item.getSaldo());
              }
            })
        .toList();
  }

  @Override
  @GetMapping("page")
  public Page<Item> findAllPaged(
          @RequestParam("page") int page,
          @RequestParam("size") int size,
          @RequestParam(required = false) String filter,
          @RequestParam(required = false) String order,
          @RequestParam(required = false) Boolean asc) {

    PageRequest pageRequest = PageRequest.of(page, size);
    if (order != null && asc != null) {
      pageRequest = PageRequest.of(page, size, asc ? Sort.Direction.ASC : Sort.Direction.DESC, order);
    }

    Page<Item> pageResult;

    if (filter != null && !filter.isEmpty()) {
      Specification<Item> spec = getService().filterByAllFields(filter);
      pageResult = getService().findAllSpecification(spec, pageRequest);
    } else {
      pageResult = getService().findAll(pageRequest);
    }

    // Aplica o cálculo nos itens do resultado
    pageResult.forEach(item -> {
      if (item.getTipoItem() != null && "P".equals(item.getTipoItem().name())) {
        BigDecimal disponivel =  item.getDisponivelEmprestimoCalculado();
        BigDecimal saldo = item.getSaldo();

        if (saldo == null) saldo = BigDecimal.ZERO;
        if (disponivel == null) disponivel = BigDecimal.ZERO;

        item.setDisponivelEmprestimoCalculado(saldo.subtract(disponivel));
      }else{
        item.setDisponivelEmprestimoCalculado(item.getSaldo());
      }
    });
    return pageResult;
  }

  @Override
  public void postSave(Item object) {
    if (this.imagesToCopy != null) {
      itemService.copyImagesItem(this.imagesToCopy, object.getId());
    }
    this.imagesToCopy = null;
  }

  @GetMapping("/complete")
  public List<ItemResponseDto> complete(
      @RequestParam("query") String query, @RequestParam("hasEstoque") Boolean hasEstoque) {
    return itemService.itemComplete(query, hasEstoque);
  }

  @PostMapping("upload-images")
  public void upload(
      @RequestParam("idItem") Long idItem,
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
  public void deleteImageItem(
      @PathVariable("idItem") Long idItem, @RequestBody ItemImage itemImage) {
    itemService.deleteImage(itemImage, idItem);
  }
}
