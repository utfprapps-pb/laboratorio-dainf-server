package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.dto.SaidaListDto;
import br.com.utfpr.gerenciamento.server.dto.SaidaResponseDTO;
import br.com.utfpr.gerenciamento.server.model.EmprestimoDevolucaoItem;
import br.com.utfpr.gerenciamento.server.model.Saida;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensSaidas;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SaidaService extends CrudService<Saida, Long, SaidaResponseDTO> {

  /**
   * Busca paginada para listagem com DTO simplificado.
   *
   * @param filter Filtro textual opcional
   * @param pageable Configuração de paginação
   * @return Página de DTOs simplificados
   */
  Page<SaidaListDto> findAllPagedList(String filter, Pageable pageable);

  List<DashboardItensSaidas> findItensMaisSaidas(LocalDate dtIni, LocalDate dtFim);

  void createSaidaByDevolucaoEmprestimo(List<EmprestimoDevolucaoItem> emprestimoDevolucaoItem);

  void deleteSaidaByEmprestimo(Long idEmprestimo);
}
