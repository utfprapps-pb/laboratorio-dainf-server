package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Relatorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RelatorioRepository
    extends JpaRepository<Relatorio, Long>, JpaSpecificationExecutor<Relatorio> {}
