package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.ennumeation.StatusDevolucao;
import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.model.EmprestimoDevolucaoItem;
import br.com.utfpr.gerenciamento.server.model.EmprestimoItem;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardEmprestimoDia;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensEmprestados;
import br.com.utfpr.gerenciamento.server.model.filter.EmprestimoFilter;
import br.com.utfpr.gerenciamento.server.repository.EmprestimoFilterRepository;
import br.com.utfpr.gerenciamento.server.repository.EmprestimoRepository;
import br.com.utfpr.gerenciamento.server.service.EmprestimoService;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmprestimoServiceImpl extends CrudServiceImpl<Emprestimo, Long> implements EmprestimoService {

    @Autowired
    private EmprestimoRepository emprestimoRepository;
    @Autowired
    private EmprestimoFilterRepository emprestimoFilterRepository;
    @Autowired
    private UsuarioService usuarioService;

    @Override
    protected JpaRepository<Emprestimo, Long> getRepository() {
        return emprestimoRepository;
    }

    @Override
    public List<Emprestimo> findAllByDataEmprestimoBetween(LocalDate dtIni, LocalDate dtFim) {
        return emprestimoRepository.findAllByDataEmprestimoBetween(dtIni, dtFim);
    }

    @Override
    public List<DashboardEmprestimoDia> countByDataEmprestimo(LocalDate dtIni, LocalDate dtFim) {
        return emprestimoRepository.countByDataEmprestimo(dtIni, dtFim);
    }

    @Override
    public List<DashboardItensEmprestados> findItensMaisEmprestados(LocalDate dtIni, LocalDate dtFim) {
        return emprestimoRepository.findItensMaisEmprestados(dtIni, dtFim);
    }

    @Override
    public List<EmprestimoDevolucaoItem> createEmprestimoItemDevolucao(List<EmprestimoItem> emprestimoItem) {
        List<EmprestimoDevolucaoItem> toReturn = new ArrayList<>();
        emprestimoItem.stream().filter(empItem -> empItem.getItem().getDevolver().equals(true))
                .forEach(empItem1 -> {
                    EmprestimoDevolucaoItem empDevItem = new EmprestimoDevolucaoItem();
                    empDevItem.setItem(empItem1.getItem());
                    empDevItem.setQtde(empItem1.getQtde());
                    empDevItem.setStatusDevolucao(StatusDevolucao.P);
                    empDevItem.setEmprestimo(empItem1.getEmprestimo());
                    toReturn.add(empDevItem);
                });
        return toReturn;
    }

    @Override
    public List<Emprestimo> filter(EmprestimoFilter emprestimoFilter) {
        return emprestimoFilterRepository.filter(emprestimoFilter);
    }

    @Override
    public List<Emprestimo> findAllUsuarioEmprestimo(String username) {
        var usuario = usuarioService.findByUsername(username);
        return emprestimoRepository.findAllByUsuarioEmprestimo(usuario);
    }

    @Override
    public List<Emprestimo> findAllEmprestimosAbertos() {
        return emprestimoRepository.findAllByDataDevolucaoIsNullOrderById();
    }
}
