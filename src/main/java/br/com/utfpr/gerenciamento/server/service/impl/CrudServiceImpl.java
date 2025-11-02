package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.dto.EmprestimoResponseDto;
import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public abstract class CrudServiceImpl<T, ID extends Serializable, DTO> implements CrudService<T, ID, DTO> {

  protected abstract JpaRepository<T, ID> getRepository();

  /**
   * 🔁 Métodos para conversão — devem ser implementados nas subclasses
   * (ex: usando ModelMapper, MapStruct, ou manualmente)
   */
  public abstract DTO toDto(T entity);
  public abstract T toEntity(DTO dto);

  @Override
  @Transactional(readOnly = true)
  public List<DTO> findAll() {
    return getRepository().findAll().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<DTO> findAll(Sort sort) {
    return getRepository().findAll(sort).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public Page<DTO> findAll(Pageable pageable) {
    Page<T> page = getRepository().findAll(pageable);
    List<DTO> dtoList = page.getContent().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    return new PageImpl<>(dtoList, pageable, page.getTotalElements());
  }

  @Override
  @Transactional
  public DTO save(T entity) {
    return toDto(getRepository().save(entity));
  }

  @Override
  @Transactional
  public DTO saveAndFlush(T entity) {
    return toDto(getRepository().saveAndFlush(entity));
  }

  @Override
  @Transactional
  public Iterable<DTO> save(Iterable<T> iterable) {
    List<T> entities = (List<T>) iterable;
    return getRepository().saveAll(entities)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void flush() {
    getRepository().flush();
  }

  @Override
  @Transactional(readOnly = true)
  public DTO findOne(ID id) {
    T entity = getRepository()
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Entidade não encontrada com ID: " + id));
    return toDto(entity);
  }

  @Override
  @Transactional(readOnly = true)
  public List<DTO> findAllById(Iterable<ID> ids) {
    return getRepository().findAllById(ids).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
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

      Predicate[] predicates = root.getModel().getDeclaredSingularAttributes().stream()
              .filter(attr -> {
                Class<?> javaType = attr.getJavaType();
                return javaType.equals(String.class) || Number.class.isAssignableFrom(javaType);
              })
              .map(attr -> {
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
  @Transactional(readOnly = true)
  public Page<DTO> findAllSpecification(Specification<T> specification, Pageable pageable) {
    var repo = (org.springframework.data.jpa.repository.JpaSpecificationExecutor<T>) getRepository();
    Page<T> page = repo.findAll(specification, pageable);
    List<DTO> dtoList = page.getContent().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    return new PageImpl<>(dtoList, pageable, page.getTotalElements());
  }

}
