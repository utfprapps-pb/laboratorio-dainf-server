package br.com.utfpr.gerenciamento.server.model;

import br.com.utfpr.gerenciamento.server.annotation.UtfprEmailValidator;
import jakarta.persistence.*;
import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "system_config")
@Getter
@Setter
@NoArgsConstructor
public class SystemConfig implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @UtfprEmailValidator
  @Column(name = "nada_consta_email", nullable = false)
  private String nadaConstaEmail;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;
}
