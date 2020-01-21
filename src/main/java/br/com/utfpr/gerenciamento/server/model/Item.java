package br.com.utfpr.gerenciamento.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.math.BigInteger;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@Table(name = "item")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotEmpty(message = "O campo 'Nome' é de preenchimento obrigatório.")
    @Column(name = "nome", length = 50, nullable = false)
    private String nome;

    @Column(name = "patrimonio")
    private BigInteger patrimonio;

    @Column(name = "siorg")
    private BigInteger siorg;

    @DecimalMin(value = "0.01", message = "O valor deve ser maior que R$ 0.00.")
    @Column(name = "valor", nullable = false)
    private BigDecimal valor;

    @Min(value = 1, message = "O valor deve ser maior que R$ 0.00.")
    @Column(name = "qtde_minima", nullable = false)
    private BigDecimal qtdeMinima;

    @Column(name = "localizacao")
    private String localizacao;

    @Column(name = "devolver")
    private Boolean devolver;

    @DecimalMin(value = "0.01", message = "O valor deve ser maior que R$ 0.00.")
    @Column(name = "saldo")
    private BigDecimal saldo;

    @ManyToOne
    @JoinColumn(name = "grupo_id", referencedColumnName = "id")
    private Grupo grupo;
}
