package br.com.utfpr.gerenciamento.server.model;

import lombok.*;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Entity(name = "tb_recover_password")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecoverPassword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String code;
    private LocalDateTime dateTime;

}
