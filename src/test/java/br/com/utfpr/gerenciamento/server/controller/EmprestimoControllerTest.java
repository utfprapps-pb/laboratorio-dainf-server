package br.com.utfpr.gerenciamento.server.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.utfpr.gerenciamento.server.dto.EmprestimoResponseDto;
import br.com.utfpr.gerenciamento.server.service.EmprestimoService;
import java.util.Collections;
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
class EmprestimoControllerTest {

  private MockMvc mockMvc;

  @Mock private EmprestimoService emprestimoService;

  @InjectMocks private EmprestimoController emprestimoController;

  @BeforeEach
  void setup() {
    mockMvc = MockMvcBuilders.standaloneSetup(emprestimoController).build();
  }

  @Test
  void testFindByItemId_DeveRetornarListaDeEmprestimos() throws Exception {
    // Given
    Long itemId = 1L;
    EmprestimoResponseDto emprestimoDto = new EmprestimoResponseDto();
    emprestimoDto.setId(1L);

    when(emprestimoService.findAllByItemId(itemId))
        .thenReturn(Collections.singletonList(emprestimoDto));

    // When & Then
    mockMvc
        .perform(
            get("/emprestimo/find-by-item/{itemId}", itemId).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].id").value(1L));
  }

  @Test
  void testFindByItemId_DeveRetornarListaVaziaQuandoNenhumEmprestimoEncontrado() throws Exception {
    // Given
    Long itemId = 999L;

    when(emprestimoService.findAllByItemId(itemId)).thenReturn(Collections.emptyList());

    // When & Then
    mockMvc
        .perform(
            get("/emprestimo/find-by-item/{itemId}", itemId).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());
  }
}
