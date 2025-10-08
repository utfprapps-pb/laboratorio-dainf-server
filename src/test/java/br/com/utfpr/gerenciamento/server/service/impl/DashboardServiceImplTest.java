package br.com.utfpr.gerenciamento.server.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.dto.dashboards.DashboardEmprestimoCountRangeResponseDto;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardEmprestimoCountRange;
import br.com.utfpr.gerenciamento.server.repository.EmprestimoRepository;
import br.com.utfpr.gerenciamento.server.service.CompraService;
import br.com.utfpr.gerenciamento.server.service.EmprestimoService;
import br.com.utfpr.gerenciamento.server.service.SaidaService;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DashboardServiceImplTest {

  @Mock private EmprestimoService emprestimoService;

  @Mock private EmprestimoRepository emprestimoRepository;

  @Mock private CompraService compraService;

  @Mock private SaidaService saidaService;

  @Mock private ModelMapper modelMapper;

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
    DashboardEmprestimoCountRange mockResult = new DashboardEmprestimoCountRange(100, 75, 10, 15);

    DashboardEmprestimoCountRangeResponseDto expectedDto =
        new DashboardEmprestimoCountRangeResponseDto();
    expectedDto.setTotal(100);
    expectedDto.setEmAndamento(75);
    expectedDto.setEmAtraso(10);
    expectedDto.setFinalizado(15);

    when(emprestimoRepository.countEmprestimosByStatusInRange(dtIni, dtFim)).thenReturn(mockResult);
    when(modelMapper.map(mockResult, DashboardEmprestimoCountRangeResponseDto.class))
        .thenReturn(expectedDto);

    // Act
    DashboardEmprestimoCountRangeResponseDto result =
        dashboardService.findDadosEmprestimoCountRange(dtIni, dtFim);

    // Assert
    assertNotNull(result);
    assertEquals(100, result.getTotal());
    assertEquals(75, result.getEmAndamento());
    assertEquals(10, result.getEmAtraso());
    assertEquals(15, result.getFinalizado());

    verify(emprestimoRepository, times(1)).countEmprestimosByStatusInRange(dtIni, dtFim);
    verify(modelMapper, times(1)).map(mockResult, DashboardEmprestimoCountRangeResponseDto.class);
  }

  @Test
  void testFindDadosEmprestimoCountRange_comZeroEmprestimos() {
    // Arrange
    DashboardEmprestimoCountRange mockResult = new DashboardEmprestimoCountRange(0, 0, 0, 0);

    DashboardEmprestimoCountRangeResponseDto expectedDto =
        new DashboardEmprestimoCountRangeResponseDto();
    expectedDto.setTotal(0);
    expectedDto.setEmAndamento(0);
    expectedDto.setEmAtraso(0);
    expectedDto.setFinalizado(0);

    when(emprestimoRepository.countEmprestimosByStatusInRange(dtIni, dtFim)).thenReturn(mockResult);
    when(modelMapper.map(mockResult, DashboardEmprestimoCountRangeResponseDto.class))
        .thenReturn(expectedDto);

    // Act
    DashboardEmprestimoCountRangeResponseDto result =
        dashboardService.findDadosEmprestimoCountRange(dtIni, dtFim);

    // Assert
    assertNotNull(result);
    assertEquals(0, result.getTotal());
    assertEquals(0, result.getEmAndamento());
    assertEquals(0, result.getEmAtraso());
    assertEquals(0, result.getFinalizado());
  }
}
