package br.com.utfpr.gerenciamento.server.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

/**
 * Entidade de revisão customizada para Hibernate Envers.
 *
 * <p>Armazena metadados de cada operação auditada, incluindo identificador da revisão, momento da
 * operação, usuário responsável e endereço IP da requisição.
 *
 * @author Rodrigo Izidoro
 * @see AuditRevisionListener
 */
@Entity
@Table(name = "revinfo")
@RevisionEntity(AuditRevisionListener.class)
@Getter
@Setter
public class AuditRevision {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @RevisionNumber
  private Long id;

  @RevisionTimestamp
  @Column(name = "timestamp", nullable = false)
  private Long timestamp;

  @Column(name = "usuario", length = 100)
  private String usuario;

  @Column(name = "ip", length = 45)
  private String ip;
}
