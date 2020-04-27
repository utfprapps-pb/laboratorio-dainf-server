package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardEmprestimoDia;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensEmprestados;

import java.time.LocalDate;
import java.util.List;

public interface EmprestimoService extends CrudService<Emprestimo, Long> {

    List<Emprestimo> findAllByDataEmprestimoBetween(LocalDate dtIni, LocalDate dtFim);

    List<DashboardEmprestimoDia> countByDataEmprestimo(LocalDate dtIni, LocalDate dtFim);

    List<DashboardItensEmprestados> findItensMaisEmprestados(LocalDate dtIni, LocalDate dtFim);
}
