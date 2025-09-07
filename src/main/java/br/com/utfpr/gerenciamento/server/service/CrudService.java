package br.com.utfpr.gerenciamento.server.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
<<<<<<< Updated upstream

<<<<<<< Updated upstream
import java.io.Serializable;
import java.util.List;

public interface CrudService<T, ID extends Serializable> {
=======
public interface CrudService<T, ID extends Serializable>  {
>>>>>>> Stashed changes
=======

public interface CrudService<T, ID extends Serializable>  {
>>>>>>> Stashed changes

    List<T> findAll();

    List<T> findAll(Sort sort);

    Page<T> findAll(Pageable pageable);

<<<<<<< Updated upstream
    T save(T entity);
=======
  Page<T> findAllSpecification(Specification<T> specification,Pageable pageable);

  Page<T> findAllSpecification(Specification<T> specification,Pageable pageable);

  T save(T entity);
>>>>>>> Stashed changes

    T saveAndFlush(T entity);

    Iterable<T> save(Iterable<T> iterable);

    void flush();

    T findOne(ID id);

    boolean exists(ID id);

    long count();

    void delete(ID id);

    void delete(T entity);

    void delete(Iterable<T> iterable);

    void deleteAll();

<<<<<<< Updated upstream
=======
  Specification<T> filterByAllFields(String filter);

  Specification<T> filterByAllFields(String filter);

  void deleteAll();
>>>>>>> Stashed changes
}
