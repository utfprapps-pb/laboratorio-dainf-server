package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardEmprestimoDia;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensEmprestados;
import br.com.utfpr.gerenciamento.server.repository.EmprestimoRepository;
import br.com.utfpr.gerenciamento.server.service.EmprestimoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EmprestimoServiceImpl extends CrudServiceImpl<Emprestimo, Long> implements EmprestimoService {

    @Autowired
    private EmprestimoRepository emprestimoRepository;

    @Override
    protected JpaRepository<Emprestimo, Long> getRepository() {
        return emprestimoRepository;
    }

    @Override
    public List<Emprestimo> findAllByDataEmprestimoBetween(LocalDate dtIni, LocalDate dtFim) {
        return emprestimoRepository.findAllByDataEmprestimoBetween(dtIni, dtFim);
    }

    @Override
    public List<DashboardEmprestimoDia> countByDataEmprestimo(LocalDate dtIni, LocalDate dtFim) {
        return emprestimoRepository.countByDataEmprestimo(dtIni, dtFim);
    }

    @Override
    public List<DashboardItensEmprestados> findItensMaisEmprestados(LocalDate dtIni, LocalDate dtFim) {
        return emprestimoRepository.findItensMaisEmprestados(dtIni, dtFim);
    }
}
