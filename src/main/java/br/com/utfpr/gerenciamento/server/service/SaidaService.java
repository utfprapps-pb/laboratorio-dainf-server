package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.model.EmprestimoDevolucaoItem;
import br.com.utfpr.gerenciamento.server.model.Saida;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensSaidas;

import java.time.LocalDate;
import java.util.List;

public interface SaidaService extends CrudService<Saida, Long> {

    List<DashboardItensSaidas> findItensMaisSaidas(LocalDate dtIni, LocalDate dtFim);

    void createSaidaByDevolucaoEmprestimo(List<EmprestimoDevolucaoItem> emprestimoDevolucaoItem);

    void deleteSaidaByEmprestimo(Long idEmprestimo);
}
