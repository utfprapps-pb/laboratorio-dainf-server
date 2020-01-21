package br.com.utfpr.gerenciamento.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nome", length = 255, nullable = false)
    private String nome;

    @Column(name = "username", length = 100, nullable = false)
    private String username;

    @Column(name = "password", length = 1024, nullable = false)
    private String password;

    @Column(name = "email", length = 100, nullable = false)
    private String email;
}
