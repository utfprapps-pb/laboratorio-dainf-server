package br.com.utfpr.gerenciamento.server.model.modelTemplate;

import br.com.utfpr.gerenciamento.server.model.EmprestimoDevolucaoItem;
import br.com.utfpr.gerenciamento.server.model.EmprestimoItem;
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
