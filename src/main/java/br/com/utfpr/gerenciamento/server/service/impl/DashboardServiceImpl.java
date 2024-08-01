package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.model.dashboards.*;
import br.com.utfpr.gerenciamento.server.service.CompraService;
import br.com.utfpr.gerenciamento.server.service.DashboardService;
import br.com.utfpr.gerenciamento.server.service.EmprestimoService;
import br.com.utfpr.gerenciamento.server.service.SaidaService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final EmprestimoService emprestimoService;
    private final CompraService compraService;
    private final SaidaService saidaService;

    public DashboardServiceImpl(EmprestimoService emprestimoService, CompraService compraService, SaidaService saidaService) {
        this.emprestimoService = emprestimoService;
        this.compraService = compraService;
        this.saidaService = saidaService;
    }

    @Override
    public DashboardEmprestimoCountRange findDadosEmprestimoCountRange(LocalDate dtIni, LocalDate dtFim) {
        List<Emprestimo> emprestimoList = emprestimoService.findAllByDataEmprestimoBetween(dtIni, dtFim);

        DashboardEmprestimoCountRange toReturn = new DashboardEmprestimoCountRange();
        toReturn.setTotal(emprestimoList.size());
        toReturn.setEmAtraso((int) emprestimoList
                .stream()
                .filter(emprestimo -> emprestimo.getPrazoDevolucao().isBefore(LocalDate.now())
                        && emprestimo.getDataDevolucao() == null)
                .count());
        toReturn.setEmAndamento((int) emprestimoList.stream()
                .filter(emprestimo -> emprestimo.getDataDevolucao() == null
                        && (emprestimo.getPrazoDevolucao().isEqual(LocalDate.now())
                        || emprestimo.getPrazoDevolucao().isAfter(LocalDate.now())))
                .count());
        toReturn.setFinalizado((int) emprestimoList.stream().filter(emprestimo -> emprestimo.getDataDevolucao() != null).count());
        return toReturn;
    }

    @Override
    public List<DashboardEmprestimoDia> findTotalEmprestimoByDia(LocalDate dtIni, LocalDate dtFim) {
        return emprestimoService.countByDataEmprestimo(dtIni, dtFim);
    }

    @Override
    public List<DashboardItensEmprestados> findItensMaisEmprestados(LocalDate dtIni, LocalDate dtFim) {
        return emprestimoService.findItensMaisEmprestados(dtIni, dtFim);
    }

    @Override
    public List<DashboardItensAdquiridos> findItensMaisAdquiridos(LocalDate dtIni, LocalDate dtFim) {
        return compraService.findItensMaisAdquiridos(dtIni, dtFim);
    }

    @Override
    public List<DashboardItensSaidas> findItensComMaisSaidas(LocalDate dtIni, LocalDate dtFim) {
        return saidaService.findItensMaisSaidas(dtIni, dtFim);
    }
}
