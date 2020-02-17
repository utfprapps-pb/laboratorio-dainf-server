package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.model.Fornecedor;
import br.com.utfpr.gerenciamento.server.repository.FornecedorRepository;
import br.com.utfpr.gerenciamento.server.service.FornecedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FornecedorServiceImpl extends CrudServiceImpl<Fornecedor, Long> implements FornecedorService {

    @Autowired
    private FornecedorRepository fornecedorRepository;

    @Override
    protected JpaRepository<Fornecedor, Long> getRepository() {
        return fornecedorRepository;
    }

    @Override
    public List<Fornecedor> completeFornecedor(String query) {
        if ("".equalsIgnoreCase(query)) {
            return fornecedorRepository.findAll();
        } else {
            return fornecedorRepository.findByNomeFantasiaLikeIgnoreCase("%" + query + "%");
        }
    }
}
