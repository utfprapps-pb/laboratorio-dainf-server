package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.EmprestimoItem;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmprestimoItemRepository
    extends JpaRepository<EmprestimoItem, Long>, JpaSpecificationExecutor<EmprestimoItem> {

  @Query(
      "SELECT SUM(e.qtde) FROM EmprestimoItem e WHERE e.item.id = :itemId AND e.emprestimo.dataDevolucao IS NULL")
  BigDecimal findQtdeEmprestadaByItemIdAndEmprestimo_DataDevolucaoIsNull(
      @Param("itemId") Long itemId);
}
