package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.model.Fornecedor;
import br.com.utfpr.gerenciamento.server.repository.FornecedorRepository;
import br.com.utfpr.gerenciamento.server.service.FornecedorService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FornecedorServiceImpl extends CrudServiceImpl<Fornecedor, Long> implements FornecedorService {

    private final FornecedorRepository fornecedorRepository;

    public FornecedorServiceImpl(FornecedorRepository fornecedorRepository) {
        this.fornecedorRepository = fornecedorRepository;
    }

    @Override
    protected JpaRepository<Fornecedor, Long> getRepository() {
        return fornecedorRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Fornecedor> completeFornecedor(String query) {
        if ("".equalsIgnoreCase(query)) {
            return fornecedorRepository.findAll();
        } else {
            return fornecedorRepository.findByNomeFantasiaLikeIgnoreCase("%" + query + "%");
        }
    }
}
