package br.com.utfpr.gerenciamento.server.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.math.BigDecimal;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@Table(name = "reserva_item")
public class ReservaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Min(value = 1, message = "Deve ser escolhido ao menos 1 item.")
    @Column(name = "qtde", nullable = false)
    private BigDecimal qtde;

    @ManyToOne
    @JoinColumn(name = "item_id", referencedColumnName = "id")
    private Item item;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "reserva_id", referencedColumnName = "id")
    private Reserva reserva;
}
