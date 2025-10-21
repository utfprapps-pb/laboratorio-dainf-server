package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.EmprestimoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface EmprestimoItemRepository extends JpaRepository<EmprestimoItem, Long>, JpaSpecificationExecutor<EmprestimoItem> {

    @Query("SELECT SUM(e.qtde) FROM EmprestimoItem e WHERE e.item.id = :itemId AND e.emprestimo.dataDevolucao IS NULL")
    BigDecimal findQtdeEmprestadaByItemIdAndEmprestimo_DataDevolucaoIsNull(@Param("itemId") Long itemId);

}
