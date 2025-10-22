package br.com.utfpr.gerenciamento.server.mapper;

import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.util.DateUtil;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Mapper responsável por converter entidades Emprestimo em dados para templates de email.
 *
 * <p>Extrai a lógica de preparação de template do EmailEventListener, seguindo o princípio de
 * responsabilidade única (SRP). Facilita testes unitários e reutilização da lógica de mapeamento.
 *
 * @author Rodrigo Izidoro
 * @since 2025-10-22
 */
@Component
public class EmprestimoTemplateMapper {

  /**
   * Converte Emprestimo para Map de dados do template FreeMarker.
   *
   * <p>Este método mapeia todos os campos necessários para renderização dos templates de email de
   * empréstimo: confirmação, devolução, alteração de prazo, e lembretes.
   *
   * @param emprestimo Entidade Emprestimo com todas relações carregadas
   * @return Map contendo dados formatados para template FreeMarker
   */
  public Map<String, Object> toTemplateData(Emprestimo emprestimo) {
    Map<String, Object> template = new HashMap<>();

    // Dados do usuário que fez o empréstimo
    template.put("usuarioEmprestimo", emprestimo.getUsuarioEmprestimo().getNome());

    // Datas formatadas (dd/MM/yyyy - padrão brasileiro)
    template.put("dtEmprestimo", DateUtil.parseLocalDateToString(emprestimo.getDataEmprestimo()));
    template.put(
        "dtPrazoDevolucao", DateUtil.parseLocalDateToString(emprestimo.getPrazoDevolucao()));

    // Data de devolução (pode ser null para empréstimos em aberto)
    template.put(
        "dtDevolucao",
        emprestimo.getDataDevolucao() != null
            ? DateUtil.parseLocalDateToString(emprestimo.getDataDevolucao())
            : null);

    // Dados do responsável pelo empréstimo
    template.put("usuarioResponsavel", emprestimo.getUsuarioResponsavel().getNome());

    // Listas de itens (emprestados e de devolução)
    template.put("emprestimoItem", emprestimo.getEmprestimoItem());
    template.put("emprestimoDevolucaoItem", emprestimo.getEmprestimoDevolucaoItem());

    return template;
  }
}
