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
@Table(name = "compra")
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @NotNull(message = "O campo 'Data de Compra' deve ser selecionado.")
    @Column(name = "data_compra", nullable = false)
    private LocalDate dataCompra;

    @NotNull(message = "O campo 'Fornecedor' deve ser escolhido.")
    @ManyToOne
    @JoinColumn(name = "fornecedor_id", referencedColumnName = "id")
    private Fornecedor fornecedor;

    @ManyToOne
    @JoinColumn(name = "usuario_id", referencedColumnName = "id")
    private Usuario usuario;

    @NotNull(message = "Deve ser escolhido ao menos 1 produto.")
    @OneToMany(mappedBy = "compra",
            cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JsonManagedReference
    private List<CompraItem> compraItem;
}
