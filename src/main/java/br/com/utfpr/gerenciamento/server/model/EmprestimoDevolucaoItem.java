package br.com.utfpr.gerenciamento.server.model;

import br.com.utfpr.gerenciamento.server.ennumeation.StatusDevolucao;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
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
@Table(name = "emprestimo_devolucao_item")
public class EmprestimoDevolucaoItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "qtde", nullable = false)
  private BigDecimal qtde;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", length = 1)
  private StatusDevolucao statusDevolucao;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "item_id", referencedColumnName = "id", nullable = false)
  private Item item;

  @ManyToOne
  @JsonBackReference
  @JoinColumn(name = "emprestimo_id", referencedColumnName = "id")
  private Emprestimo emprestimo;

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
    EmprestimoDevolucaoItem that = (EmprestimoDevolucaoItem) o;
    return getId() != null && Objects.equals(getId(), that.getId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy hibernateProxy
        ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass().hashCode()
        : getClass().hashCode();
  }
}
