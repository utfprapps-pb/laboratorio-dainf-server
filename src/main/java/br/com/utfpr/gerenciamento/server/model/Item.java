package br.com.utfpr.gerenciamento.server.model;

import br.com.utfpr.gerenciamento.server.enumeration.TipoItem;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;
import org.hibernate.proxy.HibernateProxy;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "item")
public class Item {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @NotEmpty(message = "O campo 'Nome' é de preenchimento obrigatório.") @Column(name = "nome", length = 50, nullable = false)
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

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_item", length = 1)
  private TipoItem tipoItem;

  @Column(name = "saldo")
  private BigDecimal saldo;

  @ManyToOne
  @JoinColumn(name = "grupo_id", referencedColumnName = "id")
  private Grupo grupo;

  @Column(name = "descricao", length = 4000)
  private String descricao;

  @OneToMany(
      mappedBy = "item",
      cascade = {CascadeType.ALL},
      orphanRemoval = true)
  @JsonManagedReference
  private List<ItemImage> imageItem;

  @Transient
  private BigDecimal disponivelEmprestimoCalculado;

  @Formula("(SELECT COALESCE(SUM(e.qtde), 0) " +
          "FROM emprestimo_item e " +
          "JOIN emprestimo em ON e.emprestimo_id = em.id " +
          "WHERE e.item_id = id AND em.data_devolucao IS NULL)")
  private BigDecimal disponivelEmprestimo;
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
    Item item = (Item) o;
    return getId() != null && Objects.equals(getId(), item.getId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy hibernateProxy
        ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass().hashCode()
        : getClass().hashCode();
  }
}
