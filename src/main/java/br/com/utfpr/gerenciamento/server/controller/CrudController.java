package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.service.CrudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;

public abstract class CrudController<T, ID extends Serializable> {

    protected abstract CrudService<T, ID> getService();

<<<<<<< Updated upstream
    @GetMapping
    public List<T> findAll() {
        return getService().findAll(Sort.by("id"));
    }

    @PostMapping
    public T save(@RequestBody T object) {
        preSave(object);
        T toReturn = getService().save(object);
        postSave(object);
        return toReturn;
    }

    public void preSave(T object) {
    }

    public void postSave(T object) {
    }

    @GetMapping("{id}")
    public T findone(@PathVariable("id") ID id) {
        return getService().findOne(id);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable("id") ID id) {
        T object = getService().findOne(id);
        getService().delete(id);
        postDelete(object);
    }

    public void postDelete(T object) {
    }

    @GetMapping("exists/{id}")
    public boolean exists(@PathVariable ID id) {
        return getService().exists(id);
    }

    @GetMapping("count")
    public long count() {
        return getService().count();
    }

    @GetMapping("page")
    public Page<T> findAllPaged(@RequestParam("page") int page,
                                @RequestParam("size") int size,
                                @RequestParam(required = false) String order,
                                @RequestParam(required = false) Boolean asc) {
        PageRequest pageRequest = PageRequest.of(page, size);
        if (order != null && asc != null) {
            pageRequest = PageRequest.of(page, size, asc ? Sort.Direction.ASC : Sort.Direction.DESC, order);
        }
        return getService().findAll(pageRequest);
    }
=======
  @GetMapping()
  public List<T> findAll() {
    return getService().findAll(Sort.by("id"));
  }

  @PostMapping
  public T save(@RequestBody T object) {
    preSave(object);
    T toReturn = getService().save(object);
    postSave(object);
    return toReturn;
  }

  public void preSave(T object) {}

  public void postSave(T object) {}

  @GetMapping("{id}")
  public T findone(@PathVariable("id") ID id) {
    return getService().findOne(id);
  }

  @DeleteMapping("{id}")
  public void delete(@PathVariable("id") ID id) {
    T object = getService().findOne(id);
    getService().delete(id);
    postDelete(object);
  }

  public void postDelete(T object) {}

  @GetMapping("exists/{id}")
  public boolean exists(@PathVariable ID id) {
    return getService().exists(id);
  }

  @GetMapping("count")
  public long count() {
    return getService().count();
  }

  @GetMapping("page")
  public Page<T> findAllPaged(
      @RequestParam("page") int page,
      @RequestParam("size") int size,
      @RequestParam(required = false) String filter,
      @RequestParam(required = false) String order,
      @RequestParam(required = false) Boolean asc) {
    PageRequest pageRequest = PageRequest.of(page, size);
    if (order != null && asc != null) {
      pageRequest =
          PageRequest.of(page, size, asc ? Sort.Direction.ASC : Sort.Direction.DESC, order);
    }
    if(filter != null && !filter.isEmpty()) {
      Specification<T> spec = getService().filterByAllFields(filter);
      return getService().findAllSpecification(spec, pageRequest);
    }
    else
      return getService().findAll(pageRequest);
  }
>>>>>>> Stashed changes
}

