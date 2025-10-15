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
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class SystemConfigControllerTest {
  @Autowired private WebApplicationContext context;

  @MockitoBean
  private SystemConfigService service;

  private MockMvc mockMvc;

  @BeforeEach
  void setup() {
    mockMvc =
        MockMvcBuilders.webAppContextSetup(context)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .build();
  }

  @Test
  void shouldAllowAdminToGetConfig() throws Exception {
    SystemConfig config = new SystemConfig();
    config.setId(1L);
    config.setNadaConstaEmail("admin@utfpr.edu.br");
    Mockito.when(service.getConfig()).thenReturn(Optional.of(config));
    mockMvc
        .perform(get("/config")
            .with(SecurityMockMvcRequestPostProcessors.user("admin")
                .authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nadaConstaEmail").value("admin@utfpr.edu.br"));
  }

  @Test
  void shouldAllowAdminToSaveValidEmail() throws Exception {
    SystemConfig config = new SystemConfig();
    config.setId(1L);
    config.setNadaConstaEmail("admin@utfpr.edu.br");
    Mockito.doReturn(config).when(service).saveConfig(Mockito.any());
    mockMvc
        .perform(
            post("/config")
                .with(SecurityMockMvcRequestPostProcessors.user("admin")
                    .authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nadaConstaEmail\":\"admin@utfpr.edu.br\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nadaConstaEmail").value("admin@utfpr.edu.br"));
  }

  @Test
  void shouldRejectInvalidEmailDomain() throws Exception {
    mockMvc
        .perform(
            post("/config")
                .with(SecurityMockMvcRequestPostProcessors.user("admin")
                    .authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nadaConstaEmail\":\"admin@gmail.com\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldRejectNonAdminAccess() throws Exception {
    mockMvc.perform(get("/config")
        .with(SecurityMockMvcRequestPostProcessors.user("user")
            .authorities(new SimpleGrantedAuthority("ROLE_ALUNO"))))
        .andExpect(status().isForbidden());
    mockMvc
        .perform(
            post("/config")
                .with(SecurityMockMvcRequestPostProcessors.user("user")
                    .authorities(new SimpleGrantedAuthority("ROLE_ALUNO")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nadaConstaEmail\":\"admin@utfpr.edu.br\"}"))
        .andExpect(status().isForbidden());
  }
}
