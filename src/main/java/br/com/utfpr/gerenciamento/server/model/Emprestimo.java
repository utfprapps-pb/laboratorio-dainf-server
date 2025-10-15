package br.com.utfpr.gerenciamento.server.model;

import br.com.utfpr.gerenciamento.server.config.LocalDateDeserializer;
import br.com.utfpr.gerenciamento.server.config.LocalDateSerializer;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "emprestimo")
public class Emprestimo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @JsonSerialize(using = LocalDateSerializer.class)
  @JsonDeserialize(using = LocalDateDeserializer.class)
  @NotNull(message = "O campo 'Data do Emprestimo' deve ser selecionado.") @Column(name = "data_emprestimo")
  private LocalDate dataEmprestimo;

  @JsonSerialize(using = LocalDateSerializer.class)
  @JsonDeserialize(using = LocalDateDeserializer.class)
  @Column(name = "prazo_devolucao")
  private LocalDate prazoDevolucao;

  @JsonSerialize(using = LocalDateSerializer.class)
  @JsonDeserialize(using = LocalDateDeserializer.class)
  @Column(name = "data_devolucao")
  private LocalDate dataDevolucao;

  @ManyToOne
  @JoinColumn(name = "usuario_responsavel_id", referencedColumnName = "id")
  private Usuario usuarioResponsavel;

  @ManyToOne
  @JoinColumn(name = "usuario_emprestimo_id", referencedColumnName = "id")
  private Usuario usuarioEmprestimo;

  @Column(name = "observacao")
  private String observacao;

  @NotNull(message = "Deve ser escolhido ao menos 1 produto.") @OneToMany(
      mappedBy = "emprestimo",
      cascade = {CascadeType.ALL},
      orphanRemoval = true)
  @JsonManagedReference
  private List<EmprestimoItem> emprestimoItem;

  @OneToMany(
      mappedBy = "emprestimo",
      cascade = {CascadeType.ALL},
      orphanRemoval = true)
  @JsonManagedReference
  private List<EmprestimoDevolucaoItem> emprestimoDevolucaoItem;

  @Override
  @SuppressWarnings(
      "java:S2097") // False positive - type check via HibernateProxy pattern (SONARJAVA-5765)
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    Class<?> oEffectiveClass =
        o instanceof HibernateProxy hibernateProxy
            ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass()
            : o.getClass();
    Class<?> thisEffectiveClass =
        this instanceof HibernateProxy hibernateProxy
            ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass()
            : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) return false;
    Emprestimo that = (Emprestimo) o;
    return getId() != null && Objects.equals(getId(), that.getId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy hibernateProxy
        ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass().hashCode()
        : getClass().hashCode();
  }
}
