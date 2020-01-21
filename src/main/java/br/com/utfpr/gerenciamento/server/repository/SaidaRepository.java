package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Saida;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaidaRepository extends JpaRepository<Saida, Long> {
}
