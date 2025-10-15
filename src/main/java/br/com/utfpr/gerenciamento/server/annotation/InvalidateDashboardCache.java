package br.com.utfpr.gerenciamento.server.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.annotation.AliasFor;

/**
 * Anotação composta para invalidação do cache de dashboard de empréstimos.
 *
 * <p>Combina {@link CacheEvict} com configurações padrão para evitar duplicação de código. Elimina
 * necessidade de repetir a mesma anotação múltiplas vezes (DRY principle).
 *
 * <p>Uso típico em métodos de serviço que modificam dados de empréstimos:
 *
 * <pre>{@code
 * @InvalidateDashboardCache
 * @Transactional
 * public Emprestimo save(Emprestimo entity) {
 *   // implementação
 * }
 * }</pre>
 *
 * <h3>Estratégia de Evicção: allEntries=true</h3>
 *
 * <p>Utiliza invalidação total do cache (allEntries=true) ao invés de evicção seletiva pelos
 * seguintes motivos:
 *
 * <ul>
 *   <li><b>Complexidade de chaves:</b> Cache usa ranges de datas (dtIni, dtFim), tornando difícil
 *       determinar quais entradas específicas invalidar
 *   <li><b>Baixa frequência:</b> Modificações de empréstimos não são operações de alta frequência
 *   <li><b>Simplicidade:</b> Evita lógica complexa de pattern matching para invalidação seletiva
 *   <li><b>TTL adaptativo:</b> Cache já usa TTLs diferentes (5min/6h) para otimizar retenção
 * </ul>
 *
 * <p><b>Quando migrar para evicção seletiva:</b> Se modificações de empréstimos superarem 100
 * ops/min e métricas mostrarem cache thrashing, considerar implementar evicção baseada em padrões
 * de chave (ex: invalidar apenas entradas onde dtIni <= dataEmprestimo <= dtFim).
 *
 * @author Rodrigo Izidoro
 * @since 2025-10-08
 * @see CacheEvict
 * @see br.com.utfpr.gerenciamento.server.config.DashboardCacheKeyGenerator
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@CacheEvict(value = "dashboard-emprestimo-range", allEntries = true)
public @interface InvalidateDashboardCache {

  /**
   * Alias para o atributo 'value' do {@link CacheEvict}.
   *
   * <p>Permite sobrescrever o nome do cache se necessário, embora o padrão seja suficiente para
   * maioria dos casos.
   *
   * @return nomes dos caches a invalidar
   */
  @AliasFor(annotation = CacheEvict.class, attribute = "value")
  String[] value() default {"dashboard-emprestimo-range"};

  /**
   * Alias para o atributo 'allEntries' do {@link CacheEvict}.
   *
   * <p>Por padrão invalida todas as entradas do cache. Pode ser alterado para false se precisar
   * evicção seletiva baseada em chaves específicas.
   *
   * @return true para invalidar todas as entradas (padrão), false para evicção seletiva
   */
  @AliasFor(annotation = CacheEvict.class, attribute = "allEntries")
  boolean allEntries() default true;
}
