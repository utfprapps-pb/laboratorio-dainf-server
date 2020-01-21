package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.repository.EmprestimoRepository;
import br.com.utfpr.gerenciamento.server.service.EmprestimoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class EmprestimoServiceImpl extends CrudServiceImpl<Emprestimo, Long> implements EmprestimoService {

    @Autowired
    private EmprestimoRepository emprestimoRepository;

    @Override
    protected JpaRepository<Emprestimo, Long> getRepository() {
        return emprestimoRepository;
    }
}
