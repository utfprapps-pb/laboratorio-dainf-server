package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.model.Saida;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensSaidas;
import br.com.utfpr.gerenciamento.server.repository.SaidaRepository;
import br.com.utfpr.gerenciamento.server.service.SaidaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
}
