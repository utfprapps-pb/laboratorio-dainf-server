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
 * <p>Exemplo: - findDadosEmprestimoCountRange(2025-01-01, 2025-10-07) → chave com "_CURRENT" → TTL
 * 5 min - findDadosEmprestimoCountRange(2024-01-01, 2024-12-31) → chave com "_HISTORICAL" → TTL 6h
 *
 * <p>O sufixo é lido por {@link DashboardCacheExpiry} que aplica o TTL correspondente.
 *
 * <p>Formato de data brasileiro: dd/MM/yyyy (padrão pt-BR)
 *
 * @author Rodrigo Izidoro
 * @since 2025-10-07
 * @see DashboardCacheExpiry
 * @see CacheConfig#caffeineConfigDashboard()
 */
@Component("dashboardCacheKeyGenerator")
public class DashboardCacheKeyGenerator implements KeyGenerator {

  @Override
  public Object generate(Object target, Method method, Object... params) {
    StringBuilder key = new StringBuilder();
    key.append(method.getName());

    // Adiciona parâmetros à chave
    for (Object param : params) {
      key.append("_");
      if (param == null) {
        key.append("null");
      } else {
        key.append(param.toString());
      }
    }

    // Adiciona sufixo que determina o TTL (processado por DashboardCacheExpiry)
    if (params.length >= 2 && params[1] instanceof LocalDate dtFim) {
      LocalDate hoje = LocalDate.now();

      if (dtFim.isBefore(hoje)) {
        // Dados históricos - sufixo dispara TTL longo (6h) no DashboardCacheExpiry
        key.append("_HISTORICAL");
      } else {
        // Inclui data atual ou futura - sufixo dispara TTL curto (5 min) no DashboardCacheExpiry
        key.append("_CURRENT");
      }
    }

    return key.toString();
  }
}
