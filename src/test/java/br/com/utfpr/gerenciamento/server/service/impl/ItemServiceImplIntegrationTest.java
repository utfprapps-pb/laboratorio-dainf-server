package br.com.utfpr.gerenciamento.server.service.impl;

import static org.assertj.core.api.Assertions.*;

import br.com.utfpr.gerenciamento.server.dto.ItemResponseDto;
import br.com.utfpr.gerenciamento.server.enumeration.TipoItem;
import br.com.utfpr.gerenciamento.server.exception.EntityNotFoundException;
import br.com.utfpr.gerenciamento.server.exception.SaldoInsuficienteException;
import br.com.utfpr.gerenciamento.server.model.Grupo;
import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.repository.GrupoRepository;
import br.com.utfpr.gerenciamento.server.repository.ItemRepository;
import br.com.utfpr.gerenciamento.server.service.ItemService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceImplIntegrationTest {

  @Autowired private ItemService itemService;

  @Autowired private ItemRepository itemRepository;

  @Autowired private GrupoRepository grupoRepository;

  private Grupo grupo;
  private Item itemPermanente;
  private Item itemConsumo;

  @BeforeEach
  void setUp() {
    // Limpa dados
    itemRepository.deleteAll();
    grupoRepository.deleteAll();

    // Cria grupo
    grupo = new Grupo();
    grupo.setDescricao("Eletrônicos");
    grupo = grupoRepository.save(grupo);

    // Cria item permanente
    itemPermanente = new Item();
    itemPermanente.setNome("Notebook Dell Latitude");
    itemPermanente.setDescricao("Notebook para desenvolvimento");
    itemPermanente.setTipoItem(TipoItem.P);
    itemPermanente.setSaldo(new BigDecimal("10.00"));
    itemPermanente.setQtdeMinima(new BigDecimal("2.00"));
    itemPermanente.setValor(new BigDecimal("3000.00"));
    itemPermanente.setGrupo(grupo);
    itemPermanente = itemRepository.save(itemPermanente);

    // Cria item de consumo
    itemConsumo = new Item();
    itemConsumo.setNome("Cabo HDMI");
    itemConsumo.setDescricao("Cabo HDMI 2.0");
    itemConsumo.setTipoItem(TipoItem.C);
    itemConsumo.setSaldo(new BigDecimal("50.00"));
    itemConsumo.setQtdeMinima(new BigDecimal("10.00"));
    itemConsumo.setValor(new BigDecimal("25.00"));
    itemConsumo.setGrupo(grupo);
    itemConsumo = itemRepository.save(itemConsumo);
  }

  @Test
  void testItemComplete_ComFiltroVazio_DeveRetornarTodosItens() {
    // Act
    List<ItemResponseDto> result = itemService.itemComplete("", false);

    // Assert
    assertThat(result).hasSize(2);
    assertThat(result.stream().findFirst().orElseThrow().getNome()).isNotEmpty();
  }

  @Test
  void testItemComplete_ComFiltro_DeveRetornarItensFiltrados() {
    // Act
    List<ItemResponseDto> result = itemService.itemComplete("Notebook", false);

    // Assert
    assertThat(result).hasSize(1);
    assertThat(result.stream().findFirst().orElseThrow().getNome())
        .containsIgnoringCase("Notebook");
  }

  @Test
  void testItemComplete_ComEstoque_DeveRetornarApenasItensComSaldo() {
    // Arrange - zera saldo de um item
    itemConsumo.setSaldo(BigDecimal.ZERO);
    itemRepository.save(itemConsumo);

    // Act
    List<ItemResponseDto> result = itemService.itemComplete("", true);

    // Assert
    assertThat(result).hasSize(1); // Apenas itemPermanente tem saldo > 0
    assertThat(result.stream().findFirst().orElseThrow().getSaldo()).isGreaterThan(BigDecimal.ZERO);
  }

  @Test
  void testFindByGrupo_DeveRetornarItensDoGrupo() {
    // Act
    List<ItemResponseDto> result = itemService.findByGrupo(grupo.getId());

    // Assert
    assertThat(result).hasSize(2).allMatch(dto -> dto.getGrupo().getId().equals(grupo.getId()));
  }

  @Test
  void testConvertToDto_DeveConverterEntityParaDto() {
    // Act
    ItemResponseDto dto = itemService.toDto(itemPermanente);

    // Assert
    assertThat(dto.getId()).isEqualTo(itemPermanente.getId());
    assertThat(dto.getNome()).isEqualTo(itemPermanente.getNome());
    assertThat(dto.getSaldo()).isEqualByComparingTo(itemPermanente.getSaldo());
    assertThat(dto.getGrupo().getId()).isEqualTo(grupo.getId());
  }

  @Test
  void testDiminuiSaldoItem_ComValidacao_DeveDiminuirSaldo() {
    // Arrange
    BigDecimal saldoInicial = itemPermanente.getSaldo();
    BigDecimal qtdeDiminuir = new BigDecimal("3.00");

    // Act
    itemService.diminuiSaldoItem(itemPermanente.getId(), qtdeDiminuir, true);

    // Assert
    Item itemAtualizado = itemRepository.findById(itemPermanente.getId()).orElseThrow();
    assertThat(itemAtualizado.getSaldo()).isEqualByComparingTo(saldoInicial.subtract(qtdeDiminuir));
  }

  @Test
  void testDiminuiSaldoItem_SemValidacao_DeveDiminuirSaldo() {
    // Arrange
    BigDecimal saldoInicial = itemPermanente.getSaldo();
    BigDecimal qtdeDiminuir = new BigDecimal("15.00"); // Maior que saldo disponível

    // Act
    itemService.diminuiSaldoItem(itemPermanente.getId(), qtdeDiminuir, false);

    // Assert
    Item itemAtualizado = itemRepository.findById(itemPermanente.getId()).orElseThrow();
    assertThat(itemAtualizado.getSaldo()).isEqualByComparingTo(saldoInicial.subtract(qtdeDiminuir));
  }

  @Test
  void testDiminuiSaldoItem_SaldoInsuficiente_DeveLancarExcecao() {
    // Arrange
    BigDecimal qtdeDiminuir = new BigDecimal("15.00"); // Maior que saldo disponível
    Long itemId = itemPermanente.getId();

    // Act & Assert
    assertThatThrownBy(() -> itemService.diminuiSaldoItem(itemId, qtdeDiminuir, true))
        .isInstanceOf(SaldoInsuficienteException.class)
        .hasMessageContaining("Saldo menor que a quantidade informada");
  }

  @Test
  void testDiminuiSaldoItem_ItemNaoEncontrado_DeveLancarExcecao() {
    // Arrange
    Long itemIdInexistente = 999L;
    BigDecimal qtde = new BigDecimal("1.00");

    // Act & Assert
    assertThatThrownBy(() -> itemService.diminuiSaldoItem(itemIdInexistente, qtde, true))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Item não encontrado com ID: 999");
  }

  @Test
  void testAumentaSaldoItem_DeveAumentarSaldo() {
    // Arrange
    BigDecimal saldoInicial = itemPermanente.getSaldo();
    BigDecimal qtdeAumentar = new BigDecimal("5.00");

    // Act
    itemService.aumentaSaldoItem(itemPermanente.getId(), qtdeAumentar);

    // Assert
    Item itemAtualizado = itemRepository.findById(itemPermanente.getId()).orElseThrow();
    assertThat(itemAtualizado.getSaldo()).isEqualByComparingTo(saldoInicial.add(qtdeAumentar));
  }

  @Test
  void testAumentaSaldoItem_ItemNaoEncontrado_DeveLancarExcecao() {
    // Arrange
    Long itemIdInexistente = 999L;
    BigDecimal qtde = new BigDecimal("1.00");

    // Act & Assert
    assertThatThrownBy(() -> itemService.aumentaSaldoItem(itemIdInexistente, qtde))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Item não encontrado com ID: 999");
  }

  @Test
  void testGetSaldoItem_DeveRetornarSaldo() {
    // Act
    BigDecimal saldo = itemService.getSaldoItem(itemPermanente.getId());

    // Assert
    assertThat(saldo).isEqualByComparingTo(new BigDecimal("10.00"));
  }

  @Test
  void testGetSaldoItem_ItemNaoEncontrado_DeveLancarExcecao() {
    // Act & Assert
    assertThatThrownBy(() -> itemService.getSaldoItem(999L))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Item não encontrado com ID: 999");
  }

  @Test
  void testSaldoItemIsValid_SaldoSuficiente_DeveRetornarTrue() {
    // Arrange
    BigDecimal saldoDisponivel = new BigDecimal("10.00");
    BigDecimal qtdeVerificar = new BigDecimal("5.00");

    // Act
    Boolean resultado = itemService.saldoItemIsValid(saldoDisponivel, qtdeVerificar);

    // Assert
    assertThat(resultado).isTrue();
  }

  @Test
  void testSaldoItemIsValid_SaldoIgualQuantidade_DeveRetornarTrue() {
    // Arrange
    BigDecimal saldoDisponivel = new BigDecimal("10.00");
    BigDecimal qtdeVerificar = new BigDecimal("10.00");

    // Act
    Boolean resultado = itemService.saldoItemIsValid(saldoDisponivel, qtdeVerificar);

    // Assert
    assertThat(resultado).isTrue();
  }

  @Test
  void testSaldoItemIsValid_SaldoInsuficiente_DeveLancarExcecao() {
    // Arrange
    BigDecimal saldoDisponivel = new BigDecimal("5.00");
    BigDecimal qtdeVerificar = new BigDecimal("10.00");

    // Act & Assert
    assertThatThrownBy(() -> itemService.saldoItemIsValid(saldoDisponivel, qtdeVerificar))
        .isInstanceOf(SaldoInsuficienteException.class)
        .hasMessageContaining("Saldo menor que a quantidade informada");
  }

  @Test
  void testSaldoItemIsValid_SaldoZero_DeveLancarExcecao() {
    // Arrange
    BigDecimal saldoDisponivel = BigDecimal.ZERO;
    BigDecimal qtdeVerificar = new BigDecimal("1.00");

    // Act & Assert
    assertThatThrownBy(() -> itemService.saldoItemIsValid(saldoDisponivel, qtdeVerificar))
        .isInstanceOf(SaldoInsuficienteException.class)
        .hasMessageContaining("Saldo menor ou igual a 0");
  }

  @Test
  void testSaldoItemIsValid_SaldoNegativo_DeveLancarExcecao() {
    // Arrange
    BigDecimal saldoDisponivel = new BigDecimal("-5.00");
    BigDecimal qtdeVerificar = new BigDecimal("1.00");

    // Act & Assert
    assertThatThrownBy(() -> itemService.saldoItemIsValid(saldoDisponivel, qtdeVerificar))
        .isInstanceOf(SaldoInsuficienteException.class)
        .hasMessageContaining("Saldo menor ou igual a 0");
  }

  @Test
  void testFluxoCompleto_EmprestimoEDevolucao() {
    // Arrange
    BigDecimal saldoInicial = itemPermanente.getSaldo();
    BigDecimal qtdeEmprestimo = new BigDecimal("3.00");

    // Act - Simula empréstimo (diminui saldo)
    itemService.diminuiSaldoItem(itemPermanente.getId(), qtdeEmprestimo, true);
    BigDecimal saldoAposEmprestimo = itemService.getSaldoItem(itemPermanente.getId());

    // Assert - Verifica diminuição
    assertThat(saldoAposEmprestimo).isEqualByComparingTo(saldoInicial.subtract(qtdeEmprestimo));

    // Act - Simula devolução (aumenta saldo)
    itemService.aumentaSaldoItem(itemPermanente.getId(), qtdeEmprestimo);
    BigDecimal saldoAposDevolucao = itemService.getSaldoItem(itemPermanente.getId());

    // Assert - Verifica restauração
    assertThat(saldoAposDevolucao).isEqualByComparingTo(saldoInicial);
  }

  @Test
  void testFluxoCompleto_MultiplasDiminuicoes() {
    // Arrange
    BigDecimal saldoInicial = new BigDecimal("100.00");
    itemConsumo.setSaldo(saldoInicial);
    itemRepository.save(itemConsumo);

    // Act - Múltiplas diminuições (simulando múltiplos empréstimos)
    itemService.diminuiSaldoItem(itemConsumo.getId(), new BigDecimal("10.00"), true);
    itemService.diminuiSaldoItem(itemConsumo.getId(), new BigDecimal("20.00"), true);
    itemService.diminuiSaldoItem(itemConsumo.getId(), new BigDecimal("15.00"), true);

    BigDecimal saldoFinal = itemService.getSaldoItem(itemConsumo.getId());

    // Assert
    assertThat(saldoFinal).isEqualByComparingTo(new BigDecimal("55.00"));
  }
}
