package br.com.utfpr.gerenciamento.server.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import br.com.utfpr.gerenciamento.server.dto.NadaConstaRequestDto;
import br.com.utfpr.gerenciamento.server.dto.NadaConstaResponseDto;
import br.com.utfpr.gerenciamento.server.service.NadaConstaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
class NadaConstaControllerTest {
    @Autowired private WebApplicationContext context;
    @MockitoBean private NadaConstaService nadaConstaService;
    @Autowired private ObjectMapper objectMapper;
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void shouldAllowAdminToSolicitarNadaConsta() throws Exception {
        NadaConstaRequestDto req = new NadaConstaRequestDto();
        req.setDocumento("123456");
        NadaConstaResponseDto resp = new NadaConstaResponseDto();
        resp.setUsuarioUsername("aluno");
        Mockito.when(nadaConstaService.solicitarNadaConsta("123456")).thenReturn(resp);
        mockMvc.perform(post("/nadaconsta/solicitar")
                .with(SecurityMockMvcRequestPostProcessors.user("admin")
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioUsername").value("aluno"));
    }

    @Test
    void shouldAllowLaboratoristaToSolicitarNadaConsta() throws Exception {
        NadaConstaRequestDto req = new NadaConstaRequestDto();
        req.setDocumento("123456");
        NadaConstaResponseDto resp = new NadaConstaResponseDto();
        resp.setUsuarioUsername("aluno");
        Mockito.when(nadaConstaService.solicitarNadaConsta("123456")).thenReturn(resp);
        mockMvc.perform(post("/nadaconsta/solicitar")
                .with(SecurityMockMvcRequestPostProcessors.user("lab")
                        .authorities(new SimpleGrantedAuthority("ROLE_LABORATORISTA")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioUsername").value("aluno"));
    }

    @Test
    void shouldRejectNonPermittedRoleSolicitarNadaConsta() throws Exception {
        NadaConstaRequestDto req = new NadaConstaRequestDto();
        req.setDocumento("123456");
        mockMvc.perform(post("/nadaconsta/solicitar")
                .with(SecurityMockMvcRequestPostProcessors.user("user")
                        .authorities(new SimpleGrantedAuthority("ROLE_ALUNO")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }
}
