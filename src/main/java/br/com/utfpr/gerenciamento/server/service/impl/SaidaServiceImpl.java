package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.model.EmprestimoDevolucaoItem;
import br.com.utfpr.gerenciamento.server.model.Saida;
import br.com.utfpr.gerenciamento.server.model.SaidaItem;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensSaidas;
import br.com.utfpr.gerenciamento.server.repository.SaidaRepository;
import br.com.utfpr.gerenciamento.server.service.SaidaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class SaidaServiceImpl extends CrudServiceImpl<Saida, Long> implements SaidaService {

    @Autowired
    private SaidaRepository saidaRepository;

    @Override
    protected JpaRepository<Saida, Long> getRepository() {
        return saidaRepository;
    }

    @Override
    public List<DashboardItensSaidas> findItensMaisSaidas(LocalDate dtIni, LocalDate dtFim) {
        return saidaRepository.findItensMaisSaidas(dtIni, dtFim);
    }

    @Override
    public void createSaidaByDevolucaoEmprestimo(List<EmprestimoDevolucaoItem> emprestimoDevolucaoItem) {
        Saida saida = new Saida();
        List<SaidaItem> saidaItemList = new ArrayList<>();
        saida.setDataSaida(LocalDate.now());
        saida.setObservacao("Saída originada do empréstimo: "
                + emprestimoDevolucaoItem.get(0).getEmprestimo().getId());
        saida.setUsuarioResponsavel(emprestimoDevolucaoItem.get(0).getEmprestimo().getUsuarioResponsavel());

        emprestimoDevolucaoItem.stream().forEach(itemDevToSaida -> {
            SaidaItem saidaItem = new SaidaItem();
            saidaItem.setItem(itemDevToSaida.getItem());
            saidaItem.setQtde(itemDevToSaida.getQtde());
            saidaItem.setSaida(saida);
            saidaItemList.add(saidaItem);
        });

        saida.setSaidaItem(saidaItemList);
        saidaRepository.save(saida);
    }
}
