package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.model.Entrada;
import br.com.utfpr.gerenciamento.server.repository.EntradaRepository;
import br.com.utfpr.gerenciamento.server.service.EntradaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class EntradaServiceImpl extends CrudServiceImpl<Entrada, Long> implements EntradaService {

    @Autowired
    private EntradaRepository entradaRepository;

    @Override
    protected JpaRepository<Entrada, Long> getRepository() {
        return entradaRepository;
    }
}
