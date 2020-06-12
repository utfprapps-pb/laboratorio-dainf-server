package br.com.utfpr.gerenciamento.server.model.modelTemplateEmail;

import br.com.utfpr.gerenciamento.server.model.EmprestimoDevolucaoItem;
import lombok.Data;

import java.util.List;

@Data
public class DevolucaoTemplate {

    private String usuarioResponsavel;
    private String usuarioEmprestimo;
    private String dtEmprestimo;
    private String dtDevolucao;
    private List<EmprestimoDevolucaoItem> emprestimoDevolucaoItem;
}
