package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.exception.EntityNotFoundException;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import jakarta.persistence.criteria.Predicate;
import java.io.Serializable;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public abstract class CrudServiceImpl<T, ID extends Serializable> implements CrudService<T, ID> {

  protected abstract JpaRepository<T, ID> getRepository();

  @Override
  @Transactional(readOnly = true)
  public List<T> findAll() {
    return getRepository().findAll();
  }

  @Override
  @Transactional(readOnly = true)
  public List<T> findAll(Sort sort) {
    return getRepository().findAll(sort);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<T> findAll(Pageable pageable) {
    return getRepository().findAll(pageable);
  }

  @Override
  @Transactional // (readOnly = false)
  public T save(T entity) {
    return getRepository().save(entity);
  }

  @Override
  @Transactional
  public T saveAndFlush(T entity) {
    return getRepository().saveAndFlush(entity);
  }

  @Override
  @Transactional
  public Iterable<T> save(Iterable<T> iterable) {
    return getRepository().saveAll(iterable);
  }

  @Override
  @Transactional
  public void flush() {
    getRepository().flush();
  }

  @Override
  @Transactional(readOnly = true)
  public T findOne(ID id) {
    return getRepository()
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Entidade não encontrada com ID: " + id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<T> findAllById(Iterable<ID> ids) {
    return getRepository().findAllById(ids);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean exists(ID id) {
    return getRepository().existsById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public long count() {
    return getRepository().count();
  }

  @Override
  @Transactional
  public void delete(ID id) {
    getRepository().deleteById(id);
  }

  @Override
  @Transactional
  public void delete(T entity) {
    getRepository().delete(entity);
  }

  @Override
  @Transactional
  public void delete(Iterable<T> iterable) {
    getRepository().deleteAll(iterable);
  }

  @Override
  @Transactional
  public void deleteAll() {
    getRepository().deleteAll();
  }

  @Override
  @Transactional
  public Specification<T> filterByAllFields(String filter) {
    return (root, query, cb) -> {
      if (filter == null || filter.trim().isEmpty()) {
        return cb.conjunction();
      }

      String likeFilter = "%" + filter.toLowerCase() + "%";

      Predicate[] predicates =
          root.getModel().getDeclaredSingularAttributes().stream()
              .filter(
                  attr -> {
                    Class<?> javaType = attr.getJavaType();
                    return javaType.equals(String.class) || Number.class.isAssignableFrom(javaType);
                  })
              .map(
                  attr -> {
                    if (attr.getJavaType().equals(String.class)) {
                      return cb.like(cb.lower(root.get(attr.getName())), likeFilter);
                    } else {
                      return cb.like(cb.toString(root.get(attr.getName())), likeFilter);
                    }
                  })
              .toArray(Predicate[]::new);

      return cb.or(predicates);
    };
  }

  @Override
  @Transactional
  public Page<T> findAllSpecification(Specification<T> specification, Pageable pageable) {
    return ((org.springframework.data.jpa.repository.JpaSpecificationExecutor<T>) getRepository())
        .findAll(specification, pageable);
  }
}
