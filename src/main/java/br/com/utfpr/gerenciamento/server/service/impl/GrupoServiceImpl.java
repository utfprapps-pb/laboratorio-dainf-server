package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.model.Grupo;
import br.com.utfpr.gerenciamento.server.repository.GrupoRepository;
import br.com.utfpr.gerenciamento.server.service.GrupoService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GrupoServiceImpl extends CrudServiceImpl<Grupo, Long> implements GrupoService {

    private final GrupoRepository grupoRepository;

    public GrupoServiceImpl(GrupoRepository grupoRepository) {
        this.grupoRepository = grupoRepository;
    }

    @Override
    protected JpaRepository<Grupo, Long> getRepository() {
        return grupoRepository;
    }

    @Override
    public List<Grupo> completeGrupo(String query) {
        if ("".equalsIgnoreCase(query)) {
            return grupoRepository.findAll();
        } else {
            return grupoRepository.findByDescricaoLikeIgnoreCase("%" + query + "%");
        }
    }
}
