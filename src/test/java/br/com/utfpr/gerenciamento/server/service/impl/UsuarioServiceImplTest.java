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

    // Mock repository para retornar usuarioExistente
    when(usuarioRepository.findById(1L)).thenReturn(java.util.Optional.of(usuarioExistente));
    when(passwordEncoder.matches("senhaAtual", "$2a$10$senhaAntigaEncodada")).thenReturn(true);
    when(passwordEncoder.encode("novaSenha123")).thenReturn("$2a$10$novaSenhaEncodada");
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioExistente);

    Usuario resultado = usuarioService.updatePassword(usuarioAtualizado, "senhaAtual");
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

    // Mock repository para retornar usuarioExistente
    when(usuarioRepository.findById(1L)).thenReturn(java.util.Optional.of(usuarioExistente));
    when(passwordEncoder.matches("senhaAtual", "encodedSenhaAtual")).thenReturn(true);
    when(passwordEncoder.encode("novaSenha")).thenReturn("encodedNovaSenha");
    when(usuarioRepository.save(any(Usuario.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Usuario result = usuarioService.updatePassword(usuarioAtualizado, "senhaAtual");
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

  // Testes para métodos não cobertos

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
  void loadUserByUsername_DeveNormalizarUsername() {
    // Given: username com subdomínio UTFPR
    String usernameComSubdominio = "prof@professores.utfpr.edu.br";
    String usernameNormalizado = "prof@utfpr.edu.br";
    Usuario usuarioMock = new Usuario();
    usuarioMock.setUsername(usernameNormalizado);
    usuarioMock.setPermissoes(new HashSet<>());

    when(usuarioRepository.findWithPermissoesByUsernameOrEmail(
            usernameNormalizado, usernameNormalizado))
        .thenReturn(usuarioMock);

    // When
    org.springframework.security.core.userdetails.UserDetails resultado =
        usuarioService.loadUserByUsername(usernameComSubdominio);

    // Then
    assertNotNull(resultado);
    verify(usuarioRepository)
        .findWithPermissoesByUsernameOrEmail(usernameNormalizado, usernameNormalizado);
  }

  @Test
  void usuarioComplete_DeveRetornarPaginaComFiltroTextual() {
    // Given
    String query = "João";
    org.springframework.data.domain.Pageable pageable =
        org.springframework.data.domain.PageRequest.of(0, 10);
    org.springframework.data.domain.Page<Usuario> pageMock =
        new org.springframework.data.domain.PageImpl<>(Collections.singletonList(usuario));

    when(usuarioRepository.findAll(
            any(org.springframework.data.jpa.domain.Specification.class), eq(pageable)))
        .thenReturn(pageMock);
    when(modelMapper.map(
            any(Usuario.class), eq(br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto.class)))
        .thenReturn(new br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto());

    // When
    org.springframework.data.domain.Page<br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto>
        resultado = usuarioService.usuarioComplete(query, pageable);

    // Then
    assertNotNull(resultado);
    assertEquals(1, resultado.getTotalElements());
  }

  @Test
  void usuarioComplete_DeveRetornarTodosUsuariosQuandoQueryNula() {
    // Given
    org.springframework.data.domain.Pageable pageable =
        org.springframework.data.domain.PageRequest.of(0, 10);
    org.springframework.data.domain.Page<Usuario> pageMock =
        new org.springframework.data.domain.PageImpl<>(Collections.singletonList(usuario));

    when(usuarioRepository.findAll(
            any(org.springframework.data.jpa.domain.Specification.class), eq(pageable)))
        .thenReturn(pageMock);
    when(modelMapper.map(
            any(Usuario.class), eq(br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto.class)))
        .thenReturn(new br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto());

    // When
    org.springframework.data.domain.Page<br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto>
        resultado = usuarioService.usuarioComplete(null, pageable);

    // Then
    assertNotNull(resultado);
  }

  @Test
  void resendEmail_DeveEnviarEmailQuandoUsuarioExisteENaoVerificado() {
    // Given
    br.com.utfpr.gerenciamento.server.dto.ConfirmEmailRequestDto requestDto =
        new br.com.utfpr.gerenciamento.server.dto.ConfirmEmailRequestDto();
    requestDto.setEmail("usuario@test.com");

    Usuario usuarioNaoVerificado = new Usuario();
    usuarioNaoVerificado.setEmail("usuario@test.com");
    usuarioNaoVerificado.setEmailVerificado(false);
    usuarioNaoVerificado.setCodigoVerificacao("codigo-123");

    when(usuarioRepository.findByEmail("usuario@test.com")).thenReturn(usuarioNaoVerificado);
    doNothing()
        .when(emailService)
        .sendEmailWithTemplate(any(), anyString(), anyString(), anyString());

    // When
    String resultado = usuarioService.resendEmail(requestDto);

    // Then
    assertNotNull(resultado);
    assertTrue(resultado.contains("reenviado"));
    verify(emailService)
        .sendEmailWithTemplate(
            any(), eq("usuario@test.com"), anyString(), eq("templateConfirmacaoCadastro"));
  }

  @Test
  void resendEmail_DeveRetornarMensagemQuandoEmailJaVerificado() {
    // Given
    br.com.utfpr.gerenciamento.server.dto.ConfirmEmailRequestDto requestDto =
        new br.com.utfpr.gerenciamento.server.dto.ConfirmEmailRequestDto();
    requestDto.setEmail("usuario@test.com");

    Usuario usuarioVerificado = new Usuario();
    usuarioVerificado.setEmail("usuario@test.com");
    usuarioVerificado.setEmailVerificado(true);

    when(usuarioRepository.findByEmail("usuario@test.com")).thenReturn(usuarioVerificado);

    // When
    String resultado = usuarioService.resendEmail(requestDto);

    // Then
    assertNotNull(resultado);
    assertTrue(resultado.contains("já foi confirmado"));
    verify(emailService, never())
        .sendEmailWithTemplate(any(), anyString(), anyString(), anyString());
  }

  @Test
  void resendEmail_DeveRetornarMensagemGenericaQuandoEmailNaoExiste() {
    // Given
    br.com.utfpr.gerenciamento.server.dto.ConfirmEmailRequestDto requestDto =
        new br.com.utfpr.gerenciamento.server.dto.ConfirmEmailRequestDto();
    requestDto.setEmail("inexistente@test.com");

    when(usuarioRepository.findByEmail("inexistente@test.com")).thenReturn(null);

    // When
    String resultado = usuarioService.resendEmail(requestDto);

    // Then
    assertNotNull(resultado);
    assertTrue(resultado.contains("Se o email existir"));
    verify(emailService, never())
        .sendEmailWithTemplate(any(), anyString(), anyString(), anyString());
  }

  @Test
  void sendEmailCodeRecoverPassword_DeveEnviarCodigoQuandoUsuarioExiste() {
    // Given
    String email = "usuario@test.com";
    Usuario usuarioMock = new Usuario();
    usuarioMock.setEmail(email);
    usuarioMock.setNome("Usuário Teste");

    when(usuarioRepository.findByEmail(email)).thenReturn(usuarioMock);
    when(recoverPasswordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    doNothing()
        .when(emailService)
        .sendEmailWithTemplate(any(), anyString(), anyString(), anyString());

    // When
    br.com.utfpr.gerenciamento.server.dto.GenericResponse resultado =
        usuarioService.sendEmailCodeRecoverPassword(email);

    // Then
    assertNotNull(resultado);
    assertTrue(resultado.getMessage().contains("solicitação foi enviada"));
    verify(recoverPasswordRepository).save(any());
    verify(emailService)
        .sendEmailWithTemplate(any(), eq(email), anyString(), eq("templateRecoverPassword"));
  }

  @Test
  void sendEmailCodeRecoverPassword_DeveRetornarMensagemGenericaQuandoUsuarioNaoExiste() {
    // Given
    String email = "inexistente@test.com";
    when(usuarioRepository.findByEmail(email)).thenReturn(null);

    // When
    br.com.utfpr.gerenciamento.server.dto.GenericResponse resultado =
        usuarioService.sendEmailCodeRecoverPassword(email);

    // Then
    assertNotNull(resultado);
    assertTrue(resultado.getMessage().contains("Se o email existir"));
    verify(recoverPasswordRepository, never()).save(any());
    verify(emailService, never())
        .sendEmailWithTemplate(any(), anyString(), anyString(), anyString());
  }

  @Test
  void resetPassword_DeveLancarExcecaoQuandoCodigoExpirado() {
    // Given
    br.com.utfpr.gerenciamento.server.dto.RecoverPasswordRequestDto requestDto =
        new br.com.utfpr.gerenciamento.server.dto.RecoverPasswordRequestDto();
    requestDto.setCode("codigo-expirado");
    requestDto.setPassword("novaSenha123");
    requestDto.setRepeatPassword("novaSenha123");

    br.com.utfpr.gerenciamento.server.model.RecoverPassword recoverPasswordExpirado =
        new br.com.utfpr.gerenciamento.server.model.RecoverPassword();
    recoverPasswordExpirado.setEmail("teste@test.com");
    recoverPasswordExpirado.setDateTime(
        java.time.LocalDateTime.now().minusHours(25)); // Expirado (>24h)

    when(recoverPasswordRepository.findByCode("codigo-expirado"))
        .thenReturn(recoverPasswordExpirado);

    // When/Then
    assertThrows(
        br.com.utfpr.gerenciamento.server.exception.RecoverCodeInvalidException.class,
        () -> usuarioService.resetPassword(requestDto));

    verify(recoverPasswordRepository).delete(recoverPasswordExpirado);
    verify(usuarioRepository, never()).save(any());
  }

  @Test
  void resetPassword_DeveLancarExcecaoQuandoSenhaVazia() {
    // Given
    br.com.utfpr.gerenciamento.server.dto.RecoverPasswordRequestDto requestDto =
        new br.com.utfpr.gerenciamento.server.dto.RecoverPasswordRequestDto();
    requestDto.setCode("codigo-123");
    requestDto.setPassword("");
    requestDto.setRepeatPassword("");

    br.com.utfpr.gerenciamento.server.model.RecoverPassword recoverPassword =
        new br.com.utfpr.gerenciamento.server.model.RecoverPassword();
    recoverPassword.setEmail("teste@test.com");
    recoverPassword.setDateTime(java.time.LocalDateTime.now());

    when(recoverPasswordRepository.findByCode("codigo-123")).thenReturn(recoverPassword);

    // When/Then
    assertThrows(
        br.com.utfpr.gerenciamento.server.exception.InvalidPasswordException.class,
        () -> usuarioService.resetPassword(requestDto));
  }

  @Test
  void resetPassword_DeveLancarExcecaoQuandoSenhaMuitoCurta() {
    // Given
    br.com.utfpr.gerenciamento.server.dto.RecoverPasswordRequestDto requestDto =
        new br.com.utfpr.gerenciamento.server.dto.RecoverPasswordRequestDto();
    requestDto.setCode("codigo-123");
    requestDto.setPassword("123");
    requestDto.setRepeatPassword("123");

    br.com.utfpr.gerenciamento.server.model.RecoverPassword recoverPassword =
        new br.com.utfpr.gerenciamento.server.model.RecoverPassword();
    recoverPassword.setEmail("teste@test.com");
    recoverPassword.setDateTime(java.time.LocalDateTime.now());

    when(recoverPasswordRepository.findByCode("codigo-123")).thenReturn(recoverPassword);

    // When/Then
    assertThrows(
        br.com.utfpr.gerenciamento.server.exception.InvalidPasswordException.class,
        () -> usuarioService.resetPassword(requestDto));
  }

  @Test
  void findByDocumento_DeveRetornarUsuarioQuandoDocumentoExiste() {
    // Given
    String documento = "12345678901";
    when(usuarioRepository.findByDocumento(documento)).thenReturn(java.util.Optional.of(usuario));

    // When
    Usuario resultado = usuarioService.findByDocumento(documento);

    // Then
    assertNotNull(resultado);
    verify(usuarioRepository).findByDocumento(documento);
  }

  @Test
  void findByDocumento_DeveRetornarNullQuandoDocumentoNaoExiste() {
    // Given
    String documento = "00000000000";
    when(usuarioRepository.findByDocumento(documento)).thenReturn(java.util.Optional.empty());

    // When
    Usuario resultado = usuarioService.findByDocumento(documento);

    // Then
    assertNull(resultado);
  }

  @Test
  void hasSolicitacaoNadaConstaPendingOrCompleted_DeveRetornarTrueQuandoExisteSolicitacao() {
    // Given
    String username = "usuario@utfpr.edu.br";
    when(usuarioRepository.findByUsernameOrEmail(username, username)).thenReturn(usuario);
    when(nadaConstaRepository.existsByUsuarioAndStatusIn(eq(usuario), anySet())).thenReturn(true);

    // When
    boolean resultado = usuarioService.hasSolicitacaoNadaConstaPendingOrCompleted(username);

    // Then
    assertTrue(resultado);
  }

  @Test
  void hasSolicitacaoNadaConstaPendingOrCompleted_DeveRetornarFalseQuandoNaoExisteSolicitacao() {
    // Given
    String username = "usuario@utfpr.edu.br";
    when(usuarioRepository.findByUsernameOrEmail(username, username)).thenReturn(usuario);
    when(nadaConstaRepository.existsByUsuarioAndStatusIn(eq(usuario), anySet())).thenReturn(false);

    // When
    boolean resultado = usuarioService.hasSolicitacaoNadaConstaPendingOrCompleted(username);

    // Then
    assertFalse(resultado);
  }

  @Test
  void hasSolicitacaoNadaConstaPendingOrCompleted_DeveRetornarFalseQuandoUsuarioNaoExiste() {
    // Given
    String username = "inexistente@test.com";
    when(usuarioRepository.findByUsernameOrEmail(username, username)).thenReturn(null);

    // When
    boolean resultado = usuarioService.hasSolicitacaoNadaConstaPendingOrCompleted(username);

    // Then
    assertFalse(resultado);
    verify(nadaConstaRepository, never()).existsByUsuarioAndStatusIn(any(), anySet());
  }

  @Test
  void updateUsuario_DeveLancarExcecaoQuandoUsuarioNaoAutorizado() {
    // Given
    org.springframework.security.core.context.SecurityContextHolder.getContext()
        .setAuthentication(
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "outroUsuario@test.com", null));

    Usuario usuarioParaAtualizar = new Usuario();
    usuarioParaAtualizar.setUsername("usuario@utfpr.edu.br");

    // When/Then
    assertThrows(
        org.springframework.security.access.AccessDeniedException.class,
        () -> usuarioService.updateUsuario(usuarioParaAtualizar));

    verify(usuarioRepository, never()).save(any());
  }

  @Test
  void updateUsuario_DeveLancarExcecaoQuandoUsuarioNaoEncontrado() {
    // Given
    org.springframework.security.core.context.SecurityContextHolder.getContext()
        .setAuthentication(
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "usuario@utfpr.edu.br", null));

    Usuario usuarioParaAtualizar = new Usuario();
    usuarioParaAtualizar.setUsername("usuario@utfpr.edu.br");

    when(usuarioRepository.findByUsername("usuario@utfpr.edu.br")).thenReturn(null);

    // When/Then
    assertThrows(
        br.com.utfpr.gerenciamento.server.exception.EntityNotFoundException.class,
        () -> usuarioService.updateUsuario(usuarioParaAtualizar));
  }

  @Test
  void saveNewUser_DevePublicarEventoAposSalvar() {
    // Given
    Usuario novoUsuario = new Usuario();
    novoUsuario.setEmail("novo@utfpr.edu.br");
    novoUsuario.setPassword("senha123");
    novoUsuario.setNome("Novo Professor");

    when(permissaoService.findByNome(anyString())).thenReturn(permissao1);
    when(usuarioRepository.save(any(Usuario.class)))
        .thenAnswer(
            invocation -> {
              Usuario u = invocation.getArgument(0);
              u.setId(999L);
              return u;
            });

    // When
    Usuario resultado = usuarioService.saveNewUser(novoUsuario);

    // Then
    assertNotNull(resultado);
    assertNotNull(resultado.getId());
    assertFalse(resultado.getEmailVerificado());
    verify(eventPublisher)
        .publishEvent(
            any(br.com.utfpr.gerenciamento.server.event.usuario.UsuarioCriadoEvent.class));
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
}
