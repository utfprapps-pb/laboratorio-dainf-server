package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.dto.SaidaResponseDTO;
import br.com.utfpr.gerenciamento.server.model.EmprestimoDevolucaoItem;
import br.com.utfpr.gerenciamento.server.model.Saida;
import br.com.utfpr.gerenciamento.server.model.SaidaItem;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensSaidas;
import br.com.utfpr.gerenciamento.server.repository.SaidaRepository;
import br.com.utfpr.gerenciamento.server.service.SaidaService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaidaServiceImpl extends CrudServiceImpl<Saida, Long, SaidaResponseDTO>
    implements SaidaService {

  private final SaidaRepository saidaRepository;
  private final ModelMapper modelMapper;

  public SaidaServiceImpl(SaidaRepository saidaRepository, ModelMapper modelMapper) {
    this.saidaRepository = saidaRepository;
    this.modelMapper = modelMapper;
  }

  @Override
  protected JpaRepository<Saida, Long> getRepository() {
    return saidaRepository;
  }

  @Override
  public SaidaResponseDTO toDto(Saida entity) {
    return modelMapper.map(entity, SaidaResponseDTO.class);
  }

  @Override
  public Saida toEntity(SaidaResponseDTO saidaResponseDTO) {
    return modelMapper.map(saidaResponseDTO, Saida.class);
  }

  @Override
  @Transactional(readOnly = true)
  public List<DashboardItensSaidas> findItensMaisSaidas(LocalDate dtIni, LocalDate dtFim) {
    return saidaRepository.findItensMaisSaidas(dtIni, dtFim);
  }

  @Override
  @Transactional
  public void createSaidaByDevolucaoEmprestimo(
      List<EmprestimoDevolucaoItem> emprestimoDevolucaoItem) {
    Saida saida = new Saida();
    List<SaidaItem> saidaItemList = new ArrayList<>();
    saida.setIdEmprestimo(emprestimoDevolucaoItem.get(0).getEmprestimo().getId());
    saida.setDataSaida(LocalDate.now());
    saida.setObservacao(
        "Saída originada do empréstimo: " + emprestimoDevolucaoItem.get(0).getEmprestimo().getId());
    saida.setUsuarioResponsavel(
        emprestimoDevolucaoItem.get(0).getEmprestimo().getUsuarioResponsavel());

    emprestimoDevolucaoItem.stream()
        .forEach(
            itemDevToSaida -> {
              SaidaItem saidaItem = new SaidaItem();
              saidaItem.setItem(itemDevToSaida.getItem());
              saidaItem.setQtde(itemDevToSaida.getQtde());
              saidaItem.setSaida(saida);
              saidaItemList.add(saidaItem);
            });

    saida.setSaidaItem(saidaItemList);
    saidaRepository.save(saida);
  }

  @Override
  @Transactional
  public void deleteSaidaByEmprestimo(Long idEmprestimo) {
    var saidaToDelete = saidaRepository.findByIdEmprestimo(idEmprestimo);
    if (saidaToDelete != null) {
      saidaRepository.delete(saidaToDelete);
    }
  }
}
