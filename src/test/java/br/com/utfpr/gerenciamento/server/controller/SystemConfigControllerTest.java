package br.com.utfpr.gerenciamento.server.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.utfpr.gerenciamento.server.model.SystemConfig;
import br.com.utfpr.gerenciamento.server.service.SystemConfigService;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class SystemConfigControllerTest {
  @Autowired private WebApplicationContext context;

  @SpyBean private SystemConfigService service;

  private MockMvc mockMvc;

  @BeforeEach
  void setup() {
    mockMvc =
        MockMvcBuilders.webAppContextSetup(context)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .build();
  }

  private void authenticateAsAdmin() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new TestingAuthenticationToken(
                "admin",
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))));
  }

  private void authenticateAsUser() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new TestingAuthenticationToken(
                "user", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ALUNO"))));
  }

  @Test
  void shouldAllowAdminToGetConfig() throws Exception {
    authenticateAsAdmin();
    SystemConfig config = new SystemConfig();
    config.setId(1L);
    config.setNadaConstaEmail("admin@utfpr.edu.br");
    Mockito.when(service.getConfig()).thenReturn(Optional.of(config));
    mockMvc
        .perform(get("/config"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nadaConstaEmail").value("admin@utfpr.edu.br"));
  }

  @Test
  void shouldAllowAdminToSaveValidEmail() throws Exception {
    authenticateAsAdmin();
    SystemConfig config = new SystemConfig();
    config.setId(1L);
    config.setNadaConstaEmail("admin@utfpr.edu.br");
    Mockito.doReturn(config).when(service).saveConfig(Mockito.any());
    mockMvc
        .perform(
            post("/config")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nadaConstaEmail\":\"admin@utfpr.edu.br\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nadaConstaEmail").value("admin@utfpr.edu.br"));
  }

  @Test
  void shouldRejectInvalidEmailDomain() throws Exception {
    authenticateAsAdmin();
    mockMvc
        .perform(
            post("/config")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nadaConstaEmail\":\"admin@gmail.com\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldRejectNonAdminAccess() throws Exception {
    authenticateAsUser();
    mockMvc.perform(get("/config")).andExpect(status().isForbidden());
    mockMvc
        .perform(
            post("/config")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nadaConstaEmail\":\"admin@utfpr.edu.br\"}"))
        .andExpect(status().isForbidden());
  }
}
