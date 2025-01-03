package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.model.Pais;
import br.com.utfpr.gerenciamento.server.repository.PaisRepository;
import br.com.utfpr.gerenciamento.server.service.PaisService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaisServiceImpl extends CrudServiceImpl<Pais, Long> implements PaisService {

    private final PaisRepository paisRepository;

    public PaisServiceImpl(PaisRepository paisRepository) {
        this.paisRepository = paisRepository;
    }

    @Override
    protected JpaRepository<Pais, Long> getRepository() {
        return this.paisRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pais> paisComplete(String query) {
        if ("".equalsIgnoreCase(query)) {
            return this.paisRepository.findAll();
        } else {
            return this.paisRepository.findByNomeLikeIgnoreCase("%" + query + "%");
        }
    }
}
