package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Solicitacao;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long> {

    List<Solicitacao> findAllByUsuario(Usuario usuario);
}
