package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.model.Relatorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

<<<<<<< Updated upstream
<<<<<<< Updated upstream
public interface RelatorioRepository extends JpaRepository<Relatorio, Long> {
}
=======
public interface RelatorioRepository extends JpaRepository<Relatorio, Long>, JpaSpecificationExecutor<Relatorio> {}
>>>>>>> Stashed changes
=======
public interface RelatorioRepository extends JpaRepository<Relatorio, Long>, JpaSpecificationExecutor<Relatorio> {}
>>>>>>> Stashed changes
