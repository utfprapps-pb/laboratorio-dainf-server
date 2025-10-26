package br.com.utfpr.gerenciamento.server.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.enumeration.TipoItem;
import br.com.utfpr.gerenciamento.server.event.item.EstoqueMinNotificacaoEvent;
import br.com.utfpr.gerenciamento.server.minio.config.MinioConfig;
import br.com.utfpr.gerenciamento.server.minio.service.MinioService;
import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.repository.EmprestimoItemRepository;
import br.com.utfpr.gerenciamento.server.repository.ItemImageRepository;
import br.com.utfpr.gerenciamento.server.repository.ItemRepository;
import br.com.utfpr.gerenciamento.server.repository.projection.ItemWithQtdeEmprestada;
import br.com.utfpr.gerenciamento.server.service.EmailService;
import br.com.utfpr.gerenciamento.server.service.RelatorioService;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    item = new Item();
    item.setId(1L);
    item.setNome("Notebook Dell");
  }

  @Test
  void testFindOneWithDisponibilidade_Permanente_ComEstoque() {
    // Arrange
    item.setTipoItem(TipoItem.P);
    item.setSaldo(new BigDecimal("10.00"));
    BigDecimal qtdeEmprestada = new BigDecimal("3.00");

    ItemWithQtdeEmprestada projection = mock(ItemWithQtdeEmprestada.class);
    when(projection.getItem()).thenReturn(item);
    when(projection.getQtdeEmprestada()).thenReturn(qtdeEmprestada);
    when(itemRepository.findByIdWithQtdeEmprestada(1L)).thenReturn(Optional.of(projection));

    // Act
    Item resultItem = service.findOneWithDisponibilidade(1L);

    // Assert
    assertNotNull(resultItem);
    assertEquals(new BigDecimal("3.00"), resultItem.getQuantidadeEmprestada());
    assertEquals(new BigDecimal("7.00"), resultItem.getDisponivelEmprestimoCalculado());
    verify(itemRepository, times(1)).findByIdWithQtdeEmprestada(1L);
  }

  @Test
  void testFindOneWithDisponibilidade_Permanente_EstoqueZerado() {
    // Arrange
    item.setTipoItem(TipoItem.P);
    item.setSaldo(new BigDecimal("5.00"));
    BigDecimal qtdeEmprestada = new BigDecimal("5.00");

    ItemWithQtdeEmprestada projection = mock(ItemWithQtdeEmprestada.class);
    when(projection.getItem()).thenReturn(item);
    when(projection.getQtdeEmprestada()).thenReturn(qtdeEmprestada);
    when(itemRepository.findByIdWithQtdeEmprestada(1L)).thenReturn(Optional.of(projection));

    // Act
    Item resultItem = service.findOneWithDisponibilidade(1L);

    // Assert
    assertNotNull(resultItem);
    assertEquals(new BigDecimal("5.00"), resultItem.getQuantidadeEmprestada());
    assertEquals(
        0,
        resultItem.getDisponivelEmprestimoCalculado().compareTo(BigDecimal.ZERO)); // Usa compareTo
    verify(itemRepository, times(1)).findByIdWithQtdeEmprestada(1L);
  }

  @Test
  void testFindOneWithDisponibilidade_Permanente_Negativo() {
    // Arrange
    item.setTipoItem(TipoItem.P);
    item.setSaldo(new BigDecimal("2.00"));
    BigDecimal qtdeEmprestada = new BigDecimal("5.00");

    ItemWithQtdeEmprestada projection = mock(ItemWithQtdeEmprestada.class);
    when(projection.getItem()).thenReturn(item);
    when(projection.getQtdeEmprestada()).thenReturn(qtdeEmprestada);
    when(itemRepository.findByIdWithQtdeEmprestada(1L)).thenReturn(Optional.of(projection));

    // Act
    Item resultItem = service.findOneWithDisponibilidade(1L);

    // Assert
    assertNotNull(resultItem);
    assertEquals(new BigDecimal("5.00"), resultItem.getQuantidadeEmprestada());
    assertEquals(
        0,
        resultItem
            .getDisponivelEmprestimoCalculado()
            .compareTo(BigDecimal.ZERO)); // Não deve ser negativo
    verify(itemRepository, times(1)).findByIdWithQtdeEmprestada(1L);
  }

  @Test
  void testFindOneWithDisponibilidade_Consumo() {
    // Arrange
    item.setTipoItem(TipoItem.C);
    item.setSaldo(new BigDecimal("500.00"));
    BigDecimal qtdeEmprestada = BigDecimal.ZERO;

    ItemWithQtdeEmprestada projection = mock(ItemWithQtdeEmprestada.class);
    when(projection.getItem()).thenReturn(item);
    when(projection.getQtdeEmprestada()).thenReturn(qtdeEmprestada);
    when(itemRepository.findByIdWithQtdeEmprestada(1L)).thenReturn(Optional.of(projection));

    // Act
    Item resultItem = service.findOneWithDisponibilidade(1L);

    // Assert
    assertNotNull(resultItem);
    assertEquals(0, resultItem.getQuantidadeEmprestada().compareTo(BigDecimal.ZERO));
    assertNull(resultItem.getDisponivelEmprestimoCalculado()); // Null para consumo
    verify(itemRepository, times(1)).findByIdWithQtdeEmprestada(1L);
  }

  @Test
  void testFindOneWithDisponibilidade_ItemNaoEncontrado() {
    // Arrange
    when(itemRepository.findByIdWithQtdeEmprestada(999L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        br.com.utfpr.gerenciamento.server.exception.EntityNotFoundException.class,
        () -> service.findOneWithDisponibilidade(999L));
    verify(itemRepository, times(1)).findByIdWithQtdeEmprestada(999L);
  }

  @Test
  void testFindOneWithDisponibilidade_Permanente_QtdeEmprestadaNull() {
    // Arrange - projeção retorna null para qtdeEmprestada
    item.setTipoItem(TipoItem.P);
    item.setSaldo(new BigDecimal("10.00"));

    ItemWithQtdeEmprestada projection = mock(ItemWithQtdeEmprestada.class);
    when(projection.getItem()).thenReturn(item);
    when(projection.getQtdeEmprestada()).thenReturn(null); // Simula null da projeção
    when(itemRepository.findByIdWithQtdeEmprestada(1L)).thenReturn(Optional.of(projection));

    // Act
    Item resultItem = service.findOneWithDisponibilidade(1L);

    // Assert - deve tratar null como zero
    assertNotNull(resultItem);
    assertEquals(0, resultItem.getQuantidadeEmprestada().compareTo(BigDecimal.ZERO));
    assertEquals(new BigDecimal("10.00"), resultItem.getDisponivelEmprestimoCalculado());
    verify(itemRepository, times(1)).findByIdWithQtdeEmprestada(1L);
  }

  @Test
  void testSendNotification_WhenItemsBelowMinimum_PublishesEvent() {
    // Arrange
    when(itemRepository.countAllByQtdeMinimaIsLessThanSaldo()).thenReturn(5L);

    // Act
    service.sendNotificationItensAtingiramQtdeMin();

    // Assert
    verify(itemRepository, times(1)).countAllByQtdeMinimaIsLessThanSaldo();
    verify(eventPublisher, times(1)).publishEvent(any(EstoqueMinNotificacaoEvent.class));
  }

  @Test
  void testSendNotification_WhenNoItemsBelowMinimum_DoesNotPublishEvent() {
    // Arrange
    when(itemRepository.countAllByQtdeMinimaIsLessThanSaldo()).thenReturn(0L);

    // Act
    service.sendNotificationItensAtingiramQtdeMin();

    // Assert
    verify(itemRepository, times(1)).countAllByQtdeMinimaIsLessThanSaldo();
    verify(eventPublisher, never()).publishEvent(any(EstoqueMinNotificacaoEvent.class));
  }
}
