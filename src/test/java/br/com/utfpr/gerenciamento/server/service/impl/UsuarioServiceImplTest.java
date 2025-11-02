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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
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

  @InjectMocks private UsuarioServiceImpl usuarioService;

  private Usuario usuario;
  private Permissao permissao1;
  private Permissao permissao2;

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

    // Mock para preservar emailVerificado quando usuário já existe (tem ID)
    when(usuarioRepository.findByUsername(anyString())).thenReturn(usuario);
  }

  @Test
  void save_DeveHandlePermissoesNull() {
    // Given: Usuário sem permissões (null)
    usuario.setPermissoes(null);
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

    // When
    Usuario resultado = usuarioService.toEntity( usuarioService.save(usuario));

    // Then: Deve criar Set vazio, não lançar NPE
    assertNotNull(resultado);
    assertNotNull(resultado.getPermissoes());
    assertTrue(resultado.getPermissoes().isEmpty());
    verify(permissaoService, never()).findAllById(any());
  }

  @Test
  void save_DeveHandlePermissoesVazias() {
    // Given: Usuário com Set vazio de permissões
    usuario.setPermissoes(new HashSet<>());
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

    // When
    Usuario resultado = usuarioService.toEntity( usuarioService.save(usuario));

    // Then: Deve manter Set vazio
    assertNotNull(resultado);
    assertNotNull(resultado.getPermissoes());
    assertTrue(resultado.getPermissoes().isEmpty());
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

    when(permissaoService.findAllById(any())).thenReturn(Collections.singletonList(permissao1).stream().map(permissaoService::toDto).toList());
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

    // When
    Usuario resultado = usuarioService.toEntity( usuarioService.save(usuario));

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

    when(permissaoService.findAllById(any())).thenReturn(Collections.singletonList(permissao1).stream().map(permissaoService::toDto).toList());
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

    // When
    Usuario resultado = usuarioService.toEntity( usuarioService.save(usuario));

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

    List<Permissao> permissoesResolvidas = Arrays.asList(permissao1, permissao2);
    when(permissaoService.findAllById(any())).thenReturn(permissoesResolvidas.stream().map(permissaoService::toDto).toList());
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

    // When
    Usuario resultado = usuarioService.toEntity( usuarioService.save(usuario));

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

    // Verifica que as permissões foram corretamente resolvidas
    assertNotNull(resultado.getPermissoes());
    assertEquals(2, resultado.getPermissoes().size());
  }

  @Test
  void save_DeveHandlePermissaoServiceRetornandoNull() {
    // Given: findAllById retorna lista vazia (permissões não encontradas)
    Set<Permissao> permissoes = new HashSet<>();
    permissoes.add(permissao1);

    usuario.setPermissoes(permissoes);

    when(permissaoService.findAllById(any())).thenReturn(Collections.emptyList());
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

    // When
    Usuario resultado = usuarioService.toEntity( usuarioService.save(usuario));

    // Then: Deve criar Set vazio, não lançar exceção
    assertNotNull(resultado);
    assertNotNull(resultado.getPermissoes());
    assertTrue(resultado.getPermissoes().isEmpty());
  }

  @Test
  void save_DeveResolverPermissoesCorretamente() {
    // Given: Cenário normal com permissões válidas
    Set<Permissao> permissoesInput = new HashSet<>();
    permissoesInput.add(permissao1);

    usuario.setPermissoes(permissoesInput);

    when(permissaoService.findAllById(any())).thenReturn(Collections.singletonList(permissao1).stream().map(permissaoService::toDto).toList());
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

    // When
    Usuario resultado = usuarioService.toEntity( usuarioService.save(usuario));

    // Then: Permissões devem ser resolvidas e atribuídas ao usuário
    assertNotNull(resultado);
    assertNotNull(resultado.getPermissoes());
    assertEquals(1, resultado.getPermissoes().size());
    assertTrue(resultado.getPermissoes().contains(permissao1));

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

    // When
    Usuario resultado = usuarioService.toEntity( usuarioService.save(usuario));

    // Then: Senha deve ser encodada
    assertNotNull(resultado);
    verify(passwordEncoder).encode("senha123");
    verify(usuarioRepository).save(argThat(u -> u.getPassword().equals("$2a$10$encodedPassword")));
  }

  @Test
  void save_NaoDeveReencodarPasswordJaEncodada() {
    // Given: Usuário com senha já encodada (formato BCrypt válido)
    String senhaJaEncodada =
        "$2a$10$N9qo8uLOickgx2ZMRZoMye/IVI9lvfjv4LvvQm.0M9cJXH/3u4bly"; // BCrypt válido
    usuario.setPassword(senhaJaEncodada);

    when(permissaoService.findAllById(any())).thenReturn(Collections.emptyList());
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

    // When
    Usuario resultado = usuarioService.toEntity( usuarioService.save(usuario));

    // Then: Senha não deve ser re-encodada
    assertNotNull(resultado);
    verify(passwordEncoder, never()).encode(anyString());
    verify(usuarioRepository).save(argThat(u -> u.getPassword().equals(senhaJaEncodada)));
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
        "Senha alterada. Já é possível autenticar-se com a nova senha.", response.getMessage());
    verify(passwordEncoder).encode("novaSenha123");
    verify(usuarioRepository)
        .save(argThat(u -> u.getPassword().equals("$2a$10$encodedNewPassword")));
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
    assertThrows(RuntimeException.class, () -> usuarioService.resetPassword(requestDto));

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

    // Mock findOne para retornar usuarioExistente
    UsuarioServiceImpl spyService = spy(usuarioService);
    doReturn(usuarioExistente).when(spyService).findOne(1L);
    when(passwordEncoder.matches("senhaAtual", "$2a$10$senhaAntigaEncodada")).thenReturn(true);
    when(passwordEncoder.encode("novaSenha123")).thenReturn("$2a$10$novaSenhaEncodada");
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioExistente);

    Usuario resultado = usuarioService.toEntity( spyService.updatePassword(usuarioAtualizado, "senhaAtual"));
    assertNotNull(resultado);
    assertEquals("$2a$10$novaSenhaEncodada", resultado.getPassword());
    assertTrue(resultado.getEmailVerificado());
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
        RuntimeException.class,
        () -> usuarioService.updatePassword(usuarioAtualizado, "senhaErrada"));

    verify(passwordEncoder, never()).encode(anyString());
    verify(usuarioRepository, never()).save(any(Usuario.class));
  }

  @Test
  void testFindByUsername() {
    when(usuarioRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(usuario);
    Usuario result = usuarioService.toEntity( usuarioService.findByUsername("usuario@utfpr.edu.br"));
    assertNotNull(result);
    assertEquals("usuario@utfpr.edu.br", result.getUsername());
  }

  @Test
  void testFindByUsernameForAuthentication() {
    Usuario usuarioMock = new Usuario();
    usuarioMock.setId(1L);
    usuarioMock.setUsername("user@utfpr.edu.br");
    usuarioMock.setPermissoes(new HashSet<>());
    when(usuarioRepository.findWithPermissoesByUsernameOrEmail(anyString(), anyString()))
        .thenReturn(usuarioMock);
    Usuario result = usuarioService.toEntity( usuarioService.findByUsernameForAuthentication("user@utfpr.edu.br"));
    assertNotNull(result);
    assertEquals("user@utfpr.edu.br", result.getUsername());
  }

  @Test
  void testSaveUsuarioWithPermissoes() {
    usuario.setPermissoes(Set.of(permissao1, permissao2));
    when(permissaoService.findAllById(anySet())).thenReturn(List.of(permissao1, permissao2).stream().map(permissaoService::toDto).toList());
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
    Usuario result = usuarioService.toEntity( usuarioService.save(usuario));
    assertNotNull(result);
    assertEquals(2, result.getPermissoes().size());
  }

  @Test
  void testSaveUsuarioWithoutPermissoes() {
    usuario.setPermissoes(null);
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
    Usuario result = usuarioService.toEntity( usuarioService.save(usuario));
    assertNotNull(result);
    assertEquals(0, result.getPermissoes().size());
  }

  @Test
  void testSaveNewUserAluno() {
    usuario.setEmail("aluno@dominio.com");
    usuario.setPassword("senha");
    when(permissaoService.findByNome(anyString())).thenReturn(permissao1);
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
    Usuario result = usuarioService.toEntity(usuarioService.saveNewUser(usuario));
    assertNotNull(result);
    assertFalse(result.getEmailVerificado());
    assertEquals(1, result.getPermissoes().size());
  }

  @Test
  void testUpdateUsuario() {
    // Configura contexto de autenticação
    org.springframework.security.core.context.SecurityContextHolder.getContext()
        .setAuthentication(
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "usuario@utfpr.edu.br", null));
    when(usuarioRepository.findByUsername(anyString())).thenReturn(usuario);
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
    usuario.setTelefone("123456789");
    Usuario result = usuarioService.toEntity( usuarioService.updateUsuario(usuario));
    assertNotNull(result);
    assertEquals("123456789", result.getTelefone());
  }

  @Test
  void testUpdatePasswordSuccess() {
    Usuario usuarioExistente = new Usuario();
    usuarioExistente.setId(1L);
    usuarioExistente.setPassword("encodedSenhaAtual");
    usuarioExistente.setEmailVerificado(true);
    usuarioExistente.setUsername("usuario@utfpr.edu.br");
    usuarioExistente.setEmail("usuario@utfpr.edu.br");

    Usuario usuarioAtualizado = new Usuario();
    usuarioAtualizado.setId(1L);
    usuarioAtualizado.setPassword("novaSenha");

    // Spy para mockar findOne
    UsuarioServiceImpl spyService = spy(usuarioService);
    doReturn(usuarioExistente).when(spyService).findOne(1L);
    when(passwordEncoder.matches("senhaAtual", "encodedSenhaAtual")).thenReturn(true);
    when(passwordEncoder.encode("novaSenha")).thenReturn("encodedNovaSenha");
    when(usuarioRepository.save(any(Usuario.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Usuario result = usuarioService.toEntity( spyService.updatePassword(usuarioAtualizado, "senhaAtual"));
    assertNotNull(result);
    assertEquals("encodedNovaSenha", result.getPassword());
    assertTrue(result.getEmailVerificado());
  }

  @Test
  void testUpdatePasswordFail() {
    usuario.setPassword("encodedSenha");
    when(usuarioRepository.findById(anyLong())).thenReturn(java.util.Optional.of(usuario));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
    Usuario novo = new Usuario();
    novo.setId(1L);
    novo.setPassword("novaSenha");
    assertThrows(RuntimeException.class, () -> usuarioService.updatePassword(novo, "senhaErrada"));
  }
}
