package br.com.utfpr.gerenciamento.server.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "fornecedor")
public class Fornecedor {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @NotEmpty(message = "O campo 'Razão Social' é de preenchimento obrigatório.") @Column(name = "razao_social", length = 80, nullable = false)
  private String razaoSocial;

  @NotEmpty(message = "O campo 'Nome Fantasia' é de preenchimento obrigatório.") @Column(name = "nome_fantasia", length = 80, nullable = false)
  private String nomeFantasia;

  @NotEmpty(message = "O campo 'CNPJ' é de preenchimento obrigatório.") @Column(name = "cnpj", length = 14, nullable = false)
  private String cnpj;

  @NotEmpty(message = "O campo 'Inscrição Estadual' é de preenchimento obrigatório.") @Column(name = "ie", length = 14, nullable = false)
  private String ie;

  @Column(name = "endereco", length = 100)
  private String endereco;

  @Column(name = "observacao", length = 2000)
  private String observacao;

  @Column(name = "email")
  private String email;

  @Column(name = "telefone", length = 15)
  private String telefone;

  @NotNull(message = "O campo 'Cidade' deve ser selecionado.") @ManyToOne
  @JoinColumn(name = "cidade_id", referencedColumnName = "id")
  private Cidade cidade;

  @NotNull(message = "O campo 'Estado' deve ser selecionado.") @ManyToOne
  @JoinColumn(name = "estado_id", referencedColumnName = "id")
  private Estado estado;

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
    Fornecedor that = (Fornecedor) o;
    return getId() != null && Objects.equals(getId(), that.getId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy hibernateProxy
        ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass().hashCode()
        : getClass().hashCode();
  }
}
