package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.model.Permissao;
import br.com.utfpr.gerenciamento.server.repository.PermissaoRepository;
import br.com.utfpr.gerenciamento.server.service.PermissaoService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PermissaoServiceImpl extends CrudServiceImpl<Permissao, Long> implements PermissaoService {

    private final PermissaoRepository permissaoRepository;

    public PermissaoServiceImpl(PermissaoRepository permissaoRepository) {
        this.permissaoRepository = permissaoRepository;
    }

    @Override
    protected JpaRepository<Permissao, Long> getRepository() {
        return permissaoRepository;
    }


    @Override
    @Transactional(readOnly = true)
    public Permissao findByNome(String nome) {
        return permissaoRepository.findByNome(nome);
    }
}
