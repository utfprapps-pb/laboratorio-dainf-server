package br.com.utfpr.gerenciamento.server.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.dto.ReservaResponseDto;
import br.com.utfpr.gerenciamento.server.service.ReservaService;
import br.com.utfpr.gerenciamento.server.util.SecurityUtils;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

/**
 * Testes unitários para o ReservaController com foco nos filtros de dados por role (ALUNO e
 * PROFESSOR).
 */
@ExtendWith(MockitoExtension.class)
class ReservaControllerTest {

  @Mock private ReservaService reservaService;

  @InjectMocks private ReservaController reservaController;

  private List<ReservaResponseDto> reservasLista;
  private Page<ReservaResponseDto> reservasPage;

  @BeforeEach
  void setUp() {
    reservasLista = List.of(new ReservaResponseDto());
    reservasPage = new PageImpl<>(reservasLista);
  }

  @Test
  void findAll_comRoleAluno_deveRetornarApenasReservasDoUsuario() {
    try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
      // Arrange
      securityUtils
          .when(SecurityUtils::getAuthenticatedUserRoles)
          .thenReturn(List.of("ROLE_ALUNO"));

      when(reservaService.findAllByAuthenticatedUser()).thenReturn(reservasLista);

      // Act
      List<ReservaResponseDto> resultado = reservaController.findAll();

      // Assert
      assertEquals(reservasLista, resultado);
      verify(reservaService).findAllByAuthenticatedUser();
      verify(reservaService, never()).findAll();
    }
  }

  @Test
  void findAll_comRoleProfessor_deveRetornarApenasReservasDoUsuario() {
    try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
      // Arrange
      securityUtils
          .when(SecurityUtils::getAuthenticatedUserRoles)
          .thenReturn(List.of("ROLE_PROFESSOR"));

      when(reservaService.findAllByAuthenticatedUser()).thenReturn(reservasLista);

      // Act
      List<ReservaResponseDto> resultado = reservaController.findAll();

      // Assert
      assertEquals(reservasLista, resultado);
      verify(reservaService).findAllByAuthenticatedUser();
      verify(reservaService, never()).findAll();
    }
  }

  @Test
  void findAll_comRoleAdministrador_deveRetornarTodasReservas() {
    try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
      // Arrange
      securityUtils
          .when(SecurityUtils::getAuthenticatedUserRoles)
          .thenReturn(List.of("ROLE_ADMINISTRADOR"));

      when(reservaService.findAll(any(Sort.class))).thenReturn(reservasLista);

      // Act
      List<ReservaResponseDto> resultado = reservaController.findAll();

      // Assert
      assertEquals(reservasLista, resultado);
      verify(reservaService).findAll(any(Sort.class));
      verify(reservaService, never()).findAllByAuthenticatedUser();
    }
  }

  @Test
  void findAll_comRoleLaboratorista_deveRetornarTodasReservas() {
    try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
      // Arrange
      securityUtils
          .when(SecurityUtils::getAuthenticatedUserRoles)
          .thenReturn(List.of("ROLE_LABORATORISTA"));

      when(reservaService.findAll(any(Sort.class))).thenReturn(reservasLista);

      // Act
      List<ReservaResponseDto> resultado = reservaController.findAll();

      // Assert
      assertEquals(reservasLista, resultado);
      verify(reservaService).findAll(any(Sort.class));
      verify(reservaService, never()).findAllByAuthenticatedUser();
    }
  }

  @Test
  void findAllByAuthenticatedUser_deveRetornarReservasDoUsuarioAutenticado() {
    // Arrange
    when(reservaService.findAllByAuthenticatedUser()).thenReturn(reservasLista);

    // Act
    List<ReservaResponseDto> resultado = reservaController.findAllByAuthenticatedUser();

    // Assert
    assertEquals(reservasLista, resultado);
    verify(reservaService).findAllByAuthenticatedUser();
  }

  @Test
  void findAllPaged_comRoleAluno_deveAplicarFiltroDeUsuario() {
    try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
      // Arrange
      securityUtils.when(SecurityUtils::getAuthenticatedUsername).thenReturn("aluno@utfpr.edu.br");
      securityUtils
          .when(SecurityUtils::getAuthenticatedUserRoles)
          .thenReturn(List.of("ROLE_ALUNO"));

      when(reservaService.filterByAllFields("usuario.username:aluno@utfpr.edu.br"))
          .thenReturn(mock(Specification.class));
      when(reservaService.findAllSpecification(any(Specification.class), any(PageRequest.class)))
          .thenReturn(reservasPage);

      // Act
      Page<ReservaResponseDto> resultado = reservaController.findAllPaged(0, 10, null, null, null);

      // Assert
      assertEquals(reservasPage, resultado);
      verify(reservaService).filterByAllFields("usuario.username:aluno@utfpr.edu.br");
      verify(reservaService).findAllSpecification(any(Specification.class), any(PageRequest.class));
      verify(reservaService, never()).findAll(any(PageRequest.class));
    }
  }

  @Test
  void findAllPaged_comRoleProfessor_deveAplicarFiltroDeUsuario() {
    try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
      // Arrange
      securityUtils
          .when(SecurityUtils::getAuthenticatedUsername)
          .thenReturn("professor@utfpr.edu.br");
      securityUtils
          .when(SecurityUtils::getAuthenticatedUserRoles)
          .thenReturn(List.of("ROLE_PROFESSOR"));

      when(reservaService.filterByAllFields("usuario.username:professor@utfpr.edu.br"))
          .thenReturn(mock(Specification.class));
      when(reservaService.findAllSpecification(any(Specification.class), any(PageRequest.class)))
          .thenReturn(reservasPage);

      // Act
      Page<ReservaResponseDto> resultado = reservaController.findAllPaged(0, 10, null, null, null);

      // Assert
      assertEquals(reservasPage, resultado);
      verify(reservaService).filterByAllFields("usuario.username:professor@utfpr.edu.br");
      verify(reservaService).findAllSpecification(any(Specification.class), any(PageRequest.class));
      verify(reservaService, never()).findAll(any(PageRequest.class));
    }
  }

  @Test
  void findAllPaged_comRoleAlunoEFilterExistente_deveCombinarFiltros() {
    try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
      // Arrange
      securityUtils.when(SecurityUtils::getAuthenticatedUsername).thenReturn("aluno@utfpr.edu.br");
      securityUtils
          .when(SecurityUtils::getAuthenticatedUserRoles)
          .thenReturn(List.of("ROLE_ALUNO"));

      String filtroCombinado = "(item.nome:Laptop) AND usuario.username:aluno@utfpr.edu.br";
      when(reservaService.filterByAllFields(filtroCombinado)).thenReturn(mock(Specification.class));
      when(reservaService.findAllSpecification(any(Specification.class), any(PageRequest.class)))
          .thenReturn(reservasPage);

      // Act
      Page<ReservaResponseDto> resultado =
          reservaController.findAllPaged(0, 10, "item.nome:Laptop", null, null);

      // Assert
      assertEquals(reservasPage, resultado);
      verify(reservaService).filterByAllFields(filtroCombinado);
      verify(reservaService).findAllSpecification(any(Specification.class), any(PageRequest.class));
    }
  }

  @Test
  void findAllPaged_comRoleProfessorEFilterExistente_deveCombinarFiltros() {
    try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
      // Arrange
      securityUtils
          .when(SecurityUtils::getAuthenticatedUsername)
          .thenReturn("professor@utfpr.edu.br");
      securityUtils
          .when(SecurityUtils::getAuthenticatedUserRoles)
          .thenReturn(List.of("ROLE_PROFESSOR"));

      String filtroCombinado =
          "(dataReserva:27/10/2025) AND usuario.username:professor@utfpr.edu.br";
      when(reservaService.filterByAllFields(filtroCombinado)).thenReturn(mock(Specification.class));
      when(reservaService.findAllSpecification(any(Specification.class), any(PageRequest.class)))
          .thenReturn(reservasPage);

      // Act
      Page<ReservaResponseDto> resultado =
          reservaController.findAllPaged(0, 10, "dataReserva:27/10/2025", null, null);

      // Assert
      assertEquals(reservasPage, resultado);
      verify(reservaService).filterByAllFields(filtroCombinado);
      verify(reservaService).findAllSpecification(any(Specification.class), any(PageRequest.class));
    }
  }

  @Test
  void findAllPaged_comRoleAlunoEOrdenacao_deveAplicarFiltroEOrdenacao() {
    try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
      // Arrange
      securityUtils.when(SecurityUtils::getAuthenticatedUsername).thenReturn("aluno@utfpr.edu.br");
      securityUtils
          .when(SecurityUtils::getAuthenticatedUserRoles)
          .thenReturn(List.of("ROLE_ALUNO"));

      when(reservaService.filterByAllFields("usuario.username:aluno@utfpr.edu.br"))
          .thenReturn(mock(Specification.class));
      when(reservaService.findAllSpecification(any(Specification.class), any(PageRequest.class)))
          .thenReturn(reservasPage);

      // Act
      Page<ReservaResponseDto> resultado =
          reservaController.findAllPaged(0, 10, null, "dataReserva", true);

      // Assert
      assertEquals(reservasPage, resultado);
      verify(reservaService).filterByAllFields("usuario.username:aluno@utfpr.edu.br");
      verify(reservaService).findAllSpecification(any(Specification.class), any(PageRequest.class));
    }
  }

  @Test
  void findAllPaged_comMultiplasRolesIncluindoAluno_deveAplicarFiltroUsuario() {
    try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
      // Arrange
      securityUtils.when(SecurityUtils::getAuthenticatedUsername).thenReturn("aluno@utfpr.edu.br");
      securityUtils
          .when(SecurityUtils::getAuthenticatedUserRoles)
          .thenReturn(List.of("ROLE_ALUNO", "ROLE_USUARIO"));

      when(reservaService.filterByAllFields("usuario.username:aluno@utfpr.edu.br"))
          .thenReturn(mock(Specification.class));
      when(reservaService.findAllSpecification(any(Specification.class), any(PageRequest.class)))
          .thenReturn(reservasPage);

      // Act
      Page<ReservaResponseDto> resultado = reservaController.findAllPaged(0, 10, null, null, null);

      // Assert
      assertEquals(reservasPage, resultado);
      verify(reservaService).filterByAllFields("usuario.username:aluno@utfpr.edu.br");
      verify(reservaService).findAllSpecification(any(Specification.class), any(PageRequest.class));
    }
  }

  @Test
  void findAllByIdItem_deveRetornarReservasDoItem() {
    // Arrange
    Long idItem = 1L;
    when(reservaService.findAllByIdItem(idItem)).thenReturn(reservasLista);

    // Act
    List<ReservaResponseDto> resultado = reservaController.findAllByIdItem(idItem);

    // Assert
    assertEquals(reservasLista, resultado);
    verify(reservaService).findAllByIdItem(idItem);
  }
}
