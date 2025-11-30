package br.com.utfpr.gerenciamento.server.audit.service;

import br.com.utfpr.gerenciamento.server.audit.AuditRevision;
import br.com.utfpr.gerenciamento.server.audit.dto.AuditEntryDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço para consultar histórico de auditoria via Hibernate Envers.
 *
 * <p>Todas as operações são read-only e executadas em transação para garantir snapshot consistente
 * dos dados de auditoria.
 *
 * @author Rodrigo Izidoro
 * @see AuditRevision
 * @see AuditEntryDto
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditService {

  private static final ZoneId BRAZIL_ZONE = ZoneId.of("America/Sao_Paulo");

  private static final Set<String> SENSITIVE_FIELDS =
      Set.of("password", "codigoVerificacao", "documento", "telefone");

  @PersistenceContext private EntityManager entityManager;

  private final ObjectMapper objectMapper;

  /**
   * Obtém o histórico de revisões de uma entidade com paginação.
   *
   * @param <T> tipo da entidade
   * @param entityClass classe da entidade
   * @param id identificador da entidade
   * @param pageable configuração de paginação
   * @return página de entradas de auditoria
   */
  @SuppressWarnings("unchecked")
  public <T> Page<AuditEntryDto> getHistoricoPaginado(
      Class<T> entityClass, Long id, Pageable pageable) {
    AuditReader reader = AuditReaderFactory.get(entityManager);

    long total = contarRevisoes(entityClass, id);

    if (total == 0) {
      return Page.empty(pageable);
    }

    List<Object[]> resultados =
        reader
            .createQuery()
            .forRevisionsOfEntity(entityClass, false, true)
            .add(AuditEntity.id().eq(id))
            .addOrder(AuditEntity.revisionNumber().desc())
            .setFirstResult((int) pageable.getOffset())
            .setMaxResults(pageable.getPageSize())
            .getResultList();

    List<AuditEntryDto> historico = processarResultados(resultados, entityClass);
    return new PageImpl<>(historico, pageable, total);
  }

  /**
   * Obtém o número total de revisões de uma entidade.
   *
   * <p>Usa projeção COUNT para evitar carregar todos os IDs de revisão na memória.
   *
   * @param <T> tipo da entidade
   * @param entityClass classe da entidade
   * @param id identificador da entidade
   * @return número total de revisões
   */
  public <T> long contarRevisoes(Class<T> entityClass, Long id) {
    AuditReader reader = AuditReaderFactory.get(entityManager);

    Number count =
        (Number)
            reader
                .createQuery()
                .forRevisionsOfEntity(entityClass, false, false)
                .add(AuditEntity.id().eq(id))
                .addProjection(AuditEntity.revisionNumber().count())
                .getSingleResult();

    return count != null ? count.longValue() : 0L;
  }

  /**
   * Obtém o estado da entidade em uma revisão específica.
   *
   * @param <T> tipo da entidade
   * @param entityClass classe da entidade
   * @param id identificador da entidade
   * @param revisao número da revisão
   * @return estado da entidade naquela revisão, ou null se não encontrada
   */
  public <T> T getEstadoEmRevisao(Class<T> entityClass, Long id, Long revisao) {
    AuditReader reader = AuditReaderFactory.get(entityManager);
    return reader.find(entityClass, id, revisao);
  }

  /**
   * Obtém o estado da entidade em um momento específico.
   *
   * @param <T> tipo da entidade
   * @param entityClass classe da entidade
   * @param id identificador da entidade
   * @param dataHora momento para consulta
   * @return estado da entidade naquele momento, ou null se não existia
   */
  public <T> T getEstadoEm(Class<T> entityClass, Long id, LocalDateTime dataHora) {
    AuditReader reader = AuditReaderFactory.get(entityManager);
    Date date = Date.from(dataHora.atZone(BRAZIL_ZONE).toInstant());

    Number revision = reader.getRevisionNumberForDate(date);
    if (revision == null) {
      return null;
    }

    return reader.find(entityClass, id, revision);
  }

  /**
   * Obtém informações de uma revisão específica.
   *
   * @param revisao número da revisão
   * @return informações da revisão, ou null se não encontrada
   */
  public AuditEntryDto getRevisao(Long revisao) {
    AuditReader reader = AuditReaderFactory.get(entityManager);
    AuditRevision revInfo = reader.findRevision(AuditRevision.class, revisao);

    if (revInfo == null) {
      return null;
    }

    return AuditEntryDto.builder()
        .revisao(revisao)
        .dataHora(timestampToLocalDateTime(revInfo.getTimestamp()))
        .usuario(revInfo.getUsuario())
        .ip(revInfo.getIp())
        .build();
  }

  @SuppressWarnings("unchecked")
  private <T> List<AuditEntryDto> processarResultados(
      List<Object[]> resultados, Class<T> entityClass) {
    List<AuditEntryDto> historico = new ArrayList<>();

    for (Object[] resultado : resultados) {
      try {
        T entidade = (T) resultado[0];
        AuditRevision revInfo = (AuditRevision) resultado[1];
        RevisionType tipo = (RevisionType) resultado[2];

        AuditEntryDto entry =
            AuditEntryDto.builder()
                .revisao(revInfo.getId())
                .dataHora(timestampToLocalDateTime(revInfo.getTimestamp()))
                .usuario(revInfo.getUsuario())
                .ip(revInfo.getIp())
                .tipoOperacao(traduzirTipo(tipo))
                .entidade(entityToMap(entidade))
                .build();

        historico.add(entry);
      } catch (ClassCastException e) {
        log.warn("Revisão corrompida para {}: {}", entityClass.getSimpleName(), e.getMessage());
      } catch (Exception e) {
        log.error("Erro crítico ao processar revisão para {}: ", entityClass.getSimpleName(), e);
      }
    }

    return historico;
  }

  private String traduzirTipo(RevisionType tipo) {
    return switch (tipo) {
      case ADD -> "CRIACAO";
      case MOD -> "ALTERACAO";
      case DEL -> "EXCLUSAO";
    };
  }

  private LocalDateTime timestampToLocalDateTime(Long timestamp) {
    return Instant.ofEpochMilli(timestamp).atZone(BRAZIL_ZONE).toLocalDateTime();
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> entityToMap(Object entity) {
    if (entity == null) {
      return Collections.emptyMap();
    }

    try {
      Map<String, Object> map = objectMapper.convertValue(entity, Map.class);

      SENSITIVE_FIELDS.forEach(map::remove);

      map.entrySet()
          .removeIf(
              entry -> {
                Object value = entry.getValue();
                return value != null && value.getClass().getName().contains("HibernateProxy");
              });

      return map;
    } catch (IllegalArgumentException e) {
      log.warn("Erro ao converter entidade para Map: {}", e.getMessage());
      return Collections.emptyMap();
    }
  }
}
