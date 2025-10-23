package br.com.utfpr.gerenciamento.server.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.dto.dashboards.DashboardEmprestimoCountRangeResponseDto;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardEmprestimoCountRange;
import br.com.utfpr.gerenciamento.server.repository.EmprestimoRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DashboardServiceImplTest {

  @Mock private EmprestimoRepository emprestimoRepository;

  @InjectMocks private DashboardServiceImpl dashboardService;

  private LocalDate dtIni;
  private LocalDate dtFim;

  @BeforeEach
  void setUp() {
    dtIni = LocalDate.of(2025, 6, 1);
    dtFim = LocalDate.of(2025, 10, 31);
  }

  @Test
  void testFindDadosEmprestimoCountRange_deveRetornarDadosCorretamente() {
    // Arrange
    DashboardEmprestimoCountRange mockResult =
        new DashboardEmprestimoCountRange(100L, 75L, 10L, 15L);

    when(emprestimoRepository.countEmprestimosByStatusInRange(dtIni, dtFim)).thenReturn(mockResult);

    // Act
    DashboardEmprestimoCountRangeResponseDto result =
        dashboardService.findDadosEmprestimoCountRange(dtIni, dtFim);

    // Assert
    assertNotNull(result);
    assertEquals(100L, result.total());
    assertEquals(75L, result.emAndamento());
    assertEquals(10L, result.emAtraso());
    assertEquals(15L, result.finalizado());

    verify(emprestimoRepository, times(1)).countEmprestimosByStatusInRange(dtIni, dtFim);
  }

  @Test
  void testFindDadosEmprestimoCountRange_QuandoResultRetornaNulo_DeveEntregarValorPadrao() {
    when(emprestimoRepository.countEmprestimosByStatusInRange(dtIni, dtFim)).thenReturn(null);

    DashboardEmprestimoCountRangeResponseDto result =
        dashboardService.findDadosEmprestimoCountRange(dtIni, dtFim);

    assertNotNull(result);
    assertEquals(0L, result.total());
    assertEquals(0L, result.emAndamento());
    assertEquals(0L, result.emAtraso());
    assertEquals(0L, result.finalizado());
  }

  @Test
  void testFindDadosEmprestimoCountRange_comZeroEmprestimos() {
    // Arrange
    DashboardEmprestimoCountRange mockResult = new DashboardEmprestimoCountRange(0L, 0L, 0L, 0L);

    when(emprestimoRepository.countEmprestimosByStatusInRange(dtIni, dtFim)).thenReturn(mockResult);

    // Act
    DashboardEmprestimoCountRangeResponseDto result =
        dashboardService.findDadosEmprestimoCountRange(dtIni, dtFim);

    // Assert
    assertNotNull(result);
    assertEquals(0L, result.total());
    assertEquals(0L, result.emAndamento());
    assertEquals(0L, result.emAtraso());
    assertEquals(0L, result.finalizado());
  }
}
