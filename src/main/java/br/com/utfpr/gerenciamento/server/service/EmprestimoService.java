package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.model.EmprestimoDevolucaoItem;
import br.com.utfpr.gerenciamento.server.model.EmprestimoItem;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardEmprestimoDia;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensEmprestados;
import br.com.utfpr.gerenciamento.server.model.filter.EmprestimoFilter;
import java.time.LocalDate;
import java.util.List;

public interface EmprestimoService extends CrudService<Emprestimo, Long> {

  List<Emprestimo> findAllByDataEmprestimoBetween(LocalDate dtIni, LocalDate dtFim);

  List<DashboardEmprestimoDia> countByDataEmprestimo(LocalDate dtIni, LocalDate dtFim);

  List<DashboardItensEmprestados> findItensMaisEmprestados(LocalDate dtIni, LocalDate dtFim);

  List<EmprestimoDevolucaoItem> createEmprestimoItemDevolucao(List<EmprestimoItem> emprestimoItem);

  List<Emprestimo> filter(EmprestimoFilter emprestimoFilter);

  List<Emprestimo> findAllUsuarioEmprestimo(String username);

  List<Emprestimo> findAllEmprestimosAbertos();

  void changePrazoDevolucao(Long idEmprestimo, LocalDate novaData);

  void sendEmailConfirmacaoEmprestimo(Emprestimo emprestimo);

  void sendEmailConfirmacaoDevolucao(Emprestimo emprestimo);

  void sendEmailPrazoDevolucaoProximo();
}
