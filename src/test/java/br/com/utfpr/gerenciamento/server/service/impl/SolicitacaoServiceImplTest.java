package br.com.utfpr.gerenciamento.server.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.dto.SolicitacaoResponseDto;
import br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto;
import br.com.utfpr.gerenciamento.server.model.Solicitacao;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.repository.SolicitacaoRepository;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
class SolicitacaoServiceImplTest {

  @Mock private SolicitacaoRepository solicitacaoRepository;
  @Mock private UsuarioService usuarioService;
  @Mock private ModelMapper modelMapper;

  @InjectMocks private SolicitacaoServiceImpl solicitacaoService;

  private Solicitacao solicitacao;
  private SolicitacaoResponseDto solicitacaoResponseDto;
  private Usuario usuario;
  private UsuarioResponseDto usuarioResponseDto;

  @BeforeEach
  void setUp() {
    usuario = criarUsuario(1L, "joao.silva");
    usuarioResponseDto = criarUsuarioResponseDto(1L, "joao.silva");
    solicitacao = criarSolicitacao(1L, usuario);
    solicitacaoResponseDto = criarSolicitacaoResponseDto(1L, usuarioResponseDto);
  }

  @Test
  void testGetRepository_DeveRetornarSolicitacaoRepository() {
    // When
    var result = solicitacaoService.getRepository();

    // Then
    assertThat(result).isEqualTo(solicitacaoRepository);
  }

  @Test
  void testToDto_DeveConverterSolicitacaoParaDTO() {
    // Given
    when(modelMapper.map(solicitacao, SolicitacaoResponseDto.class))
        .thenReturn(solicitacaoResponseDto);

    // When
    SolicitacaoResponseDto result = solicitacaoService.toDto(solicitacao);

    // Then
    assertNotNull(result);
    assertThat(result).isEqualTo(solicitacaoResponseDto);
    verify(modelMapper).map(solicitacao, SolicitacaoResponseDto.class);
  }

  @Test
  void testToDto_ComSolicitacaoCompleta_DeveConverterCorretamente() {
    // Given
    solicitacao.setDescricao("Solicitação de equipamentos para laboratório");
    solicitacao.setObservacao("Urgente");

    SolicitacaoResponseDto dtoEsperado = criarSolicitacaoResponseDto(1L, usuarioResponseDto);
    dtoEsperado.setDescricao("Solicitação de equipamentos para laboratório");
    dtoEsperado.setObservacao("Urgente");

    when(modelMapper.map(solicitacao, SolicitacaoResponseDto.class)).thenReturn(dtoEsperado);

    // When
    SolicitacaoResponseDto result = solicitacaoService.toDto(solicitacao);

    // Then
    assertNotNull(result);
    assertThat(result.getDescricao()).isEqualTo("Solicitação de equipamentos para laboratório");
    assertThat(result.getObservacao()).isEqualTo("Urgente");
    verify(modelMapper).map(solicitacao, SolicitacaoResponseDto.class);
  }

  @Test
  void testToDto_ComSolicitacaoNula_DeveRetornarNull() {
    // Given
    when(modelMapper.map(null, SolicitacaoResponseDto.class)).thenReturn(null);

    // When
    SolicitacaoResponseDto result = solicitacaoService.toDto(null);

    // Then
    assertNull(result);
  }

  @Test
  void testToEntity_DeveConverterDTOParaSolicitacao() {
    // Given
    when(modelMapper.map(solicitacaoResponseDto, Solicitacao.class)).thenReturn(solicitacao);

    // When
    Solicitacao result = solicitacaoService.toEntity(solicitacaoResponseDto);

    // Then
    assertNotNull(result);
    assertThat(result).isEqualTo(solicitacao);
    verify(modelMapper).map(solicitacaoResponseDto, Solicitacao.class);
  }

  @Test
  void testToEntity_ComDTONull_DeveRetornarNull() {
    // Given
    when(modelMapper.map(null, Solicitacao.class)).thenReturn(null);

    // When
    Solicitacao result = solicitacaoService.toEntity(null);

    // Then
    assertNull(result);
  }

  @Test
  void testFindAllByUsername_DeveRetornarListaDeSolicitacoes() {
    // Given
    String username = "joao.silva";

    Solicitacao solicitacao1 = criarSolicitacao(1L, usuario);
    solicitacao1.setDescricao("Solicitação 1");

    Solicitacao solicitacao2 = criarSolicitacao(2L, usuario);
    solicitacao2.setDescricao("Solicitação 2");

    SolicitacaoResponseDto dto1 = criarSolicitacaoResponseDto(1L, usuarioResponseDto);
    dto1.setDescricao("Solicitação 1");

    SolicitacaoResponseDto dto2 = criarSolicitacaoResponseDto(2L, usuarioResponseDto);
    dto2.setDescricao("Solicitação 2");

    List<Solicitacao> solicitacoes = Arrays.asList(solicitacao1, solicitacao2);

    when(usuarioService.findByUsername(username)).thenReturn(usuarioResponseDto);
    when(usuarioService.toEntity(usuarioResponseDto)).thenReturn(usuario);
    when(solicitacaoRepository.findAllByUsuario(usuario)).thenReturn(solicitacoes);
    when(modelMapper.map(solicitacao1, SolicitacaoResponseDto.class)).thenReturn(dto1);
    when(modelMapper.map(solicitacao2, SolicitacaoResponseDto.class)).thenReturn(dto2);

    // When
    List<SolicitacaoResponseDto> result = solicitacaoService.findAllByUsername(username);

    // Then
    assertNotNull(result);
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getDescricao()).isEqualTo("Solicitação 1");
    assertThat(result.get(1).getDescricao()).isEqualTo("Solicitação 2");
    verify(usuarioService).findByUsername(username);
    verify(usuarioService).toEntity(usuarioResponseDto);
    verify(solicitacaoRepository).findAllByUsuario(usuario);
  }

  @Test
  void testFindAllByUsername_SemSolicitacoes_DeveRetornarListaVazia() {
    // Given
    String username = "maria.santos";

    when(usuarioService.findByUsername(username)).thenReturn(usuarioResponseDto);
    when(usuarioService.toEntity(usuarioResponseDto)).thenReturn(usuario);
    when(solicitacaoRepository.findAllByUsuario(usuario)).thenReturn(Collections.emptyList());

    // When
    List<SolicitacaoResponseDto> result = solicitacaoService.findAllByUsername(username);

    // Then
    assertNotNull(result);
    assertThat(result).isEmpty();
    verify(usuarioService).findByUsername(username);
    verify(usuarioService).toEntity(usuarioResponseDto);
    verify(solicitacaoRepository).findAllByUsuario(usuario);
  }

  @Test
  void testFindAllByUsername_ComUmaSolicitacao_DeveRetornarLista() {
    // Given
    String username = "pedro.costa";

    Solicitacao solicitacaoUnica = criarSolicitacao(10L, usuario);
    solicitacaoUnica.setDescricao("Solicitação única");

    SolicitacaoResponseDto dtoUnico = criarSolicitacaoResponseDto(10L, usuarioResponseDto);
    dtoUnico.setDescricao("Solicitação única");

    when(usuarioService.findByUsername(username)).thenReturn(usuarioResponseDto);
    when(usuarioService.toEntity(usuarioResponseDto)).thenReturn(usuario);
    when(solicitacaoRepository.findAllByUsuario(usuario))
        .thenReturn(Collections.singletonList(solicitacaoUnica));
    when(modelMapper.map(solicitacaoUnica, SolicitacaoResponseDto.class)).thenReturn(dtoUnico);

    // When
    List<SolicitacaoResponseDto> result = solicitacaoService.findAllByUsername(username);

    // Then
    assertNotNull(result);
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(10L);
    assertThat(result.get(0).getDescricao()).isEqualTo("Solicitação única");
    verify(solicitacaoRepository).findAllByUsuario(usuario);
  }

  @Test
  void testFindAllByUsername_DeveConverterTodasAsSolicitacoes() {
    // Given
    String username = "ana.oliveira";

    Solicitacao sol1 = criarSolicitacao(1L, usuario);
    Solicitacao sol2 = criarSolicitacao(2L, usuario);
    Solicitacao sol3 = criarSolicitacao(3L, usuario);

    SolicitacaoResponseDto dto1 = criarSolicitacaoResponseDto(1L, usuarioResponseDto);
    SolicitacaoResponseDto dto2 = criarSolicitacaoResponseDto(2L, usuarioResponseDto);
    SolicitacaoResponseDto dto3 = criarSolicitacaoResponseDto(3L, usuarioResponseDto);

    List<Solicitacao> solicitacoes = Arrays.asList(sol1, sol2, sol3);

    when(usuarioService.findByUsername(username)).thenReturn(usuarioResponseDto);
    when(usuarioService.toEntity(usuarioResponseDto)).thenReturn(usuario);
    when(solicitacaoRepository.findAllByUsuario(usuario)).thenReturn(solicitacoes);
    when(modelMapper.map(sol1, SolicitacaoResponseDto.class)).thenReturn(dto1);
    when(modelMapper.map(sol2, SolicitacaoResponseDto.class)).thenReturn(dto2);
    when(modelMapper.map(sol3, SolicitacaoResponseDto.class)).thenReturn(dto3);

    // When
    List<SolicitacaoResponseDto> result = solicitacaoService.findAllByUsername(username);

    // Then
    assertNotNull(result);
    assertThat(result).hasSize(3);
    verify(modelMapper, times(3)).map(any(Solicitacao.class), eq(SolicitacaoResponseDto.class));
  }

  @Test
  void testFindAllByUsername_DeveChamarServicosNaOrdemCorreta() {
    // Given
    String username = "carlos.pereira";

    when(usuarioService.findByUsername(username)).thenReturn(usuarioResponseDto);
    when(usuarioService.toEntity(usuarioResponseDto)).thenReturn(usuario);
    when(solicitacaoRepository.findAllByUsuario(usuario)).thenReturn(Collections.emptyList());

    // When
    solicitacaoService.findAllByUsername(username);

    // Then
    var inOrder = inOrder(usuarioService, solicitacaoRepository);
    inOrder.verify(usuarioService).findByUsername(username);
    inOrder.verify(usuarioService).toEntity(usuarioResponseDto);
    inOrder.verify(solicitacaoRepository).findAllByUsuario(usuario);
  }

  @Test
  void testFindAllByUsername_ComDiferentesUsuarios_DeveRetornarSolicitacoesCorretas() {
    // Given
    String username1 = "usuario1";

    Usuario usuario1 = criarUsuario(1L, username1);

    UsuarioResponseDto userDto1 = criarUsuarioResponseDto(1L, username1);

    Solicitacao sol1 = criarSolicitacao(1L, usuario1);

    SolicitacaoResponseDto dto1 = criarSolicitacaoResponseDto(1L, userDto1);

    // Configurar mocks para usuario1
    when(usuarioService.findByUsername(username1)).thenReturn(userDto1);
    when(usuarioService.toEntity(userDto1)).thenReturn(usuario1);
    when(solicitacaoRepository.findAllByUsuario(usuario1))
        .thenReturn(Collections.singletonList(sol1));
    when(modelMapper.map(sol1, SolicitacaoResponseDto.class)).thenReturn(dto1);

    // When
    List<SolicitacaoResponseDto> result1 = solicitacaoService.findAllByUsername(username1);

    // Then
    assertNotNull(result1);
    assertThat(result1).hasSize(1);
    assertThat(result1.get(0).getId()).isEqualTo(1L);
    verify(solicitacaoRepository).findAllByUsuario(usuario1);
  }

  @Test
  void testFindAllByUsername_ComSolicitacoesComObservacoes_DeveRetornarCompleto() {
    // Given
    String username = "teste.usuario";

    Solicitacao sol = criarSolicitacao(1L, usuario);
    sol.setDescricao("Descrição teste");
    sol.setObservacao("Observação importante");

    SolicitacaoResponseDto dto = criarSolicitacaoResponseDto(1L, usuarioResponseDto);
    dto.setDescricao("Descrição teste");
    dto.setObservacao("Observação importante");

    when(usuarioService.findByUsername(username)).thenReturn(usuarioResponseDto);
    when(usuarioService.toEntity(usuarioResponseDto)).thenReturn(usuario);
    when(solicitacaoRepository.findAllByUsuario(usuario))
        .thenReturn(Collections.singletonList(sol));
    when(modelMapper.map(sol, SolicitacaoResponseDto.class)).thenReturn(dto);

    // When
    List<SolicitacaoResponseDto> result = solicitacaoService.findAllByUsername(username);

    // Then
    assertNotNull(result);
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getObservacao()).isEqualTo("Observação importante");
  }

  // Métodos auxiliares para criar objetos de teste

  private Solicitacao criarSolicitacao(Long id, Usuario usuario) {
    Solicitacao solicitacao = new Solicitacao();
    solicitacao.setId(id);
    solicitacao.setDescricao("Solicitação de teste");
    solicitacao.setDataSolicitacao(LocalDate.now());
    solicitacao.setUsuario(usuario);
    solicitacao.setSolicitacaoItem(Collections.emptyList());
    return solicitacao;
  }

  private SolicitacaoResponseDto criarSolicitacaoResponseDto(
      Long id, UsuarioResponseDto usuarioDto) {
    SolicitacaoResponseDto dto = new SolicitacaoResponseDto();
    dto.setId(id);
    dto.setDescricao("Solicitação de teste");
    dto.setDataSolicitacao(LocalDate.now());
    dto.setUsuario(usuarioDto);
    dto.setSolicitacaoItem(Collections.emptyList());
    return dto;
  }

  private Usuario criarUsuario(Long id, String username) {
    Usuario usuario = new Usuario();
    usuario.setId(id);
    usuario.setUsername(username);
    usuario.setNome("Nome " + username);
    return usuario;
  }

  private UsuarioResponseDto criarUsuarioResponseDto(Long id, String username) {
    UsuarioResponseDto dto = new UsuarioResponseDto();
    dto.setId(id);
    dto.setUsername(username);
    dto.setNome("Nome " + username);
    return dto;
  }
}
