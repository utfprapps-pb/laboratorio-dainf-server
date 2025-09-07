package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Estado;
<<<<<<< Updated upstream
<<<<<<< Updated upstream
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
=======
import java.util.List;

import br.com.utfpr.gerenciamento.server.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EstadoRepository extends JpaRepository<Estado, Long>, JpaSpecificationExecutor<Estado> {
>>>>>>> Stashed changes

public interface EstadoRepository extends JpaRepository<Estado, Long> {
=======
import java.util.List;

import br.com.utfpr.gerenciamento.server.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EstadoRepository extends JpaRepository<Estado, Long>, JpaSpecificationExecutor<Estado> {
>>>>>>> Stashed changes

    List<Estado> findByNomeLikeIgnoreCase(String query);
}
