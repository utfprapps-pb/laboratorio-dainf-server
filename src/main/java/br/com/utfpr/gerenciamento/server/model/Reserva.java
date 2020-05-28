package br.com.utfpr.gerenciamento.server.model;

import br.com.utfpr.gerenciamento.server.config.LocalDateDeserializer;
import br.com.utfpr.gerenciamento.server.config.LocalDateSerializer;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@Table(name = "reserva")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "descricao")
    private String descricao;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @Column(name = "data_reserva")
    private LocalDate dataReserva;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @Column(name = "data_retirada")
    private LocalDate dataRetirada;

    @Column(name = "observacao")
    private String observacao;

    @ManyToOne
    @JoinColumn(name = "usuario_id", referencedColumnName = "id")
    private Usuario usuario;

    @NotNull(message = "Deve ser escolhido ao menos 1 item.")
    @OneToMany(mappedBy = "reserva",
            cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JsonManagedReference
    private List<ReservaItem> reservaItem;
}
