package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Fornecedor;
<<<<<<< Updated upstream
<<<<<<< Updated upstream
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
=======
import java.util.List;

import br.com.utfpr.gerenciamento.server.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FornecedorRepository extends JpaRepository<Fornecedor, Long>, JpaSpecificationExecutor<Fornecedor> {
>>>>>>> Stashed changes

public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {
=======
import java.util.List;

import br.com.utfpr.gerenciamento.server.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FornecedorRepository extends JpaRepository<Fornecedor, Long>, JpaSpecificationExecutor<Fornecedor> {
>>>>>>> Stashed changes

    List<Fornecedor> findByNomeFantasiaLikeIgnoreCase (String query);
}
