package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.model.Compra;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensAdquiridos;
import br.com.utfpr.gerenciamento.server.repository.CompraRepository;
import br.com.utfpr.gerenciamento.server.service.CompraService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompraServiceImpl extends CrudServiceImpl<Compra, Long> implements CompraService {

  private final CompraRepository compraRepository;

  public CompraServiceImpl(CompraRepository compraRepository) {
    this.compraRepository = compraRepository;
  }

  @Override
  protected JpaRepository<Compra, Long> getRepository() {
    return compraRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<DashboardItensAdquiridos> findItensMaisAdquiridos(LocalDate dtIni, LocalDate dtFim) {
    return compraRepository.findItensMaisAdquiridos(dtIni, dtFim);
  }
}
