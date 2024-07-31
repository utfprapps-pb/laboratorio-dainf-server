package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.model.Permissao;
import br.com.utfpr.gerenciamento.server.repository.PermissaoRepository;
import br.com.utfpr.gerenciamento.server.service.PermissaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class PermissaoServiceImpl extends CrudServiceImpl<Permissao, Long> implements PermissaoService {

    @Autowired
    private PermissaoRepository permissaoRepository;

    @Override
    protected JpaRepository<Permissao, Long> getRepository() {
        return permissaoRepository;
    }


    @Override
    public Permissao findByNome(String nome) {
        return permissaoRepository.findByNome(nome);
    }
}
