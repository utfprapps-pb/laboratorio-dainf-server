package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.dto.EmprestimoResponseDto;
import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.model.EmprestimoDevolucaoItem;
import br.com.utfpr.gerenciamento.server.model.EmprestimoItem;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardEmprestimoDia;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensEmprestados;
import br.com.utfpr.gerenciamento.server.model.filter.EmprestimoFilter;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmprestimoService extends CrudService<Emprestimo, Long, EmprestimoResponseDto> {

  /**
   * Busca paginada com filtro textual e cache otimizado.
   *
   * <p>Esta versão aceita parâmetros estáveis (String + Pageable) para cache key determinística,
   * resolvendo problema de Specification com equals/hashCode instável.
   *
   * @param textFilter Filtro textual opcional (busca em todos os campos)
   * @param pageable Configuração de paginação e ordenação
   * @return Página de empréstimos com JOIN FETCH otimizado
   */
  Page<EmprestimoResponseDto> findAllPagedWithTextFilter(String textFilter, Pageable pageable);

  List<EmprestimoResponseDto> findAllByDataEmprestimoBetween(LocalDate dtIni, LocalDate dtFim);

  List<DashboardEmprestimoDia> countByDataEmprestimo(LocalDate dtIni, LocalDate dtFim);

  List<DashboardItensEmprestados> findItensMaisEmprestados(LocalDate dtIni, LocalDate dtFim);

  List<EmprestimoDevolucaoItem> createEmprestimoItemDevolucao(List<EmprestimoItem> emprestimoItem);

  List<EmprestimoResponseDto> filter(EmprestimoFilter emprestimoFilter);

  List<EmprestimoResponseDto> findAllUsuarioEmprestimo(String username);

  List<EmprestimoResponseDto> findAllEmprestimosAbertos();

  List<EmprestimoResponseDto> findAllEmprestimosAbertosByUsuario(String username);

  void changePrazoDevolucao(Long idEmprestimo, LocalDate novaData);

  void sendEmailConfirmacaoEmprestimo(Emprestimo emprestimo);

  void sendEmailConfirmacaoDevolucao(Emprestimo emprestimo);

  void sendEmailPrazoDevolucaoProximo();

  /**
   * Processa criação/edição de empréstimo com toda lógica de negócio.
   *
   * @param emprestimo Empréstimo a ser processado
   * @param idReserva ID da reserva a finalizar (0 se não houver)
   * @return DTO do empréstimo salvo
   */
  EmprestimoResponseDto processEmprestimo(Emprestimo emprestimo, Long idReserva);

  /**
   * Processa devolução de empréstimo com toda lógica de negócio.
   *
   * @param emprestimo Empréstimo com dados de devolução
   * @return DTO do empréstimo atualizado
   */
  EmprestimoResponseDto processDevolucao(Emprestimo emprestimo);

  /**
   * Prepara empréstimo antes de salvar (restaura saldo, valida itens, cria itens de devolução).
   *
   * @param emprestimo Empréstimo a preparar
   */
  void prepareEmprestimo(Emprestimo emprestimo);

  /**
   * Finaliza empréstimo após salvar (baixa saldo, envia email).
   *
   * @param emprestimo Empréstimo salvo
   */
  void finalizeEmprestimo(Emprestimo emprestimo);

  /**
   * Limpa dados após deletar empréstimo (restaura saldo, deleta saídas).
   *
   * @param emprestimo Empréstimo deletado
   */
  void cleanupAfterDelete(Emprestimo emprestimo);
}
