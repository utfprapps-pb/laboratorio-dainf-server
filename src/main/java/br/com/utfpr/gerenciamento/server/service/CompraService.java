package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.dto.CompraResponseDTO;
import br.com.utfpr.gerenciamento.server.model.Compra;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensAdquiridos;
import java.time.LocalDate;
import java.util.List;

public interface CompraService extends CrudService<Compra, Long, CompraResponseDTO> {

  List<DashboardItensAdquiridos> findItensMaisAdquiridos(LocalDate dtIni, LocalDate dtFim);
}
