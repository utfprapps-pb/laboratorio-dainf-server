package br.com.utfpr.gerenciamento.server.model;

import br.com.utfpr.gerenciamento.server.config.LocalDateDeserializer;
import br.com.utfpr.gerenciamento.server.config.LocalDateSerializer;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "saida")
public class Saida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate dataSaida;

    @Column(name = "observacao")
    private String observacao;

    @NotNull(message = "Deve ser escolhido ao menos 1 produto.")
    @OneToMany(mappedBy = "saida",
            cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JsonManagedReference
    private List<SaidaItem> saidaItem;

    @ManyToOne
    @JoinColumn(name = "usuario_id", referencedColumnName = "id")
    private Usuario usuarioResponsavel;

    // utilizado apenas para ter um vínculo quando uma devolução tiver uma saída
    @Column(name = "emprestimo_id")
    private Long idEmprestimo;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Saida saida = (Saida) o;
        return getId() != null && Objects.equals(getId(), saida.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
