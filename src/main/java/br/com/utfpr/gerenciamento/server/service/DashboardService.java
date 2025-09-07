package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.model.dashboards.*;
import java.time.LocalDate;
import java.util.List;

public interface DashboardService {

  DashboardEmprestimoCountRange findDadosEmprestimoCountRange(LocalDate dtIni, LocalDate dtFim);

  List<DashboardEmprestimoDia> findTotalEmprestimoByDia(LocalDate dtIni, LocalDate dtFim);

  List<DashboardItensEmprestados> findItensMaisEmprestados(LocalDate dtIni, LocalDate dtFim);

  List<DashboardItensAdquiridos> findItensMaisAdquiridos(LocalDate dtIni, LocalDate dtFim);

  List<DashboardItensSaidas> findItensComMaisSaidas(LocalDate dtIni, LocalDate dtFim);
}
