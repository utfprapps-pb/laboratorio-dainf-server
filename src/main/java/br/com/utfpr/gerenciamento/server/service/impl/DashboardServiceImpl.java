package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.dto.dashboards.*;
import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.model.dashboards.*;
import br.com.utfpr.gerenciamento.server.service.CompraService;
import br.com.utfpr.gerenciamento.server.service.DashboardService;
import br.com.utfpr.gerenciamento.server.service.EmprestimoService;
import br.com.utfpr.gerenciamento.server.service.SaidaService;
import java.time.LocalDate;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardServiceImpl implements DashboardService {

  private final EmprestimoService emprestimoService;
  private final CompraService compraService;
  private final SaidaService saidaService;

  private final ModelMapper modelMapper;

  public DashboardServiceImpl(
      EmprestimoService emprestimoService,
      CompraService compraService,
      SaidaService saidaService,
      ModelMapper modelMapper) {
    this.emprestimoService = emprestimoService;
    this.compraService = compraService;
    this.saidaService = saidaService;
    this.modelMapper = modelMapper;
  }

  @Override
  @Transactional(readOnly = true)
  public DashboardEmprestimoCountRangeResponseDto findDadosEmprestimoCountRange(
      LocalDate dtIni, LocalDate dtFim) {
    List<Emprestimo> emprestimoList =
        emprestimoService.findAllByDataEmprestimoBetween(dtIni, dtFim);

    DashboardEmprestimoCountRange toReturn = new DashboardEmprestimoCountRange();
    toReturn.setTotal(emprestimoList.size());
    toReturn.setEmAtraso(
        (int)
            emprestimoList.stream()
                .filter(
                    emprestimo ->
                        emprestimo.getPrazoDevolucao().isBefore(LocalDate.now())
                            && emprestimo.getDataDevolucao() == null)
                .count());
    toReturn.setEmAndamento(
        (int)
            emprestimoList.stream()
                .filter(
                    emprestimo ->
                        emprestimo.getDataDevolucao() == null
                            && (emprestimo.getPrazoDevolucao().isEqual(LocalDate.now())
                                || emprestimo.getPrazoDevolucao().isAfter(LocalDate.now())))
                .count());
    toReturn.setFinalizado(
        (int)
            emprestimoList.stream()
                .filter(emprestimo -> emprestimo.getDataDevolucao() != null)
                .count());
    return convertToDto(toReturn, DashboardEmprestimoCountRangeResponseDto.class);
  }

  @Override
  @Transactional(readOnly = true)
  public List<DashboardEmprestimoDiaResponseDto> findTotalEmprestimoByDia(
      LocalDate dtIni, LocalDate dtFim) {
    return emprestimoService.countByDataEmprestimo(dtIni, dtFim).stream()
        .map(entity -> convertToDto(entity, DashboardEmprestimoDiaResponseDto.class))
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<DashboardItensEmprestadosResponseDto> findItensMaisEmprestados(
      LocalDate dtIni, LocalDate dtFim) {
    return emprestimoService.findItensMaisEmprestados(dtIni, dtFim).stream()
        .map(entity -> convertToDto(entity, DashboardItensEmprestadosResponseDto.class))
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<DashboardItensAdquiridosResponseDto> findItensMaisAdquiridos(
      LocalDate dtIni, LocalDate dtFim) {
    return compraService.findItensMaisAdquiridos(dtIni, dtFim).stream()
        .map(entity -> convertToDto(entity, DashboardItensAdquiridosResponseDto.class))
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<DashboardItensSaidasResponseDto> findItensComMaisSaidas(
      LocalDate dtIni, LocalDate dtFim) {
    return saidaService.findItensMaisSaidas(dtIni, dtFim).stream()
        .map(entity -> convertToDto(entity, DashboardItensSaidasResponseDto.class))
        .toList();
  }

  @Override
  public <D, E> D convertToDto(E entity, Class<D> dtoClass) {
    return modelMapper.map(entity, dtoClass);
  }
}
