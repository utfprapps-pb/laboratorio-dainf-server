package br.com.utfpr.gerenciamento.server.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;

import jakarta.persistence.*;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "relatorio")
public class Relatorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "name_report")
    private String nameReport;

    @OneToMany(mappedBy = "relatorio", cascade = {CascadeType.ALL},
            orphanRemoval = true)
    @JsonManagedReference
    private List<RelatorioParams> paramsList;
}
