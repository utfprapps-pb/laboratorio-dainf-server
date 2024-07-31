package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.RecoverPassword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecoverPasswordRepository extends JpaRepository<RecoverPassword, Long> {
    RecoverPassword findByCode(String code);
}
