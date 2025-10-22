package br.com.utfpr.gerenciamento.server.model.dashboards;

/**
 * DTO para contadores de empréstimos agregados por status.
 *
 * <p>Usa Long para evitar overflow em sistemas de longo prazo com muitos registros.
 *
 * @param total Total de empréstimos no período
 * @param emAndamento Empréstimos ativos dentro do prazo
 * @param emAtraso Empréstimos ativos com prazo vencido
 * @param finalizado Empréstimos devolvidos
 */
public record DashboardEmprestimoCountRange(
    Long total, Long emAndamento, Long emAtraso, Long finalizado) {}
