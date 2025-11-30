package br.com.utfpr.gerenciamento.server.audit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.audit.AuditRevision;
import br.com.utfpr.gerenciamento.server.audit.dto.AuditEntryDto;
import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.envers.query.AuditQueryCreator;
import org.hibernate.envers.query.criteria.AuditCriterion;
import org.hibernate.envers.query.order.AuditOrder;
import org.hibernate.envers.query.projection.AuditProjection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Testes unitários para AuditService.
 *
 * <p>Testa as funcionalidades de consulta de histórico de auditoria via Hibernate Envers.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuditService")
class AuditServiceTest {

  @Mock private EntityManager entityManager;

  @Mock private AuditReader auditReader;

  private AuditService auditService;

  private MockedStatic<AuditReaderFactory> auditReaderFactoryMock;

  @BeforeEach
  void setUp() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    auditService = new AuditService(objectMapper);

    Field entityManagerField = AuditService.class.getDeclaredField("entityManager");
    entityManagerField.setAccessible(true);
    entityManagerField.set(auditService, entityManager);

    auditReaderFactoryMock = mockStatic(AuditReaderFactory.class);
    auditReaderFactoryMock
        .when(() -> AuditReaderFactory.get(entityManager))
        .thenReturn(auditReader);
  }

  @AfterEach
  void tearDown() {
    auditReaderFactoryMock.close();
  }

  private void mockContarRevisoes(long count) {
    AuditQuery countQuery = mock(AuditQuery.class);
    AuditQueryCreator countQueryCreator = mock(AuditQueryCreator.class);

    when(auditReader.createQuery()).thenReturn(countQueryCreator);
    when(countQueryCreator.forRevisionsOfEntity(any(), eq(false), eq(false)))
        .thenReturn(countQuery);
    when(countQuery.add(any(AuditCriterion.class))).thenReturn(countQuery);
    when(countQuery.addProjection(any(AuditProjection.class))).thenReturn(countQuery);
    when(countQuery.getSingleResult()).thenReturn(count);
  }

  @Nested
  @DisplayName("getHistoricoPaginado")
  class GetHistoricoPaginado {

    @Test
    @DisplayName("Deve retornar página vazia quando não há revisões")
    void deveRetornarPaginaVaziaQuandoNaoHaRevisoes() {
      // Arrange
      mockContarRevisoes(0L);

      // Act
      Page<AuditEntryDto> resultado =
          auditService.getHistoricoPaginado(Emprestimo.class, 1L, PageRequest.of(0, 20));

      // Assert
      assertThat(resultado).isEmpty();
      assertThat(resultado.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("Deve retornar histórico paginado com uma revisão")
    void deveRetornarHistoricoPaginadoComUmaRevisao() {
      // Arrange
      Long entityId = 1L;
      long timestamp = System.currentTimeMillis();

      Emprestimo emprestimo = new Emprestimo();
      emprestimo.setId(entityId);
      emprestimo.setObservacao("Teste de empréstimo");

      AuditRevision revInfo = new AuditRevision();
      revInfo.setId(10L);
      revInfo.setTimestamp(timestamp);
      revInfo.setUsuario("admin");
      revInfo.setIp("192.168.1.1");

      // Mock para contarRevisoes (primeira chamada)
      AuditQuery countQuery = mock(AuditQuery.class);
      AuditQueryCreator queryCreator = mock(AuditQueryCreator.class);

      when(auditReader.createQuery()).thenReturn(queryCreator);
      when(queryCreator.forRevisionsOfEntity(Emprestimo.class, false, false))
          .thenReturn(countQuery);
      when(countQuery.add(any(AuditCriterion.class))).thenReturn(countQuery);
      when(countQuery.addProjection(any(AuditProjection.class))).thenReturn(countQuery);
      when(countQuery.getSingleResult()).thenReturn(1L);

      // Mock para query de dados (segunda chamada)
      AuditQuery dataQuery = mock(AuditQuery.class);
      when(queryCreator.forRevisionsOfEntity(Emprestimo.class, false, true)).thenReturn(dataQuery);
      when(dataQuery.add(any(AuditCriterion.class))).thenReturn(dataQuery);
      when(dataQuery.addOrder(any(AuditOrder.class))).thenReturn(dataQuery);
      when(dataQuery.setFirstResult(anyInt())).thenReturn(dataQuery);
      when(dataQuery.setMaxResults(anyInt())).thenReturn(dataQuery);

      List<Object[]> resultList = new ArrayList<>();
      resultList.add(new Object[] {emprestimo, revInfo, RevisionType.ADD});
      when(dataQuery.getResultList()).thenReturn(resultList);

      // Act
      Page<AuditEntryDto> resultado =
          auditService.getHistoricoPaginado(Emprestimo.class, entityId, PageRequest.of(0, 20));

      // Assert
      assertThat(resultado.getContent()).hasSize(1);
      assertThat(resultado.getTotalElements()).isEqualTo(1);
      assertThat(resultado.getContent().getFirst().getRevisao()).isEqualTo(10L);
      assertThat(resultado.getContent().getFirst().getUsuario()).isEqualTo("admin");
      assertThat(resultado.getContent().getFirst().getIp()).isEqualTo("192.168.1.1");
      assertThat(resultado.getContent().getFirst().getTipoOperacao()).isEqualTo("CRIACAO");
    }

    @Test
    @DisplayName("Deve traduzir tipos de operação corretamente")
    void deveTraduzirTiposDeOperacaoCorretamente() {
      // Arrange
      Long entityId = 1L;
      long timestamp = System.currentTimeMillis();

      Emprestimo emprestimo = new Emprestimo();
      emprestimo.setId(entityId);

      AuditRevision revInfo1 = criarRevInfo(1L, timestamp, "user");
      AuditRevision revInfo2 = criarRevInfo(2L, timestamp, "user");
      AuditRevision revInfo3 = criarRevInfo(3L, timestamp, "user");

      // Mock para contarRevisoes
      AuditQuery countQuery = mock(AuditQuery.class);
      AuditQueryCreator queryCreator = mock(AuditQueryCreator.class);

      when(auditReader.createQuery()).thenReturn(queryCreator);
      when(queryCreator.forRevisionsOfEntity(Emprestimo.class, false, false))
          .thenReturn(countQuery);
      when(countQuery.add(any(AuditCriterion.class))).thenReturn(countQuery);
      when(countQuery.addProjection(any(AuditProjection.class))).thenReturn(countQuery);
      when(countQuery.getSingleResult()).thenReturn(3L);

      // Mock para query de dados
      AuditQuery dataQuery = mock(AuditQuery.class);
      when(queryCreator.forRevisionsOfEntity(Emprestimo.class, false, true)).thenReturn(dataQuery);
      when(dataQuery.add(any(AuditCriterion.class))).thenReturn(dataQuery);
      when(dataQuery.addOrder(any(AuditOrder.class))).thenReturn(dataQuery);
      when(dataQuery.setFirstResult(anyInt())).thenReturn(dataQuery);
      when(dataQuery.setMaxResults(anyInt())).thenReturn(dataQuery);

      List<Object[]> resultList = new ArrayList<>();
      resultList.add(new Object[] {emprestimo, revInfo1, RevisionType.ADD});
      resultList.add(new Object[] {emprestimo, revInfo2, RevisionType.MOD});
      resultList.add(new Object[] {emprestimo, revInfo3, RevisionType.DEL});
      when(dataQuery.getResultList()).thenReturn(resultList);

      // Act
      Page<AuditEntryDto> resultado =
          auditService.getHistoricoPaginado(Emprestimo.class, entityId, PageRequest.of(0, 20));

      // Assert
      assertThat(resultado.getContent()).hasSize(3);
      assertThat(resultado.getContent().get(0).getTipoOperacao()).isEqualTo("CRIACAO");
      assertThat(resultado.getContent().get(1).getTipoOperacao()).isEqualTo("ALTERACAO");
      assertThat(resultado.getContent().get(2).getTipoOperacao()).isEqualTo("EXCLUSAO");
    }

    private AuditRevision criarRevInfo(Long id, long timestamp, String usuario) {
      AuditRevision revInfo = new AuditRevision();
      revInfo.setId(id);
      revInfo.setTimestamp(timestamp);
      revInfo.setUsuario(usuario);
      return revInfo;
    }
  }

  @Nested
  @DisplayName("contarRevisoes")
  class ContarRevisoes {

    @Test
    @DisplayName("Deve retornar zero quando não há revisões")
    void deveRetornarZeroQuandoNaoHaRevisoes() {
      // Arrange
      AuditQuery countQuery = mock(AuditQuery.class);
      AuditQueryCreator queryCreator = mock(AuditQueryCreator.class);

      when(auditReader.createQuery()).thenReturn(queryCreator);
      when(queryCreator.forRevisionsOfEntity(Emprestimo.class, false, false))
          .thenReturn(countQuery);
      when(countQuery.add(any(AuditCriterion.class))).thenReturn(countQuery);
      when(countQuery.addProjection(any(AuditProjection.class))).thenReturn(countQuery);
      when(countQuery.getSingleResult()).thenReturn(0L);

      // Act
      long resultado = auditService.contarRevisoes(Emprestimo.class, 1L);

      // Assert
      assertThat(resultado).isZero();
    }

    @Test
    @DisplayName("Deve retornar contagem correta de revisões")
    void deveRetornarContagemCorretaDeRevisoes() {
      // Arrange
      AuditQuery countQuery = mock(AuditQuery.class);
      AuditQueryCreator queryCreator = mock(AuditQueryCreator.class);

      when(auditReader.createQuery()).thenReturn(queryCreator);
      when(queryCreator.forRevisionsOfEntity(Emprestimo.class, false, false))
          .thenReturn(countQuery);
      when(countQuery.add(any(AuditCriterion.class))).thenReturn(countQuery);
      when(countQuery.addProjection(any(AuditProjection.class))).thenReturn(countQuery);
      when(countQuery.getSingleResult()).thenReturn(5L);

      // Act
      long resultado = auditService.contarRevisoes(Emprestimo.class, 1L);

      // Assert
      assertThat(resultado).isEqualTo(5);
    }

    @Test
    @DisplayName("Deve retornar zero quando resultado é null")
    void deveRetornarZeroQuandoResultadoEhNull() {
      // Arrange
      AuditQuery countQuery = mock(AuditQuery.class);
      AuditQueryCreator queryCreator = mock(AuditQueryCreator.class);

      when(auditReader.createQuery()).thenReturn(queryCreator);
      when(queryCreator.forRevisionsOfEntity(Emprestimo.class, false, false))
          .thenReturn(countQuery);
      when(countQuery.add(any(AuditCriterion.class))).thenReturn(countQuery);
      when(countQuery.addProjection(any(AuditProjection.class))).thenReturn(countQuery);
      when(countQuery.getSingleResult()).thenReturn(null);

      // Act
      long resultado = auditService.contarRevisoes(Emprestimo.class, 1L);

      // Assert
      assertThat(resultado).isZero();
    }
  }

  @Nested
  @DisplayName("getEstadoEmRevisao")
  class GetEstadoEmRevisao {

    @Test
    @DisplayName("Deve retornar estado da entidade na revisão especificada")
    void deveRetornarEstadoDaEntidadeNaRevisaoEspecificada() {
      // Arrange
      Emprestimo emprestimo = new Emprestimo();
      emprestimo.setId(1L);
      emprestimo.setObservacao("Estado na revisão 5");

      when(auditReader.find(Emprestimo.class, 1L, 5L)).thenReturn(emprestimo);

      // Act
      Emprestimo resultado = auditService.getEstadoEmRevisao(Emprestimo.class, 1L, 5L);

      // Assert
      assertThat(resultado).isNotNull();
      assertThat(resultado.getObservacao()).isEqualTo("Estado na revisão 5");
    }

    @Test
    @DisplayName("Deve retornar null quando entidade não existe na revisão")
    void deveRetornarNullQuandoEntidadeNaoExisteNaRevisao() {
      // Arrange
      when(auditReader.find(Emprestimo.class, 999L, 1L)).thenReturn(null);

      // Act
      Emprestimo resultado = auditService.getEstadoEmRevisao(Emprestimo.class, 999L, 1L);

      // Assert
      assertThat(resultado).isNull();
    }
  }

  @Nested
  @DisplayName("getEstadoEm")
  class GetEstadoEm {

    @Test
    @DisplayName("Deve retornar estado da entidade em momento específico")
    void deveRetornarEstadoDaEntidadeEmMomentoEspecifico() {
      // Arrange
      LocalDateTime dataHora = LocalDateTime.of(2025, 6, 15, 14, 30);

      Emprestimo emprestimo = new Emprestimo();
      emprestimo.setId(1L);
      emprestimo.setObservacao("Estado em 15/06/2025");

      when(auditReader.getRevisionNumberForDate(any(Date.class))).thenReturn(3L);
      when(auditReader.find(Emprestimo.class, 1L, 3L)).thenReturn(emprestimo);

      // Act
      Emprestimo resultado = auditService.getEstadoEm(Emprestimo.class, 1L, dataHora);

      // Assert
      assertThat(resultado).isNotNull();
      assertThat(resultado.getObservacao()).isEqualTo("Estado em 15/06/2025");
    }

    @Test
    @DisplayName("Deve retornar null quando não há revisão para a data")
    void deveRetornarNullQuandoNaoHaRevisaoParaData() {
      // Arrange
      LocalDateTime dataHora = LocalDateTime.of(2020, 1, 1, 0, 0);

      when(auditReader.getRevisionNumberForDate(any(Date.class))).thenReturn(null);

      // Act
      Emprestimo resultado = auditService.getEstadoEm(Emprestimo.class, 1L, dataHora);

      // Assert
      assertThat(resultado).isNull();
    }
  }

  @Nested
  @DisplayName("getRevisao")
  class GetRevisao {

    @Test
    @DisplayName("Deve retornar informações da revisão")
    void deveRetornarInformacoesDaRevisao() {
      // Arrange
      AuditRevision revInfo = new AuditRevision();
      revInfo.setId(10L);
      revInfo.setTimestamp(System.currentTimeMillis());
      revInfo.setUsuario("maria.silva");
      revInfo.setIp("10.0.0.100");

      when(auditReader.findRevision(AuditRevision.class, 10L)).thenReturn(revInfo);

      // Act
      AuditEntryDto resultado = auditService.getRevisao(10L);

      // Assert
      assertThat(resultado).isNotNull();
      assertThat(resultado.getRevisao()).isEqualTo(10L);
      assertThat(resultado.getUsuario()).isEqualTo("maria.silva");
      assertThat(resultado.getIp()).isEqualTo("10.0.0.100");
    }

    @Test
    @DisplayName("Deve retornar null para revisão inexistente")
    void deveRetornarNullParaRevisaoInexistente() {
      // Arrange
      when(auditReader.findRevision(AuditRevision.class, 999L)).thenReturn(null);

      // Act
      AuditEntryDto resultado = auditService.getRevisao(999L);

      // Assert
      assertThat(resultado).isNull();
    }
  }

  @Nested
  @DisplayName("entityToMap - Filtragem de campos sensíveis")
  class EntityToMapSensitiveFields {

    /**
     * POJO simples para testes de entityToMap. Usa campos com os mesmos nomes que as entidades
     * reais para testar a filtragem de campos sensíveis sem as complexidades de serialização JPA.
     */
    @Setter
    @Getter
    static class TestEntity {
      private Long id;
      private String nome;
      private String email;
      private String password;
      private String codigoVerificacao;
      private String documento;
      private String telefone;
    }

    @Test
    @DisplayName("Deve remover campo password do mapa")
    void deveRemoverCampoPasswordDoMapa() throws Exception {
      // Arrange
      TestEntity entity = new TestEntity();
      entity.setId(1L);
      entity.setNome("Test User");
      entity.setEmail("test@test.com");
      entity.setPassword("senha_secreta_123");

      // Act
      Map<String, Object> resultado = invocarEntityToMap(entity);

      // Assert
      assertThat(resultado).doesNotContainKey("password").containsKey("nome").containsKey("email");
    }

    @Test
    @DisplayName("Deve remover campo codigoVerificacao do mapa")
    void deveRemoverCampoCodigoVerificacaoDoMapa() throws Exception {
      // Arrange
      TestEntity entity = new TestEntity();
      entity.setId(1L);
      entity.setNome("Test User");
      entity.setCodigoVerificacao("abc123xyz");

      // Act
      Map<String, Object> resultado = invocarEntityToMap(entity);

      // Assert
      assertThat(resultado).doesNotContainKey("codigoVerificacao").containsKey("nome");
    }

    @Test
    @DisplayName("Deve remover campo documento do mapa")
    void deveRemoverCampoDocumentoDoMapa() throws Exception {
      // Arrange
      TestEntity entity = new TestEntity();
      entity.setId(1L);
      entity.setNome("Test User");
      entity.setDocumento("12345678900");

      // Act
      Map<String, Object> resultado = invocarEntityToMap(entity);

      // Assert
      assertThat(resultado).doesNotContainKey("documento").containsKey("nome");
    }

    @Test
    @DisplayName("Deve remover campo telefone do mapa")
    void deveRemoverCampoTelefoneDoMapa() throws Exception {
      // Arrange
      TestEntity entity = new TestEntity();
      entity.setId(1L);
      entity.setNome("Test User");
      entity.setTelefone("11999999999");

      // Act
      Map<String, Object> resultado = invocarEntityToMap(entity);

      // Assert
      assertThat(resultado).doesNotContainKey("telefone").containsKey("nome");
    }

    @Test
    @DisplayName("Deve remover todos os campos sensíveis simultaneamente")
    void deveRemoverTodosOsCamposSensiveisSimultaneamente() throws Exception {
      // Arrange
      TestEntity entity = new TestEntity();
      entity.setId(1L);
      entity.setNome("Test User");
      entity.setEmail("test@test.com");
      entity.setPassword("senha123");
      entity.setCodigoVerificacao("code123");
      entity.setDocumento("12345678900");
      entity.setTelefone("11999999999");

      // Act
      Map<String, Object> resultado = invocarEntityToMap(entity);

      // Assert
      assertThat(resultado)
          .doesNotContainKey("password")
          .doesNotContainKey("codigoVerificacao")
          .doesNotContainKey("documento")
          .doesNotContainKey("telefone")
          .containsKey("nome")
          .containsKey("email")
          .containsKey("id");
    }

    @Test
    @DisplayName("Deve retornar mapa vazio para entidade null")
    void deveRetornarMapaVazioParaEntidadeNull() throws Exception {
      // Act
      Map<String, Object> resultado = invocarEntityToMap(null);

      // Assert
      assertThat(resultado).isEmpty();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> invocarEntityToMap(Object entity) throws Exception {
      Method method = AuditService.class.getDeclaredMethod("entityToMap", Object.class);
      method.setAccessible(true);
      return (Map<String, Object>) method.invoke(auditService, entity);
    }
  }
}
