package br.com.utfpr.gerenciamento.server.audit.dto;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * DTO representando uma entrada no histórico de auditoria.
 *
 * <p>Contém informações sobre a revisão (número, data/hora, usuário, IP, tipo de operação) e o
 * estado da entidade naquele momento.
 *
 * @author Rodrigo Izidoro
 */
@Data
@Builder
public class AuditEntryDto {

  private Long revisao;
  private LocalDateTime dataHora;
  private String usuario;
  private String ip;
  private String tipoOperacao;
  private Map<String, Object> entidade;
}
