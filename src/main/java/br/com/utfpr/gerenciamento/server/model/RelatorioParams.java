package br.com.utfpr.gerenciamento.server.model;

import br.com.utfpr.gerenciamento.server.ennumeation.TypeParamReport;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;

import jakarta.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "relatorio_params")
public class RelatorioParams {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name_param", nullable = false, length = 30)
    private String nameParam;

    @Column(name = "alias_param", nullable = false, length = 50)
    private String aliasParam;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_param", nullable = false, length = 1)
    private TypeParamReport tipoParam;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "relatorio_id", referencedColumnName = "id")
    private Relatorio relatorio;
}
