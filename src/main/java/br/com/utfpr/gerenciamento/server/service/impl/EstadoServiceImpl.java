package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.model.Estado;
import br.com.utfpr.gerenciamento.server.repository.EstadoRepository;
import br.com.utfpr.gerenciamento.server.service.EstadoService;
 import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EstadoServiceImpl extends CrudServiceImpl<Estado, Long> implements EstadoService {

    private final EstadoRepository estadoRepository;

    public EstadoServiceImpl(EstadoRepository estadoRepository) {
        this.estadoRepository = estadoRepository;
    }

    @Override
    protected JpaRepository<Estado, Long> getRepository() {
        return this.estadoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Estado> estadoComplete(String query) {
        if ("".equalsIgnoreCase(query)) {
            return estadoRepository.findAll();
        } else {
            return estadoRepository.findByNomeLikeIgnoreCase("%" + query + "%");
        }
    }
}
