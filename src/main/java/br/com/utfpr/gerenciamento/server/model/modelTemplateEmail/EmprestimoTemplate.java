package br.com.utfpr.gerenciamento.server.model.modelTemplateEmail;

import br.com.utfpr.gerenciamento.server.model.EmprestimoDevolucaoItem;
import br.com.utfpr.gerenciamento.server.model.EmprestimoItem;
import lombok.Data;

import java.util.List;

@Data
public class EmprestimoTemplate {

    private String usuarioResponsavel;
    private String usuarioEmprestimo;
    private String dtEmprestimo;
    private String dtPrazoDevolucao;
    private String dtDevolucao;
    private List<EmprestimoItem> emprestimoItem;
    private List<EmprestimoDevolucaoItem> emprestimoDevolucaoItem;
}
