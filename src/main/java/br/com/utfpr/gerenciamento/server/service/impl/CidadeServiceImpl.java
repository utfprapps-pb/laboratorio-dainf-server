package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.model.Cidade;
import br.com.utfpr.gerenciamento.server.model.Estado;
import br.com.utfpr.gerenciamento.server.repository.CidadeRepository;
import br.com.utfpr.gerenciamento.server.service.CidadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CidadeServiceImpl extends CrudServiceImpl<Cidade, Long> implements CidadeService {

    @Autowired
    private CidadeRepository cidadeRepository;

    @Override
    protected JpaRepository<Cidade, Long> getRepository() {
        return cidadeRepository;
    }

    @Override
    public List<Cidade> cidadeComplete(String query) {
        if ("".equalsIgnoreCase(query)) {
            return this.cidadeRepository.findAll();
        } else {
            return this.cidadeRepository.findByNomeLikeIgnoreCase("%" + query + "%");
        }
    }

    @Override
    public List<Cidade> completeByEstado(String query, Estado estado) {
        if ("".equalsIgnoreCase(query)) {
            return this.cidadeRepository.findAllByEstado(estado);
        } else {
            return this.cidadeRepository.findByNomeLikeIgnoreCaseAndEstado("%" + query + "%", estado);
        }
    }
}
