package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.dto.CompraListDto;
import br.com.utfpr.gerenciamento.server.dto.CompraResponseDTO;
import br.com.utfpr.gerenciamento.server.model.Compra;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensAdquiridos;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompraService extends CrudService<Compra, Long, CompraResponseDTO> {

  /**
   * Busca paginada para listagem com DTO simplificado.
   *
   * @param filter Filtro textual opcional
   * @param pageable Configuração de paginação
   * @return Página de DTOs simplificados
   */
  Page<CompraListDto> findAllPagedList(String filter, Pageable pageable);

  List<DashboardItensAdquiridos> findItensMaisAdquiridos(LocalDate dtIni, LocalDate dtFim);
}
