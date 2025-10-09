package br.com.utfpr.gerenciamento.server.dto.dashboards;

import lombok.Data;

/**
 * DTO de resposta para contadores de empréstimos agregados por status.
 *
 * <p>Usa Long para prevenir overflow e manter compatibilidade com o modelo de domínio. ModelMapper
 * faz conversão direta Long → Long sem downcast.
 */
@Data
public class DashboardEmprestimoCountRangeResponseDto {
  private Long total;

  private Long emAndamento;

  private Long emAtraso;

  private Long finalizado;
}
