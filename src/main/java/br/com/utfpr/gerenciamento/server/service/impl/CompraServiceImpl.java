package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.model.Compra;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensAdquiridos;
import br.com.utfpr.gerenciamento.server.repository.CompraRepository;
import br.com.utfpr.gerenciamento.server.service.CompraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CompraServiceImpl extends CrudServiceImpl<Compra, Long> implements CompraService {

    @Autowired
    private CompraRepository compraRepository;

    @Override
    protected JpaRepository<Compra, Long> getRepository() {
        return compraRepository;
    }

    @Override
    public List<DashboardItensAdquiridos> findItensMaisAdquiridos(LocalDate dtIni, LocalDate dtFim) {
        return compraRepository.findItensMaisAdquiridos(dtIni, dtFim);
    }
}
