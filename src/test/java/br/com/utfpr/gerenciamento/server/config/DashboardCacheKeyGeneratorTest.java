package br.com.utfpr.gerenciamento.server.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testes para DashboardCacheKeyGenerator - Gerador inteligente de chaves.
 *
 * @author Rodrigo Izidoro
 * @since 2025-10-07
 */
class DashboardCacheKeyGeneratorTest {

  private DashboardCacheKeyGenerator generator;
  private Object mockService;
  private Method metodoMock;
  private Method metodoSemParams;
  private Method metodoComString;
  private Method metodoComList;

  @BeforeEach
  void setUp() throws NoSuchMethodException {
    generator = new DashboardCacheKeyGenerator();

    // Criar mock usando Mockito
    mockService = mock(DashboardServiceInterface.class);

    // Obter métodos da interface para testes de reflexão
    metodoMock =
        DashboardServiceInterface.class.getMethod(
            "findDadosEmprestimoCountRange", LocalDate.class, LocalDate.class);

    metodoSemParams = DashboardServiceInterface.class.getMethod("countTotal");

    metodoComString = DashboardServiceInterface.class.getMethod("findByName", String.class);

    metodoComList = DashboardServiceInterface.class.getMethod("findByIds", List.class);
  }

  @Test
  @DisplayName("Deve gerar chave com sufixo _HISTORICAL para datas passadas")
  void testGenerate_QuandoDatasPassadas_DeveRetornarSufixoHistorical() {
    // Given
    LocalDate dtInicio = LocalDate.of(2024, 1, 1);
    LocalDate dtFim = LocalDate.of(2024, 12, 31);

    // When
    Object chave = generator.generate(mockService, metodoMock, dtInicio, dtFim);

    // Then
    String chaveStr = chave.toString();
    assertTrue(chaveStr.contains("findDadosEmprestimoCountRange"));
    assertTrue(chaveStr.contains("2024-01-01"));
    assertTrue(chaveStr.contains("2024-12-31"));
    assertTrue(chaveStr.endsWith("_HISTORICAL"));
  }

  @Test
  @DisplayName("Deve gerar chave com sufixo _CURRENT para data atual")
  void testGenerate_QuandoDataAtual_DeveRetornarSufixoCurrent() {
    // Given
    LocalDate hoje = LocalDate.now();
    LocalDate dtInicio = hoje.minusDays(7);

    // When
    Object chave = generator.generate(mockService, metodoMock, dtInicio, hoje);

    // Then
    String chaveStr = chave.toString();
    assertTrue(chaveStr.endsWith("_CURRENT"));
  }

  @Test
  @DisplayName("Deve gerar chave com sufixo _CURRENT para datas futuras")
  void testGenerate_QuandoDatasFuturas_DeveRetornarSufixoCurrent() {
    // Given
    LocalDate amanha = LocalDate.now().plusDays(1);
    LocalDate semanaQueVem = LocalDate.now().plusDays(7);

    // When
    Object chave = generator.generate(mockService, metodoMock, amanha, semanaQueVem);

    // Then
    String chaveStr = chave.toString();
    assertTrue(chaveStr.endsWith("_CURRENT"));
  }

  @Test
  @DisplayName("Deve gerar chave _CURRENT quando dtFim é exatamente hoje")
  void testGenerate_QuandoDtFimEhHoje_DeveRetornarSufixoCurrent() {
    // Given
    LocalDate hoje = LocalDate.now();
    LocalDate ontem = hoje.minusDays(1);

    // When
    Object chave = generator.generate(mockService, metodoMock, ontem, hoje);

    // Then
    String chaveStr = chave.toString();

    // dtFim.isBefore(hoje) == false, portanto deve ser CURRENT
    assertTrue(chaveStr.endsWith("_CURRENT"));
  }

  @Test
  @DisplayName("Deve gerar chave _HISTORICAL quando dtFim é ontem")
  void testGenerate_QuandoDtFimEhOntem_DeveRetornarSufixoHistorical() {
    // Given
    LocalDate ontem = LocalDate.now().minusDays(1);
    LocalDate semanaPassada = ontem.minusDays(7);

    // When
    Object chave = generator.generate(mockService, metodoMock, semanaPassada, ontem);

    // Then
    String chaveStr = chave.toString();
    assertTrue(chaveStr.endsWith("_HISTORICAL"));
  }

  @Test
  @DisplayName("Deve gerar chave com 'null' quando parâmetro é nulo")
  void testGenerate_QuandoParametroNulo_DeveIncluirNullNaChave() {
    // Given
    Object param1 = null;
    Object param2 = "valor";

    // When
    Object chave = generator.generate(mockService, metodoMock, param1, param2);

    // Then
    String chaveStr = chave.toString();
    assertTrue(chaveStr.contains("::NULL"));
    assertTrue(chaveStr.contains("::String:valor"));
  }

  @Test
  @DisplayName("Deve gerar chave apenas com nome do método quando sem parâmetros")
  void testGenerate_QuandoSemParametros_DeveRetornarApenasNomeMetodo() {
    // Given

    // When
    Object chave = generator.generate(mockService, metodoSemParams);

    // Then
    String chaveStr = chave.toString();
    assertEquals("countTotal", chaveStr);
  }

  @Test
  @DisplayName("Não deve adicionar sufixo quando segundo parâmetro não é LocalDate")
  void testGenerate_QuandoSegundoParamNaoEhLocalDate_NaoDeveAdicionarSufixo() {
    // Given
    String nome = "teste";

    // When
    Object chave = generator.generate(mockService, metodoComString, nome);

    // Then
    String chaveStr = chave.toString();
    assertFalse(chaveStr.endsWith("_HISTORICAL"));
    assertFalse(chaveStr.endsWith("_CURRENT"));
    assertEquals("findByName::String:teste", chaveStr);
  }

  @Test
  @DisplayName("Deve incluir data no formato ISO 8601 na chave (yyyy-MM-dd)")
  void testGenerate_QuandoDatasInformadas_DeveIncluirFormatoISO8601() {
    // Given
    LocalDate dtInicio = LocalDate.of(2025, 10, 7); // 07/10/2025 em pt-BR
    LocalDate dtFim = LocalDate.of(2025, 10, 15);

    // When
    Object chave = generator.generate(mockService, metodoMock, dtInicio, dtFim);

    // Then
    String chaveStr = chave.toString();
    assertTrue(chaveStr.contains("2025-10-07")); // ISO format
    assertTrue(chaveStr.contains("2025-10-15"));
  }

  @Test
  @DisplayName("Deve gerar chaves diferentes para parâmetros diferentes")
  void testGenerate_QuandoParametrosDiferentes_DeveRetornarChavesDiferentes() {
    // Given
    LocalDate dt1Inicio = LocalDate.of(2024, 1, 1);
    LocalDate dt1Fim = LocalDate.of(2024, 6, 30);

    LocalDate dt2Inicio = LocalDate.of(2024, 7, 1);
    LocalDate dt2Fim = LocalDate.of(2024, 12, 31);

    // When
    Object chave1 = generator.generate(mockService, metodoMock, dt1Inicio, dt1Fim);
    Object chave2 = generator.generate(mockService, metodoMock, dt2Inicio, dt2Fim);

    // Then
    assertNotEquals(chave1.toString(), chave2.toString());
  }

  @Test
  @DisplayName("Deve gerar mesma chave para mesmos parâmetros")
  void testGenerate_QuandoMesmosParametros_DeveRetornarMesmaChave() {
    // Given
    LocalDate dtInicio = LocalDate.of(2024, 1, 1);
    LocalDate dtFim = LocalDate.of(2024, 12, 31);

    // When
    Object chave1 = generator.generate(mockService, metodoMock, dtInicio, dtFim);
    Object chave2 = generator.generate(mockService, metodoMock, dtInicio, dtFim);

    // Then
    assertEquals(chave1.toString(), chave2.toString());
  }

  @Test
  @DisplayName("Deve incluir nome do método na chave")
  void testGenerate_QuandoChamado_DeveIncluirNomeMetodoNaChave() {
    // Given
    LocalDate dtInicio = LocalDate.of(2024, 1, 1);
    LocalDate dtFim = LocalDate.of(2024, 12, 31);

    // When
    Object chave = generator.generate(mockService, metodoMock, dtInicio, dtFim);

    // Then
    String chaveStr = chave.toString();
    assertTrue(chaveStr.startsWith("findDadosEmprestimoCountRange"));
  }

  @Test
  @DisplayName("Deve lidar com múltiplos parâmetros não-LocalDate")
  void testGenerate_QuandoMultiplosParametrosNaoLocalDate_DeveGerarChaveCorretamente() {
    // Given
    String param1 = "teste";

    // When
    Object chave = generator.generate(mockService, metodoComString, param1);

    // Then
    String chaveStr = chave.toString();
    assertNotNull(chaveStr);
    assertTrue(chaveStr.contains("findByName"));
    assertTrue(chaveStr.contains("teste"));
  }

  @Test
  @DisplayName("Deve gerar chaves determinísticas para mesmos parâmetros em threads diferentes")
  void testGenerate_QuandoThreadsConcorrentes_DeveGerarChavesDeterministicas()
      throws InterruptedException {
    // Given
    LocalDate dtInicio = LocalDate.of(2024, 1, 1);
    LocalDate dtFim = LocalDate.of(2024, 12, 31);

    Set<String> chaves = ConcurrentHashMap.newKeySet();
    int numThreads = 10;
    CountDownLatch latch = new CountDownLatch(numThreads);

    // When - Gerar chaves em paralelo
    for (int i = 0; i < numThreads; i++) {
      new Thread(
              () -> {
                try {
                  Object chave = generator.generate(mockService, metodoMock, dtInicio, dtFim);
                  chaves.add(chave.toString());
                } finally {
                  latch.countDown();
                }
              })
          .start();
    }

    boolean completedInTime = latch.await(5, TimeUnit.SECONDS);

    // Then - Todas as threads devem completar no tempo esperado
    assertTrue(
        completedInTime,
        "Threads de teste não completaram em 5 segundos - possível deadlock ou lentidão");

    // E todas as threads devem gerar a mesma chave
    assertEquals(1, chaves.size());
  }

  @Test
  @DisplayName("Deve lidar com parâmetros contendo caracteres especiais")
  void testGenerate_QuandoCaracteresEspeciais_DeveIncluirNaChave() {
    // Given
    String parametroComEspeciais = "teste_com-especiais@123.456";

    // When
    Object chave = generator.generate(mockService, metodoComString, parametroComEspeciais);

    // Then
    String chaveStr = chave.toString();
    assertTrue(chaveStr.contains("teste_com-especiais@123.456"));
    assertFalse(chaveStr.isEmpty());
  }

  @Test
  @DisplayName("Deve gerar chave com List como parâmetro")
  void testGenerate_QuandoParametroList_DeveGerarChaveComConteudoLista() {
    // Given
    List<Long> ids = List.of(1L, 2L, 3L);

    // When
    Object chave = generator.generate(mockService, metodoComList, ids);

    // Then
    String chaveStr = chave.toString();
    assertTrue(chaveStr.contains("findByIds"));
    assertTrue(chaveStr.contains("[1, 2, 3]"));
  }

  @Test
  @DisplayName("Deve gerar chaves diferentes quando ordem dos parâmetros muda")
  void testGenerate_QuandoOrdemParametrosMuda_DeveRetornarChavesDiferentes() {
    // Given
    LocalDate dt1 = LocalDate.of(2024, 1, 1);
    LocalDate dt2 = LocalDate.of(2024, 12, 31);

    // When
    Object chave1 = generator.generate(mockService, metodoMock, dt1, dt2);
    Object chave2 = generator.generate(mockService, metodoMock, dt2, dt1);

    // Then
    assertNotEquals(chave1.toString(), chave2.toString());
  }

  /**
   * Interface para definir assinaturas de métodos usados em testes de reflexão. Permite uso de
   * Mockito para criar mocks dinâmicos sem necessidade de implementação manual.
   */
  interface DashboardServiceInterface {
    void findDadosEmprestimoCountRange(LocalDate inicio, LocalDate fim);

    int countTotal();

    void findByName(String nome);

    void findByIds(List<Long> ids);
  }
}
