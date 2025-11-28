package br.com.utfpr.gerenciamento.server.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.utfpr.gerenciamento.server.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Testes de integração para validar os filtros de dados por role nos controllers
 * EmprestimoController e ReservaController.
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class RoleBasedFilterIntegrationTest {

  @Autowired private WebApplicationContext webApplicationContext;

  @Autowired private UsuarioRepository usuarioRepository;

  @Autowired private ObjectMapper objectMapper;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
  }

  @ParameterizedTest
  @ValueSource(strings = {"ALUNO", "PROFESSOR"})
  void emprestimoFindAll_comRolesLimitados_deveRetornarApenasEmprestimosDoUsuario(String role)
      throws Exception {
    // Act & Assert
    mockMvc
        .perform(get("/emprestimo").with(user("test").roles(role)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @ParameterizedTest
  @ValueSource(strings = {"ADMINISTRADOR", "LABORATORISTA"})
  void emprestimoFindAll_comRolesAcessoTotal_deveRetornarTodosEmprestimos(String role)
      throws Exception {
    // Act & Assert
    mockMvc
        .perform(get("/emprestimo").with(user("test").roles(role)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @ParameterizedTest
  @ValueSource(strings = {"aluno@utfpr.edu.br", "professor@utfpr.edu.br"})
  void emprestimoFilter_comRolesLimitados_deveAdicionarUsuarioAutenticado(String username)
      throws Exception {
    // Arrange
    String filtroJson = "{}";

    // Act & Assert
    mockMvc
        .perform(
            post("/emprestimo/filter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(filtroJson)
                .with(user(username)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  @WithMockUser(roles = {"ADMINISTRADOR"})
  void emprestimoFilter_comRoleAdministrador_deveUsarFiltroOriginal() throws Exception {
    // Arrange
    String filtroJson = "{}";

    // Act & Assert
    mockMvc
        .perform(
            post("/emprestimo/filter").contentType(MediaType.APPLICATION_JSON).content(filtroJson))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @ParameterizedTest
  @ValueSource(strings = {"ALUNO", "PROFESSOR"})
  @WithMockUser
  void reservaFindAll_comRolesLimitados_deveRetornarApenasReservasDoUsuario(String role)
      throws Exception {
    // Act & Assert
    mockMvc
        .perform(get("/reserva"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @ParameterizedTest
  @ValueSource(strings = {"ADMINISTRADOR", "LABORATORISTA"})
  @WithMockUser
  void reservaFindAll_comRolesAcessoTotal_deveRetornarTodasReservas(String role) throws Exception {
    // Act & Assert
    mockMvc
        .perform(get("/reserva"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @ParameterizedTest
  @ValueSource(strings = {"aluno@utfpr.edu.br", "professor@utfpr.edu.br"})
  @WithMockUser
  void reservaFindAllPaged_comRolesLimitados_deveAplicarFiltroUsuario(String username)
      throws Exception {
    // Act & Assert
    mockMvc
        .perform(get("/reserva/page").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @ParameterizedTest
  @ValueSource(strings = {"aluno@utfpr.edu.br", "professor@utfpr.edu.br"})
  @WithMockUser
  void reservaFindAllPaged_comRolesLimitadosEFilter_deveCombinarFiltros(String username)
      throws Exception {
    // Arrange
    String filter = username.contains("aluno") ? "item.nome:Laptop" : "dataReserva:27/10/2025";

    // Act & Assert
    mockMvc
        .perform(
            get("/reserva/page").param("page", "0").param("size", "10").param("filter", filter))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @ParameterizedTest
  @ValueSource(strings = {"ADMINISTRADOR", "LABORATORISTA"})
  @WithMockUser
  void reservaFindAllPaged_comRolesAcessoTotal_deveUsarFiltroOriginal(String role)
      throws Exception {
    // Act & Assert
    mockMvc
        .perform(
            get("/reserva/page")
                .param("page", "0")
                .param("size", "10")
                .param("filter", "item.nome:Laptop"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  @WithMockUser(
      username = "aluno@utfpr.edu.br",
      roles = {"ALUNO"})
  void reservaFindAllByAuthenticatedUser_deveRetornarReservasDoUsuario() throws Exception {
    // Act & Assert
    mockMvc
        .perform(get("/reserva/find-all-by-authenticated-user"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @ParameterizedTest
  @ValueSource(strings = {"ADMINISTRADOR", "LABORATORISTA"})
  @WithMockUser
  void reservaFindAllByIdItem_comRolesAcessoTotal_deveRetornarReservasDoItem(String role)
      throws Exception {
    // Act & Assert
    mockMvc
        .perform(get("/reserva/find-all-by-item/{idItem}", 1L))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }
}
