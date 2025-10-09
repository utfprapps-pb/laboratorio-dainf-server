package br.com.utfpr.gerenciamento.server.config;

import java.lang.reflect.Method;
import java.time.LocalDate;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

/**
 * Gerador de chave de cache inteligente para Dashboard com TTL variável.
 *
 * <p>Gera chaves de cache com sufixos que determinam o TTL via {@link DashboardCacheExpiry}: -
 * Queries com data "até hoje": adiciona sufixo "_CURRENT" → TTL curto (5 min) - Queries com datas
 * passadas: adiciona sufixo "_HISTORICAL" → TTL longo (6 horas)
 *
 * <p>Se o segundo parâmetro não for LocalDate, nenhum sufixo TTL é adicionado.
 *
 * <p>Exemplo: - findDadosEmprestimoCountRange(2025-01-01, 2025-10-07) → chave com "_CURRENT" → TTL
 * 5 min - findDadosEmprestimoCountRange(2024-01-01, 2024-12-31) → chave com "_HISTORICAL" → TTL 6h
 *
 * <p>O sufixo é lido por {@link DashboardCacheExpiry} que aplica o TTL correspondente.
 *
 * @author Rodrigo Izidoro
 * @since 2025-10-07
 * @see DashboardCacheExpiry
 * @see CacheConfig#caffeineConfigDashboard()
 */
@Component("dashboardCacheKeyGenerator")
public class DashboardCacheKeyGenerator implements KeyGenerator {

  /** Delimitador principal entre componentes da chave de cache. */
  private static final String DELIMITER = "::";

  /** Separador entre nome de tipo e valor do parâmetro. */
  private static final String TYPE_SEPARATOR = ":";

  /** Representação de valor nulo na chave de cache. */
  private static final String NULL_VALUE = "NULL";

  /** Sufixo para dados históricos (dispara TTL longo no DashboardCacheExpiry). */
  private static final String HISTORICAL_SUFFIX = "_HISTORICAL";

  /** Sufixo para dados atuais/futuros (dispara TTL curto no DashboardCacheExpiry). */
  private static final String CURRENT_SUFFIX = "_CURRENT";

  @Override
  public Object generate(Object target, Method method, Object... params) {
    StringBuilder key = new StringBuilder();
    key.append(method.getName());

    // Adiciona parâmetros à chave com delimitador estruturado para evitar colisões
    // Exemplo: "findData::LocalDate:2025-01-01::LocalDate:2025-10-07"
    // vs antigo: "findData_2025-01-01_2025-10-07" (poderia colidir com "2025-01-012025-10-07")
    for (int i = 0; i < params.length; i++) {
      key.append(DELIMITER); // Delimitador claro e único
      if (params[i] == null) {
        key.append(NULL_VALUE);
      } else {
        // Adiciona prefixo de tipo para prevenir colisões entre tipos diferentes
        key.append(params[i].getClass().getSimpleName()).append(TYPE_SEPARATOR);
        key.append(params[i].toString());
      }
    }

    // Adiciona sufixo que determina o TTL (processado por DashboardCacheExpiry)
    if (params.length >= 2 && params[1] instanceof LocalDate dtFim) {
      LocalDate hoje = LocalDate.now();

      if (dtFim.isBefore(hoje)) {
        // Dados históricos - sufixo dispara TTL longo (6h) no DashboardCacheExpiry
        key.append(HISTORICAL_SUFFIX);
      } else {
        // Inclui data atual ou futura - sufixo dispara TTL curto (5 min) no DashboardCacheExpiry
        key.append(CURRENT_SUFFIX);
      }
    }

    return key.toString();
  }
}
