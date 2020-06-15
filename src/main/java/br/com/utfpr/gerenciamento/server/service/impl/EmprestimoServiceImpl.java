package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.ennumeation.StatusDevolucao;
import br.com.utfpr.gerenciamento.server.ennumeation.TipoItem;
import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.model.EmprestimoDevolucaoItem;
import br.com.utfpr.gerenciamento.server.model.EmprestimoItem;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardEmprestimoDia;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensEmprestados;
import br.com.utfpr.gerenciamento.server.model.filter.EmprestimoFilter;
import br.com.utfpr.gerenciamento.server.model.modelTemplateEmail.EmprestimoTemplate;
import br.com.utfpr.gerenciamento.server.repository.EmprestimoFilterRepository;
import br.com.utfpr.gerenciamento.server.repository.EmprestimoRepository;
import br.com.utfpr.gerenciamento.server.service.EmailService;
import br.com.utfpr.gerenciamento.server.service.EmprestimoService;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import br.com.utfpr.gerenciamento.server.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class EmprestimoServiceImpl extends CrudServiceImpl<Emprestimo, Long> implements EmprestimoService {

    @Autowired
    private EmprestimoRepository emprestimoRepository;
    @Autowired
    private EmprestimoFilterRepository emprestimoFilterRepository;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private EmailService emailService;
    private static final Logger LOGGER = Logger.getLogger(EmprestimoServiceImpl.class.getName());

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
        emprestimoItem.stream().filter(empItem -> empItem.getItem().getTipoItem().equals(TipoItem.C))
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

    @Override
    public void changePrazoDevolucao(Long idEmprestimo, LocalDate novaData) {
        var emprestimo = this.findOne(idEmprestimo);
        emprestimo.setPrazoDevolucao(novaData);
        this.save(emprestimo);
        emailService.sendEmailWithTemplate(
                converterEmprestimoToObjectTemplate(emprestimo),
                emprestimo.getUsuarioEmprestimo().getEmail(),
                "Alteração do prazo de devolução",
                "templateAlteracaoPrazoDevolucao");
    }

    @Override
    public void sendEmailConfirmacaoEmprestimo(Emprestimo emprestimo) {
        String template;
        if (emprestimo.getEmprestimoDevolucaoItem().size() > 0) {
            template = "templateConfirmacaoEmprestimo";
        } else {
            template = "templateConfirmacaoFinalizacaoEmprestimo";
        }
        emailService.sendEmailWithTemplate(
                converterEmprestimoToObjectTemplate(emprestimo),
                emprestimo.getUsuarioEmprestimo().getEmail(),
                "Confirmação de Empréstimo",
                template
        );
    }

    @Override
    public void sendEmailConfirmacaoDevolucao(Emprestimo emprestimo) {
        emailService.sendEmailWithTemplate(
                converterEmprestimoToObjectTemplate(emprestimo),
                emprestimo.getUsuarioEmprestimo().getEmail(),
                "Confirmação de Devolução do Empréstimo",
                "templateDevolucaoEmprestimo"
        );
    }

    @Override
    public void sendEmailPrazoDevolucaoProximo() {
        List<Emprestimo> emprestimos = emprestimoRepository
                .findByDataDevolucaoIsNullAndPrazoDevolucaoEquals(LocalDate.now().plusDays(3));
        if (emprestimos.size() > 0) {
            emprestimos.forEach(emprestimo -> {
                emailService.sendEmailWithTemplate(
                        converterEmprestimoToObjectTemplate(emprestimo),
                        emprestimo.getUsuarioEmprestimo().getEmail(),
                        "Empréstimo próximo da data de devolução",
                        "templateProximoPrazoDevolucaoEmprestimo");
                LOGGER.log(Level.INFO, "Email de aviso enviado com sucesso para: " + emprestimo.getUsuarioEmprestimo().getEmail());
            });
        } else {
            LOGGER.log(Level.INFO, "Nenhum empréstimo vencerá daqui 3 dias.");
        }
    }

    private EmprestimoTemplate converterEmprestimoToObjectTemplate(Emprestimo e) {
        EmprestimoTemplate toReturn = new EmprestimoTemplate();
        toReturn.setUsuarioEmprestimo(e.getUsuarioEmprestimo().getNome());
        toReturn.setDtEmprestimo(DateUtil.parseLocalDateToString(e.getDataEmprestimo()));
        toReturn.setDtPrazoDevolucao(DateUtil.parseLocalDateToString(e.getPrazoDevolucao()));
        toReturn.setDtDevolucao(
                e.getDataDevolucao() != null ? DateUtil.parseLocalDateToString(e.getDataDevolucao()) : null
        );
        toReturn.setUsuarioResponsavel(e.getUsuarioResponsavel().getNome());
        toReturn.setEmprestimoItem(e.getEmprestimoItem());
        toReturn.setEmprestimoDevolucaoItem(e.getEmprestimoDevolucaoItem());
        return toReturn;
    }
}
