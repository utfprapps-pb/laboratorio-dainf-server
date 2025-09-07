package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.model.Solicitacao;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
<<<<<<< Updated upstream

<<<<<<< Updated upstream
import java.util.List;

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long> {
=======
public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long>, JpaSpecificationExecutor<Solicitacao> {
>>>>>>> Stashed changes
=======

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long>, JpaSpecificationExecutor<Solicitacao> {
>>>>>>> Stashed changes

    List<Solicitacao> findAllByUsuario(Usuario usuario);
}
