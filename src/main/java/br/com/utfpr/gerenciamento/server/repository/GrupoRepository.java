package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Grupo;
<<<<<<< Updated upstream
<<<<<<< Updated upstream
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
=======
import java.util.List;

import br.com.utfpr.gerenciamento.server.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GrupoRepository extends JpaRepository<Grupo, Long> , JpaSpecificationExecutor<Grupo> {
>>>>>>> Stashed changes

public interface GrupoRepository extends JpaRepository<Grupo, Long> {
=======
import java.util.List;

import br.com.utfpr.gerenciamento.server.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GrupoRepository extends JpaRepository<Grupo, Long> , JpaSpecificationExecutor<Grupo> {
>>>>>>> Stashed changes

    List<Grupo> findByDescricaoLikeIgnoreCase(String query);
}
