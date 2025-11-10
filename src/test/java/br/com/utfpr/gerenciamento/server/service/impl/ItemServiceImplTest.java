package br.com.utfpr.gerenciamento.server.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.enumeration.TipoItem;
import br.com.utfpr.gerenciamento.server.minio.config.MinioConfig;
import br.com.utfpr.gerenciamento.server.minio.service.MinioService;
import br.com.utfpr.gerenciamento.server.model.Grupo;
import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.repository.EmprestimoItemRepository;
import br.com.utfpr.gerenciamento.server.repository.ItemImageRepository;
import br.com.utfpr.gerenciamento.server.repository.ItemRepository;
import br.com.utfpr.gerenciamento.server.repository.projection.ItemCompleteWithDisponibilidade;
import br.com.utfpr.gerenciamento.server.repository.projection.ItemWithQtdeEmprestada;
import br.com.utfpr.gerenciamento.server.service.EmailService;
import br.com.utfpr.gerenciamento.server.service.RelatorioService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

  @Mock private ItemRepository itemRepository;
  @Mock private EmailService emailService;
  @Mock private RelatorioService relatorioService;
  @Mock private MinioService minioService;
  @Mock private MinioConfig minioConfig;
  @Mock private ItemImageRepository itemImageRepository;
  @Mock private EmprestimoItemRepository emprestimoItemRepository;
  @Mock private ModelMapper modelMapper;
  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private ItemServiceImpl service;

  private Item item;

  @BeforeEach
  void setUp() {
    item = createItemPadrao();
  }

  @ParameterizedTest(name = "{index}: Saldo={0}, Emprestado={1} -> Disponibilidade={2} ({3})")
  @CsvSource({
    "10.00, 3.00, 7.00, Permanente com estoque positivo",
    "5.00, 5.00, 0.00, Permanente com estoque zerado",
    "2.00, 5.00, 0.00, Permanente com saldo negativo (limitado a zero)",
    "0.00, 3.00, 0.00, Permanente com saldo zero"
  })
  @DisplayName("Testa cálculo de disponibilidade para itens permanentes com múltiplos cenários")
  void testFindOneWithDisponibilidade_Permanente_Parametrizado(
      String saldo, String qtdeEmprestada, String disponibilidadeEsperada, String descricao) {
    // Arrange
    item.setTipoItem(TipoItem.P);
    item.setSaldo(new BigDecimal(saldo));

    ItemWithQtdeEmprestada projection =
        createItemWithQtdeEmprestada(item, new BigDecimal(qtdeEmprestada));
    when(itemRepository.findByIdWithQtdeEmprestada(1L)).thenReturn(Optional.of(projection));

    // Act
    Item resultItem = service.findOneWithDisponibilidade(1L);

    // Assert
    assertNotNull(resultItem, "Item resultante não deve ser nulo");
    assertEquals(
        new BigDecimal(qtdeEmprestada),
        resultItem.getQuantidadeEmprestada(),
        "Quantidade emprestada deve corresponder ao valor informado");
    assertEquals(
        0,
        new BigDecimal(disponibilidadeEsperada)
            .compareTo(resultItem.getDisponivelEmprestimoCalculado()),
        "Disponibilidade calculada deve corresponder ao valor esperado");
    verify(itemRepository, times(1)).findByIdWithQtdeEmprestada(1L);
  }

  @ParameterizedTest(
      name = "{index}: Saldo={0}, Emprestado={1} -> Sem cálculo de disponibilidade ({2})")
  @CsvSource({
    "100.00, 0.00, Consumível com saldo",
    "0.00, 0.00, Consumível zerado",
    "50.00, 10.00, Consumível com empréstimos (disponibilidade ignorada)"
  })
  @DisplayName("Testa itens consumíveis não têm cálculo de disponibilidade")
  void testFindOneWithDisponibilidade_Consumivel_Parametrizado(
      String saldo, String qtdeEmprestada, String descricao) {
    // Arrange
    item.setTipoItem(TipoItem.C);
    item.setSaldo(new BigDecimal(saldo));

    ItemWithQtdeEmprestada projection =
        createItemWithQtdeEmprestada(item, new BigDecimal(qtdeEmprestada));
    when(itemRepository.findByIdWithQtdeEmprestada(1L)).thenReturn(Optional.of(projection));

    // Act
    Item resultItem = service.findOneWithDisponibilidade(1L);

    // Assert
    assertNotNull(resultItem, "Item resultante não deve ser nulo");
    assertEquals(
        new BigDecimal(qtdeEmprestada),
        resultItem.getQuantidadeEmprestada(),
        "Quantidade emprestada deve corresponder ao valor informado");
    assertNull(
        resultItem.getDisponivelEmprestimoCalculado(),
        "Itens consumíveis não devem ter disponibilidade calculada");
    verify(itemRepository, times(1)).findByIdWithQtdeEmprestada(1L);
  }

  @Test
  void testFindOneWithDisponibilidade_ItemNaoEncontrado() {
    when(itemRepository.findByIdWithQtdeEmprestada(999L)).thenReturn(Optional.empty());

    assertThrows(
        br.com.utfpr.gerenciamento.server.exception.EntityNotFoundException.class,
        () -> service.findOneWithDisponibilidade(999L));
    verify(itemRepository, times(1)).findByIdWithQtdeEmprestada(999L);
  }

  @Test
  void testFindOneWithDisponibilidade_Permanente_QtdeEmprestadaNull() {
    item.setTipoItem(TipoItem.P);
    item.setSaldo(new BigDecimal("10.00"));

    ItemWithQtdeEmprestada projection = createItemWithQtdeEmprestada(item, null);
    when(itemRepository.findByIdWithQtdeEmprestada(1L)).thenReturn(Optional.of(projection));

    Item resultItem = service.findOneWithDisponibilidade(1L);

    assertNotNull(resultItem);
    assertEquals(0, resultItem.getQuantidadeEmprestada().compareTo(BigDecimal.ZERO));
    assertEquals(new BigDecimal("10.00"), resultItem.getDisponivelEmprestimoCalculado());
    verify(itemRepository, times(1)).findByIdWithQtdeEmprestada(1L);
  }

  @ParameterizedTest(name = "{index}: {1} itens abaixo do mínimo -> {2}")
  @CsvSource({
    "0, false, 'Sem itens abaixo do mínimo, não publica evento'",
    "1, true, 'Um item abaixo do mínimo, publica evento'",
    "5, true, 'Múltiplos itens abaixo do mínimo, publica evento'",
    "10, true, 'Muitos itens abaixo do mínimo, publica evento'"
  })
  @DisplayName("Testa envio de notificação baseado na quantidade de itens abaixo do mínimo")
  void testSendNotificationItensAtingiramQtdeMin_Parametrizado(
      long qtdeItensAbaixoMinimo, boolean devePublicarEvento, String descricao) {
    // Arrange
    when(itemRepository.countAllByQtdeMinimaIsLessThanSaldo()).thenReturn(qtdeItensAbaixoMinimo);

    // Act
    service.sendNotificationItensAtingiramQtdeMin();

    // Assert
    verify(itemRepository, times(1)).countAllByQtdeMinimaIsLessThanSaldo();

    if (devePublicarEvento) {
      verify(eventPublisher, times(1))
          .publishEvent(
              any(br.com.utfpr.gerenciamento.server.event.item.EstoqueMinNotificacaoEvent.class));
    } else {
      verify(eventPublisher, never()).publishEvent(any());
    }
  }

  @Test
  void testItemComplete_WithQueryEmpty_DisponivelFalse_ReturnsAllItems() {
    String query = "";
    boolean disponivelParaEmprestimo = false;

    ItemCompleteWithDisponibilidade projection1 =
        createItemCompleteWithDisponibilidade(
            1L, "Item A", TipoItem.C, new BigDecimal("10"), BigDecimal.ZERO);
    ItemCompleteWithDisponibilidade projection2 =
        createItemCompleteWithDisponibilidade(
            2L, "Item B", TipoItem.P, new BigDecimal("5"), new BigDecimal("2"));

    List<ItemCompleteWithDisponibilidade> projections = List.of(projection1, projection2);
    when(itemRepository.findCompleteWithDisponibilidade(query)).thenReturn(projections);

    List<br.com.utfpr.gerenciamento.server.dto.ItemResponseDto> result =
        service.itemComplete(query, disponivelParaEmprestimo);

    assertEquals(2, result.size());
    br.com.utfpr.gerenciamento.server.dto.ItemResponseDto itemA =
        result.stream().filter(i -> "Item A".equals(i.getNome())).findFirst().orElseThrow();
    br.com.utfpr.gerenciamento.server.dto.ItemResponseDto itemB =
        result.stream().filter(i -> "Item B".equals(i.getNome())).findFirst().orElseThrow();

    assertEquals(TipoItem.C, itemA.getTipoItem());
    assertEquals(TipoItem.P, itemB.getTipoItem());

    assertEquals(BigDecimal.ZERO, itemA.getQuantidadeEmprestada());
    assertNull(itemA.getDisponivelEmprestimoCalculado());

    assertEquals(new BigDecimal("2"), itemB.getQuantidadeEmprestada());
    assertEquals(new BigDecimal("3"), itemB.getDisponivelEmprestimoCalculado());

    verify(itemRepository, times(1)).findCompleteWithDisponibilidade(query);
  }

  @Test
  void testItemComplete_WithQueryEmpty_DisponivelTrue_ReturnsOnlyAvailableItems() {
    String query = "";
    boolean disponivelParaEmprestimo = true;

    ItemCompleteWithDisponibilidade projection1 =
        createItemCompleteAvailable(
            1L, "Item Disponível", TipoItem.C, new BigDecimal("10"), BigDecimal.ZERO);
    ItemCompleteWithDisponibilidade projection2 =
        createItemCompleteAvailable(
            2L, "Item Permanente", TipoItem.P, new BigDecimal("8"), new BigDecimal("3"));

    List<ItemCompleteWithDisponibilidade> projections = List.of(projection1, projection2);
    when(itemRepository.findCompleteAvailableForLoan(query)).thenReturn(projections);

    List<br.com.utfpr.gerenciamento.server.dto.ItemResponseDto> result =
        service.itemComplete(query, disponivelParaEmprestimo);

    assertEquals(2, result.size());
    br.com.utfpr.gerenciamento.server.dto.ItemResponseDto itemDisponivel =
        result.stream()
            .filter(i -> "Item Disponível".equals(i.getNome()))
            .findFirst()
            .orElseThrow();
    br.com.utfpr.gerenciamento.server.dto.ItemResponseDto itemPermanente =
        result.stream()
            .filter(i -> "Item Permanente".equals(i.getNome()))
            .findFirst()
            .orElseThrow();

    assertNull(itemDisponivel.getDisponivelEmprestimoCalculado());

    assertEquals(new BigDecimal("5"), itemPermanente.getDisponivelEmprestimoCalculado());

    verify(itemRepository, times(1)).findCompleteAvailableForLoan(query);
  }

  @Test
  void testItemComplete_WithQueryAndDisponivelTrue_FiltersCorrectly() {
    String query = "Notebook";
    boolean disponivelParaEmprestimo = true;

    ItemCompleteWithDisponibilidade projection =
        createItemCompleteAvailable(
            1L, "Notebook Dell", TipoItem.P, new BigDecimal("5"), new BigDecimal("2"));

    List<ItemCompleteWithDisponibilidade> projections = List.of(projection);
    when(itemRepository.findCompleteAvailableForLoan(query)).thenReturn(projections);

    List<br.com.utfpr.gerenciamento.server.dto.ItemResponseDto> result =
        service.itemComplete(query, disponivelParaEmprestimo);

    assertEquals(1, result.size());
    br.com.utfpr.gerenciamento.server.dto.ItemResponseDto itemResponse =
        result.stream().findFirst().orElseThrow();
    assertEquals("Notebook Dell", itemResponse.getNome());
    assertEquals(new BigDecimal("3"), itemResponse.getDisponivelEmprestimoCalculado());

    verify(itemRepository, times(1)).findCompleteAvailableForLoan(query);
  }

  @Test
  void testItemComplete_PermanentItem_ZeroSaldo_ReturnsZeroAvailability() {
    String query = "";
    boolean disponivelParaEmprestimo = false;

    ItemCompleteWithDisponibilidade projection =
        createItemCompleteWithDisponibilidade(
            1L, "Item Zerado", TipoItem.P, BigDecimal.ZERO, new BigDecimal("1"));

    List<ItemCompleteWithDisponibilidade> projections = List.of(projection);
    when(itemRepository.findCompleteWithDisponibilidade(query)).thenReturn(projections);

    List<br.com.utfpr.gerenciamento.server.dto.ItemResponseDto> result =
        service.itemComplete(query, disponivelParaEmprestimo);

    assertEquals(1, result.size());
    br.com.utfpr.gerenciamento.server.dto.ItemResponseDto itemResponse =
        result.stream().findFirst().orElseThrow();
    assertEquals("Item Zerado", itemResponse.getNome());
    assertEquals(BigDecimal.ZERO, itemResponse.getDisponivelEmprestimoCalculado());

    verify(itemRepository, times(1)).findCompleteWithDisponibilidade(query);
  }

  @Test
  void testItemComplete_NullQtdeEmprestada_TreatsAsZero() {
    String query = "";
    boolean disponivelParaEmprestimo = false;

    ItemCompleteWithDisponibilidade projection =
        createItemCompleteWithDisponibilidade(
            1L, "Item Sem Emprestimo", TipoItem.P, new BigDecimal("10"), null);

    List<ItemCompleteWithDisponibilidade> projections = List.of(projection);
    when(itemRepository.findCompleteWithDisponibilidade(query)).thenReturn(projections);

    List<br.com.utfpr.gerenciamento.server.dto.ItemResponseDto> result =
        service.itemComplete(query, disponivelParaEmprestimo);

    assertEquals(1, result.size());
    br.com.utfpr.gerenciamento.server.dto.ItemResponseDto itemResponse =
        result.stream().findFirst().orElseThrow();
    assertEquals("Item Sem Emprestimo", itemResponse.getNome());
    assertEquals(BigDecimal.ZERO, itemResponse.getQuantidadeEmprestada());
    assertEquals(new BigDecimal("10"), itemResponse.getDisponivelEmprestimoCalculado());

    verify(itemRepository, times(1)).findCompleteWithDisponibilidade(query);
  }

  @Test
  void testItemComplete_ConsumibleItem_NoDisponibilidadeCalculation() {
    String query = "";
    boolean disponivelParaEmprestimo = false;

    ItemCompleteWithDisponibilidade projection =
        createItemCompleteWithDisponibilidade(
            1L, "Item Consumível", TipoItem.C, new BigDecimal("15"), new BigDecimal("5"));

    List<ItemCompleteWithDisponibilidade> projections = List.of(projection);
    when(itemRepository.findCompleteWithDisponibilidade(query)).thenReturn(projections);

    List<br.com.utfpr.gerenciamento.server.dto.ItemResponseDto> result =
        service.itemComplete(query, disponivelParaEmprestimo);

    assertEquals(1, result.size());
    br.com.utfpr.gerenciamento.server.dto.ItemResponseDto itemResponse =
        result.stream().findFirst().orElseThrow();
    assertEquals("Item Consumível", itemResponse.getNome());
    assertEquals(TipoItem.C, itemResponse.getTipoItem());
    assertEquals(new BigDecimal("5"), itemResponse.getQuantidadeEmprestada());
    assertNull(itemResponse.getDisponivelEmprestimoCalculado());

    verify(itemRepository, times(1)).findCompleteWithDisponibilidade(query);
  }

  // Métodos auxiliares para criar dados de teste
  private Item createItemPadrao() {
    Item i = new Item();
    i.setId(1L);
    i.setNome("Notebook Dell");
    i.setDescricao("Notebook Dell 15 polegadas");
    i.setTipoItem(TipoItem.P);
    i.setSaldo(new BigDecimal("10.00"));
    i.setQtdeMinima(new BigDecimal("1.00"));
    return i;
  }

  private ItemWithQtdeEmprestada createItemWithQtdeEmprestada(
      Item item, BigDecimal qtdeEmprestada) {
    return new ItemWithQtdeEmprestada() {
      @Override
      public Item getItem() {
        return item;
      }

      @Override
      public BigDecimal getQtdeEmprestada() {
        return qtdeEmprestada;
      }
    };
  }

  private ItemCompleteWithDisponibilidade createItemCompleteWithDisponibilidade(
      Long id, String nome, TipoItem tipoItem, BigDecimal saldo, BigDecimal qtdeEmprestada) {
    return new ItemCompleteWithDisponibilidade() {
      @Override
      public Long getId() {
        return id;
      }

      @Override
      public String getNome() {
        return nome;
      }

      @Override
      public BigDecimal getSaldo() {
        return saldo;
      }

      @Override
      public TipoItem getTipoItem() {
        return tipoItem;
      }

      @Override
      public BigDecimal getQtdeEmprestada() {
        return qtdeEmprestada;
      }

      @Override
      public Grupo getGrupo() {
        return null; // Não utilizado nos testes atuais
      }
    };
  }

  private ItemCompleteWithDisponibilidade createItemCompleteAvailable(
      Long id, String nome, TipoItem tipoItem, BigDecimal saldo, BigDecimal qtdeEmprestada) {
    return createItemCompleteWithDisponibilidade(id, nome, tipoItem, saldo, qtdeEmprestada);
  }
}
