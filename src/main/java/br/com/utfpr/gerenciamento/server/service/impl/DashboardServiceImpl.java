package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.dto.dashboards.*;
import br.com.utfpr.gerenciamento.server.repository.EmprestimoRepository;
import br.com.utfpr.gerenciamento.server.service.CompraService;
import br.com.utfpr.gerenciamento.server.service.DashboardService;
import br.com.utfpr.gerenciamento.server.service.EmprestimoService;
import br.com.utfpr.gerenciamento.server.service.SaidaService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardServiceImpl implements DashboardService {

  private final EmprestimoService emprestimoService;
  private final EmprestimoRepository emprestimoRepository;
  private final CompraService compraService;
  private final SaidaService saidaService;

  public DashboardServiceImpl(
      EmprestimoService emprestimoService,
      EmprestimoRepository emprestimoRepository,
      CompraService compraService,
      SaidaService saidaService) {
    this.emprestimoService = emprestimoService;
    this.emprestimoRepository = emprestimoRepository;
    this.compraService = compraService;
    this.saidaService = saidaService;
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(
      value = "dashboard-emprestimo-range",
      keyGenerator = "dashboardCacheKeyGenerator",
      unless = "#result == null")
  public DashboardEmprestimoCountRangeResponseDto findDadosEmprestimoCountRange(
      LocalDate dtIni, LocalDate dtFim) {
    var result = emprestimoRepository.countEmprestimosByStatusInRange(dtIni, dtFim);

    if (result == null) {
      return new DashboardEmprestimoCountRangeResponseDto(0L, 0L, 0L, 0L);
    }

    return new DashboardEmprestimoCountRangeResponseDto(
        result.total(), result.emAndamento(), result.emAtraso(), result.finalizado());
  }

  @Override
  @Transactional(readOnly = true)
  public List<DashboardEmprestimoDiaResponseDto> findTotalEmprestimoByDia(
      LocalDate dtIni, LocalDate dtFim) {
    return emprestimoService.countByDataEmprestimo(dtIni, dtFim).stream()
        .map(m -> new DashboardEmprestimoDiaResponseDto(m.qtde(), m.dtEmprestimo()))
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<DashboardItensEmprestadosResponseDto> findItensMaisEmprestados(
      LocalDate dtIni, LocalDate dtFim) {
    return emprestimoService.findItensMaisEmprestados(dtIni, dtFim).stream()
        .map(m -> new DashboardItensEmprestadosResponseDto(m.qtde(), m.item()))
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<DashboardItensAdquiridosResponseDto> findItensMaisAdquiridos(
      LocalDate dtIni, LocalDate dtFim) {
    return compraService.findItensMaisAdquiridos(dtIni, dtFim).stream()
        .map(m -> new DashboardItensAdquiridosResponseDto(m.qtde(), m.item()))
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<DashboardItensSaidasResponseDto> findItensComMaisSaidas(
      LocalDate dtIni, LocalDate dtFim) {
    return saidaService.findItensMaisSaidas(dtIni, dtFim).stream()
        .map(m -> new DashboardItensSaidasResponseDto(m.qtde(), m.item()))
        .toList();
  }


}
