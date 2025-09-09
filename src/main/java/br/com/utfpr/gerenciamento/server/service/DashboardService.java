package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.dto.dashboards.*;
import br.com.utfpr.gerenciamento.server.model.dashboards.*;
import java.time.LocalDate;
import java.util.List;

public interface DashboardService {

  DashboardEmprestimoCountRangeResponseDto findDadosEmprestimoCountRange(LocalDate dtIni, LocalDate dtFim);

  List<DashboardEmprestimoDiaResponseDto> findTotalEmprestimoByDia(LocalDate dtIni, LocalDate dtFim);

  List<DashboardItensEmprestadosResponseDto> findItensMaisEmprestados(LocalDate dtIni, LocalDate dtFim);

  List<DashboardItensAdquiridosResponseDto> findItensMaisAdquiridos(LocalDate dtIni, LocalDate dtFim);

  List<DashboardItensSaidasResponseDto> findItensComMaisSaidas(LocalDate dtIni, LocalDate dtFim);

  <D, E> D convertToDto(E entity, Class<D> dtoClass);
}
