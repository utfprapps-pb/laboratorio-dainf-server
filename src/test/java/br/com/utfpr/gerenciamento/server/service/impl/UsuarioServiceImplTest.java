package br.com.utfpr.gerenciamento.server.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.model.Permissao;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.repository.NadaConstaRepository;
import br.com.utfpr.gerenciamento.server.repository.RecoverPasswordRepository;
import br.com.utfpr.gerenciamento.server.repository.UsuarioRepository;
import br.com.utfpr.gerenciamento.server.service.EmailService;
import br.com.utfpr.gerenciamento.server.service.PermissaoService;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UsuarioServiceImplTest {

  @Mock private UsuarioRepository usuarioRepository;

  @Mock private ModelMapper modelMapper;

  @Mock private RecoverPasswordRepository recoverPasswordRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private EmailService emailService;

  @Mock private PermissaoService permissaoService;

  @Mock private NadaConstaRepository nadaConstaRepository;

  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private UsuarioServiceImpl usuarioService;

  private Usuario usuario;
  private Permissao permissao1;
  private Permissao permissao2;
  private br.com.utfpr.gerenciamento.server.dto.PermissaoResponseDTO permissaoDto1;
  private br.com.utfpr.gerenciamento.server.dto.PermissaoResponseDTO permissaoDto2;

  @BeforeEach
  void setUp() {
    usuario = new Usuario();
    usuario.setId(1L);
    usuario.setNome("João Silva");
    usuario.setEmail("usuario@utfpr.edu.br");
    usuario.setUsername("usuario@utfpr.edu.br");
    usuario.setEmailVerificado(true);

    permissao1 = new Permissao();
    permissao1.setId(1L);
    permissao1.setNome("ROLE_ADMIN");

    permissao2 = new Permissao();
    permissao2.setId(2L);
    permissao2.setNome("ROLE_USER");

    // Criar DTOs correspondentes
    permissaoDto1 = new br.com.utfpr.gerenciamento.server.dto.PermissaoResponseDTO();
    permissaoDto1.setId(1L);
    permissaoDto1.setNome("ROLE_ADMIN");

    permissaoDto2 = new br.com.utfpr.gerenciamento.server.dto.PermissaoResponseDTO();
    permissaoDto2.setId(2L);
    permissaoDto2.setNome("ROLE_USER");

    // Mock para preservar emailVerificado quando usuário já existe (tem ID)
    when(usuarioRepository.findByUsername(anyString())).thenReturn(usuario);
    when(usuarioRepository.findById(anyLong())).thenReturn(java.util.Optional.of(usuario));

    // Mock para conversão de DTO para Entity no PermissaoService
    when(permissaoService.toEntity(
            any(br.com.utfpr.gerenciamento.server.dto.PermissaoResponseDTO.class)))
        .thenAnswer(
            invocation -> {
              br.com.utfpr.gerenciamento.server.dto.PermissaoResponseDTO dto =
                  invocation.getArgument(0);
              Permissao entity = new Permissao();
              entity.setId(dto.getId());
              entity.setNome(dto.getNome());
              return entity;
            });
  }

  @Test
  void save_DeveHandlePermissoesNull() {
    // Given: Usuário sem permissões (null)
    usuario.setPermissoes(null);
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
    when(modelMapper.map(any(Usuario.class), any()))
        .thenReturn(new br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto());

    // When
    br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto resultado =
        usuarioService.save(usuario);

    // Then: Deve criar Set vazio, não lançar NPE
    assertNotNull(resultado);
    verify(permissaoService, never()).findAllById(any());
  }

  @Test
  void save_DeveHandlePermissoesVazias() {
    // Given: Usuário com Set vazio de permissões
    usuario.setPermissoes(new HashSet<>());
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
    when(modelMapper.map(any(Usuario.class), any()))
        .thenReturn(new br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto());

    // When
    br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto resultado =
        usuarioService.save(usuario);

    // Then: Deve manter Set vazio
    assertNotNull(resultado);
    verify(permissaoService, never()).findAllById(any());
  }

  @Test
  void save_DeveIgnorarPermissoesComElementosNull() {
    // Given: Set contendo permissões null
    Set<Permissao> permissoesComNull = new HashSet<>();
    permissoesComNull.add(null);
    permissoesComNull.add(permissao1);
    permissoesComNull.add(null);

    usuario.setPermissoes(permissoesComNull);

    when(permissaoService.findAllById(any())).thenReturn(Collections.singletonList(permissaoDto1));
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
    when(modelMapper.map(any(Usuario.class), any()))
        .thenReturn(new br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto());

    // When
    br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto resultado =
        usuarioService.save(usuario);

    // Then: Deve filtrar nulls e processar apenas permissão válida
    assertNotNull(resultado);
    verify(permissaoService)
        .findAllById(
            argThat(
                ids -> {
                  List<Long> idList = new ArrayList<>();
                  ids.forEach(idList::add);
                  return idList.size() == 1 && idList.contains(1L);
                }));
  }

  @Test
  void save_DeveIgnorarPermissoesComIDsNull() {
    // Given: Permissões com IDs null
    Permissao permissaoSemId = new Permissao();
    permissaoSemId.setId(null);
    permissaoSemId.setNome("ROLE_INVALID");

    Set<Permissao> permissoes = new HashSet<>();
    permissoes.add(permissaoSemId);
    permissoes.add(permissao1);

    usuario.setPermissoes(permissoes);

    when(permissaoService.findAllById(any())).thenReturn(Collections.singletonList(permissaoDto1));
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
    when(modelMapper.map(any(Usuario.class), any()))
        .thenReturn(new br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto());

    // When
    br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto resultado =
        usuarioService.save(usuario);

    // Then: Deve ignorar permissão sem ID e processar apenas a válida
    assertNotNull(resultado);
    verify(permissaoService)
        .findAllById(
            argThat(
                ids -> {
                  List<Long> idList = new ArrayList<>();
                  ids.forEach(idList::add);
                  return idList.size() == 1 && idList.contains(1L);
                }));
  }

  @Test
  void save_DeveUsarBatchFetchingParaMultiplasPermissoes() {
    // Given: Usuário com múltiplas permissões
    Set<Permissao> permissoes = new HashSet<>();
    permissoes.add(permissao1);
    permissoes.add(permissao2);

    usuario.setPermissoes(permissoes);

    List<br.com.utfpr.gerenciamento.server.dto.PermissaoResponseDTO> permissoesResolvidas =
        Arrays.asList(permissaoDto1, permissaoDto2);

    when(permissaoService.findAllById(any())).thenReturn(permissoesResolvidas);
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
    when(modelMapper.map(any(Usuario.class), any()))
        .thenReturn(new br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto());

    // When
    br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto resultado =
        usuarioService.save(usuario);

    // Then: Deve chamar findAllById UMA VEZ (batch), não N vezes
    assertNotNull(resultado);
    verify(permissaoService, times(1))
        .findAllById(
            argThat(
                ids -> {
                  List<Long> idList = new ArrayList<>();
                  ids.forEach(idList::add);
                  return idList.size() == 2 && idList.contains(1L) && idList.contains(2L);
                }));
    verify(permissaoService, never()).findOne(anyLong()); // Não deve usar findOne individual
  }

  @Test
  void save_DeveHandlePermissaoServiceRetornandoNull() {
    // Given: findAllById retorna lista vazia (permissões não encontradas)
    Set<Permissao> permissoes = new HashSet<>();
    permissoes.add(permissao1);

    usuario.setPermissoes(permissoes);

    when(permissaoService.findAllById(any())).thenReturn(Collections.emptyList());
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
    when(modelMapper.map(any(Usuario.class), any()))
        .thenReturn(new br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto());

    // When
    br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto resultado =
        usuarioService.save(usuario);

    // Then: Deve criar Set vazio, não lançar exceção
    assertNotNull(resultado);
  }

  @Test
  void save_DeveResolverPermissoesCorretamente() {
    // Given: Cenário normal com permissões válidas
    Set<Permissao> permissoesInput = new HashSet<>();
    permissoesInput.add(permissao1);

    usuario.setPermissoes(permissoesInput);

    when(permissaoService.findAllById(any())).thenReturn(Collections.singletonList(permissaoDto1));
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
    when(modelMapper.map(any(Usuario.class), any()))
        .thenReturn(new br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto());

    // When
    br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto resultado =
        usuarioService.save(usuario);

    // Then: Permissões devem ser resolvidas e atribuídas ao usuário
    assertNotNull(resultado);
    verify(permissaoService)
        .findAllById(
            argThat(
                ids -> {
                  List<Long> idList = new ArrayList<>();
                  ids.forEach(idList::add);
                  return idList.contains(1L);
                }));
    verify(usuarioRepository).save(usuario);
  }

  @Test
  void save_DeveEncodarPasswordSeNaoEstiverEncodada() {
    // Given: Usuário com senha em texto plano
    usuario.setPassword("senha123");

    when(permissaoService.findAllById(any())).thenReturn(Collections.emptyList());
    when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
    when(modelMapper.map(any(Usuario.class), any()))
        .thenReturn(new br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto());

    // When
    br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto resultado =
        usuarioService.save(usuario);

    // Then: Senha deve ser encodada
    assertNotNull(resultado);
    verify(passwordEncoder).encode("senha123");
  }

  @Test
  void save_NaoDeveReencodarPasswordJaEncodada() {
    // Given: Usuário com senha já encodada (formato BCrypt válido)
    String senhaJaEncodada =
        "$2a$10$N9qo8uLOickgx2ZMRZoMye/IVI9lvfjv4LvvQm.0M9cJXH/3u4bly"; // BCrypt válido
    usuario.setPassword(senhaJaEncodada);

    when(permissaoService.findAllById(any())).thenReturn(Collections.emptyList());
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
    when(modelMapper.map(any(Usuario.class), any()))
        .thenReturn(new br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto());

    // When
    br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto resultado =
        usuarioService.save(usuario);

    // Then: Senha não deve ser re-encodada
    assertNotNull(resultado);
    verify(passwordEncoder, never()).encode(anyString());
  }

  @Test
  void confirmEmail_DeveConfirmarEmailComCodigoValido() {
    // Given
    br.com.utfpr.gerenciamento.server.dto.ConfirmEmailRequestDto requestDto =
        new br.com.utfpr.gerenciamento.server.dto.ConfirmEmailRequestDto();
    requestDto.setCode("codigo-valido-123");

    Usuario usuarioMock = new Usuario();
    usuarioMock.setId(1L);
    usuarioMock.setEmailVerificado(false);

    when(usuarioRepository.findByCodigoVerificacao("codigo-valido-123")).thenReturn(usuarioMock);
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioMock);

    // When
    br.com.utfpr.gerenciamento.server.dto.GenericResponse response =
        usuarioService.confirmEmail(requestDto);

    // Then
    assertNotNull(response);
    assertEquals("O email do usuário foi confirmado.", response.getMessage());
    verify(usuarioRepository).save(argThat(Usuario::getEmailVerificado));
  }

  @Test
  void confirmEmail_DeveLancarExcecaoComCodigoInvalido() {
    // Given
    br.com.utfpr.gerenciamento.server.dto.ConfirmEmailRequestDto requestDto =
        new br.com.utfpr.gerenciamento.server.dto.ConfirmEmailRequestDto();
    requestDto.setCode("codigo-invalido");

    when(usuarioRepository.findByCodigoVerificacao("codigo-invalido")).thenReturn(null);

    // When/Then
    assertThrows(
        br.com.utfpr.gerenciamento.server.exception.RecoverCodeInvalidException.class,
        () -> usuarioService.confirmEmail(requestDto));

    verify(usuarioRepository, never()).save(any(Usuario.class));
  }

  @Test
  void resetPassword_DeveResetarSenhaComCodigoValido() {
    // Given
    br.com.utfpr.gerenciamento.server.dto.RecoverPasswordRequestDto requestDto =
        new br.com.utfpr.gerenciamento.server.dto.RecoverPasswordRequestDto();
    requestDto.setCode("recover-code-123");
    requestDto.setPassword("novaSenha123");
    requestDto.setRepeatPassword("novaSenha123");

    br.com.utfpr.gerenciamento.server.model.RecoverPassword recoverPassword =
        new br.com.utfpr.gerenciamento.server.model.RecoverPassword();
    recoverPassword.setEmail("teste@test.com");
    recoverPassword.setDateTime(java.time.LocalDateTime.now()); // Código não expirado

    Usuario usuarioMock = new Usuario();
    usuarioMock.setEmail("teste@test.com");

    when(recoverPasswordRepository.findByCode("recover-code-123")).thenReturn(recoverPassword);
    when(usuarioRepository.findByEmail("teste@test.com")).thenReturn(usuarioMock);
    when(passwordEncoder.encode("novaSenha123")).thenReturn("$2a$10$encodedNewPassword");
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioMock);

    // When
    br.com.utfpr.gerenciamento.server.dto.GenericResponse response =
        usuarioService.resetPassword(requestDto);

    // Then
    assertNotNull(response);
    assertEquals(
        "Senha alterada com sucesso. Você já pode fazer login com a nova senha.",
        response.getMessage());
    verify(passwordEncoder).encode("novaSenha123");
  }

  @Test
  void resetPassword_DeveLancarExcecaoQuandoSenhasNaoCoincidem() {
    // Given
    br.com.utfpr.gerenciamento.server.dto.RecoverPasswordRequestDto requestDto =
        new br.com.utfpr.gerenciamento.server.dto.RecoverPasswordRequestDto();
    requestDto.setCode("recover-code-123");
    requestDto.setPassword("senha1");
    requestDto.setRepeatPassword("senha2"); // Diferente

    br.com.utfpr.gerenciamento.server.model.RecoverPassword recoverPassword =
        new br.com.utfpr.gerenciamento.server.model.RecoverPassword();
    recoverPassword.setEmail("teste@test.com");

    Usuario usuarioMock = new Usuario();
    when(recoverPasswordRepository.findByCode("recover-code-123")).thenReturn(recoverPassword);
    when(usuarioRepository.findByEmail("teste@test.com")).thenReturn(usuarioMock);

    // When/Then
    assertThrows(
        br.com.utfpr.gerenciamento.server.exception.InvalidPasswordException.class,
        () -> usuarioService.resetPassword(requestDto));

    verify(passwordEncoder, never()).encode(anyString());
    verify(usuarioRepository, never()).save(any(Usuario.class));
  }

  @Test
  void resetPassword_DeveLancarExcecaoComCodigoInvalido() {
    // Given
    br.com.utfpr.gerenciamento.server.dto.RecoverPasswordRequestDto requestDto =
        new br.com.utfpr.gerenciamento.server.dto.RecoverPasswordRequestDto();
    requestDto.setCode("codigo-invalido");
    requestDto.setPassword("senha123");
    requestDto.setRepeatPassword("senha123");

    when(recoverPasswordRepository.findByCode("codigo-invalido")).thenReturn(null);

    // When/Then
    assertThrows(
        br.com.utfpr.gerenciamento.server.exception.RecoverCodeInvalidException.class,
        () -> usuarioService.resetPassword(requestDto));

    verify(usuarioRepository, never()).save(any(Usuario.class));
  }

  @Test
  void updatePassword_DeveAtualizarSenhaComSenhaAtualCorreta() {
    Usuario usuarioExistente = new Usuario();
    usuarioExistente.setId(1L);
    usuarioExistente.setPassword("$2a$10$senhaAntigaEncodada");
    usuarioExistente.setEmailVerificado(true);

    Usuario usuarioAtualizado = new Usuario();
    usuarioAtualizado.setId(1L);
    usuarioAtualizado.setPassword("novaSenha123");

    // Mock repository para retornar usuarioExistente
    when(usuarioRepository.findById(1L)).thenReturn(java.util.Optional.of(usuarioExistente));
    when(passwordEncoder.matches("senhaAtual", "$2a$10$senhaAntigaEncodada")).thenReturn(true);
    when(passwordEncoder.encode("novaSenha123")).thenReturn("$2a$10$novaSenhaEncodada");
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioExistente);
    when(modelMapper.map(any(Usuario.class), any()))
        .thenReturn(new br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto());

    br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto resultado =
        usuarioService.updatePassword(usuarioAtualizado, "senhaAtual");

    assertNotNull(resultado);
    verify(passwordEncoder).encode("novaSenha123");
  }

  @Test
  void updatePassword_DeveLancarExcecaoComSenhaAtualIncorreta() {
    // Given
    Usuario usuarioExistente = new Usuario();
    usuarioExistente.setId(1L);
    usuarioExistente.setPassword("$2a$10$senhaAntigaEncodada");

    Usuario usuarioAtualizado = new Usuario();
    usuarioAtualizado.setId(1L);
    usuarioAtualizado.setPassword("novaSenha123");

    when(usuarioRepository.findById(1L)).thenReturn(java.util.Optional.of(usuarioExistente));
    when(passwordEncoder.matches("senhaErrada", "$2a$10$senhaAntigaEncodada")).thenReturn(false);

    // When/Then
    assertThrows(
        br.com.utfpr.gerenciamento.server.exception.InvalidPasswordException.class,
        () -> usuarioService.updatePassword(usuarioAtualizado, "senhaErrada"));

    verify(passwordEncoder, never()).encode(anyString());
    verify(usuarioRepository, never()).save(any(Usuario.class));
  }

  @Test
  void testFindByUsername() {
    when(usuarioRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(usuario);
    when(modelMapper.map(any(Usuario.class), any()))
        .thenReturn(new br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto());

    br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto result =
        usuarioService.findByUsername("usuario@utfpr.edu.br");

    assertNotNull(result);
  }

  @Test
  void testFindByUsernameForAuthentication() {
    Usuario usuarioMock = new Usuario();
    usuarioMock.setId(1L);
    usuarioMock.setUsername("user@utfpr.edu.br");
    usuarioMock.setPermissoes(new HashSet<>());

    when(usuarioRepository.findWithPermissoesByUsernameOrEmail(anyString(), anyString()))
        .thenReturn(usuarioMock);
    when(modelMapper.map(any(Usuario.class), any()))
        .thenReturn(new br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto());

    br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto result =
        usuarioService.findByUsernameForAuthentication("user@utfpr.edu.br");

    assertNotNull(result);
  }

  @Test
  void testSaveUsuarioWithPermissoes() {
    usuario.setPermissoes(Set.of(permissao1, permissao2));
    List<br.com.utfpr.gerenciamento.server.dto.PermissaoResponseDTO> permissoesDto =
        List.of(permissaoDto1, permissaoDto2);

    when(permissaoService.findAllById(anySet())).thenReturn(permissoesDto);
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
    when(modelMapper.map(any(Usuario.class), any()))
        .thenReturn(new br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto());

    br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto result = usuarioService.save(usuario);

    assertNotNull(result);
  }

  @Test
  void testSaveUsuarioWithoutPermissoes() {
    usuario.setPermissoes(null);
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
    when(modelMapper.map(any(Usuario.class), any()))
        .thenReturn(new br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto());

    br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto result = usuarioService.save(usuario);

    assertNotNull(result);
  }

  @Test
  void loadUserByUsername_DeveCarregarUsuarioPorUsername() {
    // Given
    String username = "usuario@utfpr.edu.br";
    Usuario usuarioMock = new Usuario();
    usuarioMock.setUsername(username);
    usuarioMock.setPermissoes(new HashSet<>());

    when(usuarioRepository.findWithPermissoesByUsernameOrEmail(username, username))
        .thenReturn(usuarioMock);

    // When
    org.springframework.security.core.userdetails.UserDetails resultado =
        usuarioService.loadUserByUsername(username);

    // Then
    assertNotNull(resultado);
    assertEquals(username, resultado.getUsername());
  }

  @Test
  void loadUserByUsername_DeveLancarExcecaoQuandoUsuarioNaoEncontrado() {
    // Given
    String username = "inexistente@test.com";
    when(usuarioRepository.findWithPermissoesByUsernameOrEmail(username, username))
        .thenReturn(null);

    // When/Then
    assertThrows(
        org.springframework.security.core.userdetails.UsernameNotFoundException.class,
        () -> usuarioService.loadUserByUsername(username));
  }

  @Test
  void updatePassword_DeveLancarExcecaoQuandoUsuarioNaoEncontrado() {
    // Given
    Usuario usuarioAtualizado = new Usuario();
    usuarioAtualizado.setId(999L);
    usuarioAtualizado.setPassword("novaSenha");

    when(usuarioRepository.findById(999L)).thenReturn(java.util.Optional.empty());

    // When/Then
    assertThrows(
        br.com.utfpr.gerenciamento.server.exception.EntityNotFoundException.class,
        () -> usuarioService.updatePassword(usuarioAtualizado, "senhaAtual"));
  }

  @Test
  void save_DevePreservarSenhaExistenteQuandoNovaSenhaNull() {
    // Given: Usuário existente com senha codificada
    String senhaAntiga = "$2a$10$senhaAntigaEncodada";
    usuario.setPassword(senhaAntiga);

    Usuario usuarioUpdate = new Usuario();
    usuarioUpdate.setId(1L);
    usuarioUpdate.setPassword(null); // Nova senha é null

    when(usuarioRepository.findById(1L)).thenReturn(java.util.Optional.of(usuario));
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
    when(modelMapper.map(any(Usuario.class), any()))
        .thenReturn(new br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto());

    // When
    br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto resultado =
        usuarioService.save(usuarioUpdate);

    // Then: Senha antiga deve ser preservada
    assertNotNull(resultado);
    verify(passwordEncoder, never()).encode(anyString());

    // Capture the Usuario passed to save and assert password is preserved
    ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
    verify(usuarioRepository).save(usuarioCaptor.capture());
    Usuario capturedUsuario = usuarioCaptor.getValue();
    assertEquals(senhaAntiga, capturedUsuario.getPassword());
  }

  @Test
  void save_DeveCodificarNovaSenhaParaUsuarioNovo() {
    // Given: Usuário novo sem ID
    Usuario novoUsuario = new Usuario();
    novoUsuario.setId(null);
    novoUsuario.setPassword("senhaNova");

    when(usuarioRepository.save(any(Usuario.class))).thenReturn(novoUsuario);
    when(passwordEncoder.encode("senhaNova")).thenReturn("$2a$10$senhaCodificada");
    when(modelMapper.map(any(Usuario.class), any()))
        .thenReturn(new br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto());

    // When
    br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto resultado =
        usuarioService.save(novoUsuario);

    // Then: Senha deve ser codificada
    assertNotNull(resultado);
    verify(passwordEncoder).encode("senhaNova");
    // Capture the Usuario passed to save and assert password is encoded
    ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
    verify(usuarioRepository).save(usuarioCaptor.capture());
    Usuario capturedUsuario = usuarioCaptor.getValue();
    assertEquals("$2a$10$senhaCodificada", capturedUsuario.getPassword());
  }
}
