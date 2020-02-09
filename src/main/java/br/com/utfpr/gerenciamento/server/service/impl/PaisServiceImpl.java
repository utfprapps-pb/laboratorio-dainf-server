package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.model.Pais;
import br.com.utfpr.gerenciamento.server.repository.PaisRepository;
import br.com.utfpr.gerenciamento.server.service.PaisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaisServiceImpl extends CrudServiceImpl<Pais, Long> implements PaisService {

    @Autowired
    private PaisRepository paisRepository;

    @Override
    protected JpaRepository<Pais, Long> getRepository() {
        return this.paisRepository;
    }

    @Override
    public List<Pais> paisComplete(String query) {
        if ("".equalsIgnoreCase(query)) {
            return this.paisRepository.findAll();
        } else {
            return this.paisRepository.findByNomeLikeIgnoreCase("%" + query + "%");
        }
    }
}
