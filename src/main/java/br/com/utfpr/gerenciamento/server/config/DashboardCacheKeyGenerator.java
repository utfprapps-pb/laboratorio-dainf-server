package br.com.utfpr.gerenciamento.server.config;

import java.lang.reflect.Method;
import java.time.LocalDate;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

/**
 * Gerador de chave de cache inteligente para Dashboard.
 *
 * <p>Estratégia baseada na data da query: - Queries com data "até hoje": TTL curto (5 min) - dados
 * podem mudar - Queries com datas passadas: TTL longo (6 horas) - dados históricos não mudam
 *
 * <p>Exemplo: - findDadosEmprestimoCountRange(2025-01-01, 2025-10-07) → TTL curto (hoje está no
 * range) - findDadosEmprestimoCountRange(2024-01-01, 2024-12-31) → TTL longo (dados históricos)
 *
 * <p>Formato de data brasileiro: dd/MM/yyyy (padrão pt-BR)
 *
 * @author Rodrigo Izidoro
 * @since 2025-10-07
 */
@Component("dashboardCacheKeyGenerator")
public class DashboardCacheKeyGenerator implements KeyGenerator {

  @Override
  public Object generate(Object target, Method method, Object... params) {
    StringBuilder key = new StringBuilder();
    key.append(method.getName());

    // Adiciona parametros à chave
    for (Object param : params) {
      key.append("_");
      if (param == null) {
        key.append("null");
      } else {
        key.append(param.toString());
      }
    }

    // Adiciona marcador de TTL baseado na data final
    if (params.length >= 2 && params[1] instanceof LocalDate dtFim) {
      LocalDate hoje = LocalDate.now();

      if (dtFim.isBefore(hoje)) {
        // Dados históricos - pode usar TTL longo
        key.append("_HISTORICAL");
      } else {
        // Inclui data atual - usa TTL curto
        key.append("_CURRENT");
      }
    }

    return key.toString();
  }
}
