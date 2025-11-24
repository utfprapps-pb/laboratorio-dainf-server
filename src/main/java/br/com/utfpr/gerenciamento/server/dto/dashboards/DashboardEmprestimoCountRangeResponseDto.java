package br.com.utfpr.gerenciamento.server.dto.dashboards;

/**
 * DTO de resposta para contadores de empréstimos agregados por status.
 *
 * <p>Usa Long para prevenir overflow e manter compatibilidade com o modelo de domínio.
 *
 * @param total Total de empréstimos no período
 * @param emAndamento Empréstimos ativos dentro do prazo
 * @param emAtraso Empréstimos ativos com prazo vencido
 * @param finalizado Empréstimos devolvidos
 */
public record DashboardEmprestimoCountRangeResponseDto(
    Long total, Long emAndamento, Long emAtraso, Long finalizado) {}
