package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.model.RecoverPassword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

<<<<<<< Updated upstream
<<<<<<< Updated upstream
public interface RecoverPasswordRepository extends JpaRepository<RecoverPassword, Long> {
    RecoverPassword findByCode(String code);
=======
=======
>>>>>>> Stashed changes
public interface RecoverPasswordRepository extends JpaRepository<RecoverPassword, Long> , JpaSpecificationExecutor<RecoverPassword> {
  RecoverPassword findByCode(String code);
>>>>>>> Stashed changes
}
