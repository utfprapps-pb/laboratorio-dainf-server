package br.com.utfpr.gerenciamento.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
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

    @Column(name = "valor", columnDefinition = "NUMERIC (19,2) DEFAULT '0.00'")
    private BigDecimal valor = new BigDecimal(0);

    @Column(name = "qtde_minima", nullable = false)
    private BigDecimal qtdeMinima;

    @Column(name = "localizacao")
    private String localizacao;

    @Column(name = "devolver")
    private Boolean devolver;

    @Column(name = "saldo")
    private BigDecimal saldo;

    @ManyToOne
    @JoinColumn(name = "grupo_id", referencedColumnName = "id")
    private Grupo grupo;
}
