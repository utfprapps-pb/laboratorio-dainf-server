package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.model.filter.EmprestimoFilter;
import java.util.List;

public interface EmprestimoFilterRepository {

  List<Emprestimo> filter(EmprestimoFilter emprestimoFilter);
}
