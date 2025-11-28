package br.com.utfpr.gerenciamento.server.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.dto.EmprestimoResponseDto;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.model.filter.EmprestimoFilter;
import br.com.utfpr.gerenciamento.server.service.EmprestimoService;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import br.com.utfpr.gerenciamento.server.util.SecurityUtils;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Testes unitários para o EmprestimoController com foco nos filtros de dados por role (ALUNO e
 * PROFESSOR).
 */
@ExtendWith(MockitoExtension.class)
class EmprestimoControllerTest {

  @Mock private EmprestimoService emprestimoService;

  @Mock private UsuarioService usuarioService;

  @InjectMocks private EmprestimoController emprestimoController;

  private EmprestimoFilter emprestimoFilter;
  private Usuario usuarioAluno;
  private Usuario usuarioProfessor;
  private List<EmprestimoResponseDto> emprestimosLista;

  @BeforeEach
  void setUp() {
    emprestimoFilter = new EmprestimoFilter();

    // Setup usuário aluno
    usuarioAluno = new Usuario();
    usuarioAluno.setUsername("aluno@utfpr.edu.br");
    usuarioAluno.setNome("Aluno Teste");

    // Setup usuário professor
    usuarioProfessor = new Usuario();
    usuarioProfessor.setUsername("professor@utfpr.edu.br");
    usuarioProfessor.setNome("Professor Teste");

    // Setup lista de empréstimos mock
    emprestimosLista = List.of(new EmprestimoResponseDto());
  }

  @Test
  void findAll_comRoleAluno_deveRetornarApenasEmprestimosDoUsuario() {
    try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
      // Arrange
      securityUtils.when(SecurityUtils::getAuthenticatedUsername).thenReturn("aluno@utfpr.edu.br");
      securityUtils
          .when(SecurityUtils::getAuthenticatedUserRoles)
          .thenReturn(List.of("ROLE_ALUNO"));

      when(emprestimoService.findAllEmprestimosAbertosByUsuario("aluno@utfpr.edu.br"))
          .thenReturn(emprestimosLista);

      // Act
      List<EmprestimoResponseDto> resultado = emprestimoController.findAll();

      // Assert
      assertEquals(emprestimosLista, resultado);
      verify(emprestimoService).findAllEmprestimosAbertosByUsuario("aluno@utfpr.edu.br");
      verify(emprestimoService, never()).findAllEmprestimosAbertos();
    }
  }

  @Test
  void findAll_comRoleProfessor_deveRetornarApenasEmprestimosDoUsuario() {
    try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
      // Arrange
      securityUtils
          .when(SecurityUtils::getAuthenticatedUsername)
          .thenReturn("professor@utfpr.edu.br");
      securityUtils
          .when(SecurityUtils::getAuthenticatedUserRoles)
          .thenReturn(List.of("ROLE_PROFESSOR"));

      when(emprestimoService.findAllEmprestimosAbertosByUsuario("professor@utfpr.edu.br"))
          .thenReturn(emprestimosLista);

      // Act
      List<EmprestimoResponseDto> resultado = emprestimoController.findAll();

      // Assert
      assertEquals(emprestimosLista, resultado);
      verify(emprestimoService).findAllEmprestimosAbertosByUsuario("professor@utfpr.edu.br");
      verify(emprestimoService, never()).findAllEmprestimosAbertos();
    }
  }

  @Test
  void findAll_comRoleAdministrador_deveRetornarTodosEmprestimos() {
    try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
      // Arrange
      securityUtils.when(SecurityUtils::getAuthenticatedUsername).thenReturn("admin@utfpr.edu.br");
      securityUtils
          .when(SecurityUtils::getAuthenticatedUserRoles)
          .thenReturn(List.of("ROLE_ADMINISTRADOR"));

      when(emprestimoService.findAllEmprestimosAbertos()).thenReturn(emprestimosLista);

      // Act
      List<EmprestimoResponseDto> resultado = emprestimoController.findAll();

      // Assert
      assertEquals(emprestimosLista, resultado);
      verify(emprestimoService).findAllEmprestimosAbertos();
      verify(emprestimoService, never()).findAllEmprestimosAbertosByUsuario(anyString());
    }
  }

  @Test
  void findAll_comRoleLaboratorista_deveRetornarTodosEmprestimos() {
    try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
      // Arrange
      securityUtils.when(SecurityUtils::getAuthenticatedUsername).thenReturn("lab@utfpr.edu.br");
      securityUtils
          .when(SecurityUtils::getAuthenticatedUserRoles)
          .thenReturn(List.of("ROLE_LABORATORISTA"));

      when(emprestimoService.findAllEmprestimosAbertos()).thenReturn(emprestimosLista);

      // Act
      List<EmprestimoResponseDto> resultado = emprestimoController.findAll();

      // Assert
      assertEquals(emprestimosLista, resultado);
      verify(emprestimoService).findAllEmprestimosAbertos();
      verify(emprestimoService, never()).findAllEmprestimosAbertosByUsuario(anyString());
    }
  }

  @Test
  void filter_comRoleAlunoESemUsuarioNoFiltro_deveAdicionarUsuarioAutenticado() {
    try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
      // Arrange
      securityUtils.when(SecurityUtils::getAuthenticatedUsername).thenReturn("aluno@utfpr.edu.br");
      securityUtils
          .when(SecurityUtils::getAuthenticatedUserRoles)
          .thenReturn(List.of("ROLE_ALUNO"));

      when(usuarioService.findByUsername("aluno@utfpr.edu.br"))
          .thenReturn(new br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto());
      when(usuarioService.toEntity(
              any(br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto.class)))
          .thenReturn(usuarioAluno);

      when(emprestimoService.filter(any(EmprestimoFilter.class))).thenReturn(emprestimosLista);

      // Act
      List<EmprestimoResponseDto> resultado = emprestimoController.filter(emprestimoFilter);

      // Assert
      assertEquals(emprestimosLista, resultado);
      assertEquals(usuarioAluno, emprestimoFilter.getUsuarioEmprestimo());
      verify(usuarioService).findByUsername("aluno@utfpr.edu.br");
      verify(emprestimoService).filter(emprestimoFilter);
    }
  }

  @Test
  void filter_comRoleProfessorESemUsuarioNoFiltro_deveAdicionarUsuarioAutenticado() {
    try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
      // Arrange
      securityUtils
          .when(SecurityUtils::getAuthenticatedUsername)
          .thenReturn("professor@utfpr.edu.br");
      securityUtils
          .when(SecurityUtils::getAuthenticatedUserRoles)
          .thenReturn(List.of("ROLE_PROFESSOR"));

      when(usuarioService.findByUsername("professor@utfpr.edu.br"))
          .thenReturn(new br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto());
      when(usuarioService.toEntity(
              any(br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto.class)))
          .thenReturn(usuarioProfessor);

      when(emprestimoService.filter(any(EmprestimoFilter.class))).thenReturn(emprestimosLista);

      // Act
      List<EmprestimoResponseDto> resultado = emprestimoController.filter(emprestimoFilter);

      // Assert
      assertEquals(emprestimosLista, resultado);
      assertEquals(usuarioProfessor, emprestimoFilter.getUsuarioEmprestimo());
      verify(usuarioService).findByUsername("professor@utfpr.edu.br");
      verify(emprestimoService).filter(emprestimoFilter);
    }
  }

  @Test
  void filter_comRoleAlunoEUsuarioNoFiltro_deveSubstituirPeloUsuarioAutenticado() {
    try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
      // Arrange
      Usuario usuarioExistente = new Usuario();
      usuarioExistente.setUsername("outro@utfpr.edu.br");
      emprestimoFilter.setUsuarioEmprestimo(usuarioExistente);

      securityUtils.when(SecurityUtils::getAuthenticatedUsername).thenReturn("aluno@utfpr.edu.br");
      securityUtils
          .when(SecurityUtils::getAuthenticatedUserRoles)
          .thenReturn(List.of("ROLE_ALUNO"));

      when(usuarioService.findByUsername("aluno@utfpr.edu.br"))
          .thenReturn(new br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto());
      when(usuarioService.toEntity(
              any(br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto.class)))
          .thenReturn(usuarioAluno);

      when(emprestimoService.filter(emprestimoFilter)).thenReturn(emprestimosLista);

      // Act
      List<EmprestimoResponseDto> resultado = emprestimoController.filter(emprestimoFilter);

      // Assert
      assertEquals(emprestimosLista, resultado);
      assertEquals(usuarioAluno, emprestimoFilter.getUsuarioEmprestimo());
      verify(usuarioService).findByUsername("aluno@utfpr.edu.br");
      verify(emprestimoService).filter(emprestimoFilter);
    }
  }

  @Test
  void filter_comRoleAlunoEUsuarioDiferenteNoFiltro_deveSubstituirPeloUsuarioAutenticado() {
    try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
      // Arrange
      Usuario usuarioDiferente = new Usuario();
      usuarioDiferente.setUsername("outro@utfpr.edu.br");
      emprestimoFilter.setUsuarioEmprestimo(usuarioDiferente);

      securityUtils.when(SecurityUtils::getAuthenticatedUsername).thenReturn("aluno@utfpr.edu.br");
      securityUtils
          .when(SecurityUtils::getAuthenticatedUserRoles)
          .thenReturn(List.of("ROLE_ALUNO"));

      when(usuarioService.findByUsername("aluno@utfpr.edu.br"))
          .thenReturn(new br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto());
      when(usuarioService.toEntity(
              any(br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto.class)))
          .thenReturn(usuarioAluno);

      when(emprestimoService.filter(any(EmprestimoFilter.class))).thenReturn(emprestimosLista);

      // Act
      List<EmprestimoResponseDto> resultado = emprestimoController.filter(emprestimoFilter);

      // Assert
      assertEquals(emprestimosLista, resultado);
      assertEquals(usuarioAluno, emprestimoFilter.getUsuarioEmprestimo());
      verify(usuarioService).findByUsername("aluno@utfpr.edu.br");
      verify(emprestimoService).filter(emprestimoFilter);
    }
  }

  @Test
  void filter_comRoleProfessorEUsuarioDiferenteNoFiltro_deveSubstituirPeloUsuarioAutenticado() {
    try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
      // Arrange
      Usuario usuarioDiferente = new Usuario();
      usuarioDiferente.setUsername("outro@utfpr.edu.br");
      emprestimoFilter.setUsuarioEmprestimo(usuarioDiferente);

      securityUtils
          .when(SecurityUtils::getAuthenticatedUsername)
          .thenReturn("professor@utfpr.edu.br");
      securityUtils
          .when(SecurityUtils::getAuthenticatedUserRoles)
          .thenReturn(List.of("ROLE_PROFESSOR"));

      when(usuarioService.findByUsername("professor@utfpr.edu.br"))
          .thenReturn(new br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto());
      when(usuarioService.toEntity(
              any(br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto.class)))
          .thenReturn(usuarioProfessor);

      when(emprestimoService.filter(any(EmprestimoFilter.class))).thenReturn(emprestimosLista);

      // Act
      List<EmprestimoResponseDto> resultado = emprestimoController.filter(emprestimoFilter);

      // Assert
      assertEquals(emprestimosLista, resultado);
      assertEquals(usuarioProfessor, emprestimoFilter.getUsuarioEmprestimo());
      verify(usuarioService).findByUsername("professor@utfpr.edu.br");
      verify(emprestimoService).filter(emprestimoFilter);
    }
  }

  @Test
  void filter_comRoleAdministrador_deveUsarFiltroOriginal() {
    try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
      // Arrange
      securityUtils.when(SecurityUtils::getAuthenticatedUsername).thenReturn("admin@utfpr.edu.br");
      securityUtils
          .when(SecurityUtils::getAuthenticatedUserRoles)
          .thenReturn(List.of("ROLE_ADMINISTRADOR"));

      when(emprestimoService.filter(emprestimoFilter)).thenReturn(emprestimosLista);

      // Act
      List<EmprestimoResponseDto> resultado = emprestimoController.filter(emprestimoFilter);

      // Assert
      assertEquals(emprestimosLista, resultado);
      assertNull(emprestimoFilter.getUsuarioEmprestimo());
      verify(usuarioService, never()).findByUsername(anyString());
      verify(emprestimoService).filter(emprestimoFilter);
    }
  }

  @Test
  void filter_comRoleLaboratorista_deveUsarFiltroOriginal() {
    try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
      // Arrange
      securityUtils.when(SecurityUtils::getAuthenticatedUsername).thenReturn("lab@utfpr.edu.br");
      securityUtils
          .when(SecurityUtils::getAuthenticatedUserRoles)
          .thenReturn(List.of("ROLE_LABORATORISTA"));

      when(emprestimoService.filter(emprestimoFilter)).thenReturn(emprestimosLista);

      // Act
      List<EmprestimoResponseDto> resultado = emprestimoController.filter(emprestimoFilter);

      // Assert
      assertEquals(emprestimosLista, resultado);
      assertNull(emprestimoFilter.getUsuarioEmprestimo());
      verify(usuarioService, never()).findByUsername(anyString());
      verify(emprestimoService).filter(emprestimoFilter);
    }
  }

  @Test
  void filter_comMultiplasRolesIncluindoAluno_deveAdicionarUsuarioAutenticado() {
    try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
      // Arrange
      securityUtils.when(SecurityUtils::getAuthenticatedUsername).thenReturn("aluno@utfpr.edu.br");
      securityUtils
          .when(SecurityUtils::getAuthenticatedUserRoles)
          .thenReturn(List.of("ROLE_ALUNO", "ROLE_USUARIO"));

      when(usuarioService.findByUsername("aluno@utfpr.edu.br"))
          .thenReturn(new br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto());
      when(usuarioService.toEntity(
              any(br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto.class)))
          .thenReturn(usuarioAluno);

      when(emprestimoService.filter(any(EmprestimoFilter.class))).thenReturn(emprestimosLista);

      // Act
      List<EmprestimoResponseDto> resultado = emprestimoController.filter(emprestimoFilter);

      // Assert
      assertEquals(emprestimosLista, resultado);
      assertEquals(usuarioAluno, emprestimoFilter.getUsuarioEmprestimo());
      verify(usuarioService).findByUsername("aluno@utfpr.edu.br");
      verify(emprestimoService).filter(emprestimoFilter);
    }
  }
}
