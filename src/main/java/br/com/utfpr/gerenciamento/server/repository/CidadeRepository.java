package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Cidade;
import br.com.utfpr.gerenciamento.server.model.Estado;
<<<<<<< Updated upstream
<<<<<<< Updated upstream
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CidadeRepository extends JpaRepository<Cidade, Long> {
=======
import java.util.List;
=======
import java.util.List;

import br.com.utfpr.gerenciamento.server.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CidadeRepository extends JpaRepository<Cidade, Long>, JpaSpecificationExecutor<Cidade> {
>>>>>>> Stashed changes

import br.com.utfpr.gerenciamento.server.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CidadeRepository extends JpaRepository<Cidade, Long>, JpaSpecificationExecutor<Cidade> {
>>>>>>> Stashed changes

    List<Cidade> findByNomeLikeIgnoreCase(String query);

    List<Cidade> findAllByEstado(Estado estado);

    List<Cidade> findByNomeLikeIgnoreCaseAndEstado(String query, Estado estado);
}
