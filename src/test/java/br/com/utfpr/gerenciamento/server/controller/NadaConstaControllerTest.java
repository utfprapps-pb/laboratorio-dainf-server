package br.com.utfpr.gerenciamento.server.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
public class NadaConstaControllerTest {
  private MockMvc mockMvc;
  @Mock private br.com.utfpr.gerenciamento.server.service.NadaConstaService nadaConstaService;
  @InjectMocks private NadaConstaController nadaConstaController;

  @BeforeEach
  void setup() {
    mockMvc = MockMvcBuilders.standaloneSetup(nadaConstaController).build();
  }

  @Test
  void testGetNadaConstaPdf_ReturnsPdf() throws Exception {
    byte[] pdfBytes = new byte[] {1, 2, 3};
    when(nadaConstaService.gerarNadaConstaPdf(1L)).thenReturn(pdfBytes);
    mockMvc
        .perform(get("/nadaconsta/1/pdf"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_PDF))
        .andExpect(header().string("Content-Disposition", "attachment; filename=nada-consta.pdf"))
        .andExpect(content().bytes(pdfBytes));
  }

  @Test
  void testGetNadaConstaPdf_NotFound() throws Exception {
    when(nadaConstaService.gerarNadaConstaPdf(99L)).thenThrow(new RuntimeException());
    mockMvc.perform(get("/nadaconsta/99/pdf")).andExpect(status().isNotFound());
  }
}
