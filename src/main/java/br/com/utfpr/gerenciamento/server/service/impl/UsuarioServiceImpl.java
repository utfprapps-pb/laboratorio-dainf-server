package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.dto.*;
import br.com.utfpr.gerenciamento.server.exception.EntityNotFoundException;
import br.com.utfpr.gerenciamento.server.exception.RecoverCodeInvalidException;
import br.com.utfpr.gerenciamento.server.model.Permissao;
import br.com.utfpr.gerenciamento.server.model.RecoverPassword;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.repository.RecoverPasswordRepository;
import br.com.utfpr.gerenciamento.server.repository.UsuarioRepository;
import br.com.utfpr.gerenciamento.server.service.EmailService;
import br.com.utfpr.gerenciamento.server.service.PermissaoService;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import br.com.utfpr.gerenciamento.server.util.Util;
import java.time.LocalDateTime;
import java.util.*;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioServiceImpl extends CrudServiceImpl<Usuario, Long>
    implements UsuarioService, UserDetailsService {

  public static final String CONFIRMAÇÃO_DE_EMAIL_LABORATÓRIO_DAINF_PB_UTFPR =
      "Confirmação de email - Laboratório DAINF-PB (UTFPR)";
  private final PasswordEncoder passwordEncoder;

  @Value("${utfpr.front.url}")
  private String frontBaseUrl;

  private final UsuarioRepository usuarioRepository;

  private final ModelMapper modelMapper;

  private final RecoverPasswordRepository recoverPasswordRepository;

  private final EmailService emailService;

  private final PermissaoService permissaoService;

  private static final Logger LOGGER = LoggerFactory.getLogger(UsuarioServiceImpl.class.getName());

  public UsuarioServiceImpl(
      UsuarioRepository usuarioRepository,
      ModelMapper modelMapper,
      RecoverPasswordRepository recoverPasswordRepository,
      PasswordEncoder passwordEncoder,
      EmailService emailService,
      PermissaoService permissaoService) {
    this.usuarioRepository = usuarioRepository;
    this.modelMapper = modelMapper;
    this.recoverPasswordRepository = recoverPasswordRepository;
    this.passwordEncoder = passwordEncoder;
    this.emailService = emailService;
    this.permissaoService = permissaoService;
  }

  @Override
  protected JpaRepository<Usuario, Long> getRepository() {
    return usuarioRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Usuario usuario = findByUsernameForAuthentication(username);
    if (usuario == null) {
      throw new UsernameNotFoundException("Usuário não encontrado");
    }
    return usuario;
  }

  @Override
  @Transactional(readOnly = true)
  public List<UsuarioResponseDto> usuarioComplete(String query) {
    if ("".equalsIgnoreCase(query)) {
      return usuarioRepository.findAll().stream().map(this::convertToDto).toList();
    }
    return usuarioRepository.findByNomeLikeIgnoreCase("%" + query + "%").stream()
        .map(this::convertToDto)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public Usuario findByUsername(String username) {
    username = normalizeUsername(username);
    // Usa versão SEM permissoes (LAZY) - mais performática para uso geral
    return usuarioRepository.findByUsernameOrEmail(username, username);
  }

  @Override
  @Transactional(readOnly = true)
  public Usuario findByUsernameForAuthentication(String username) {
    username = normalizeUsername(username);
    // Usa versão COM permissoes (@EntityGraph) - necessário para autenticação
    return usuarioRepository.findWithPermissoesByUsernameOrEmail(username, username);
  }

  /**
   * Normaliza username removendo subdomínios institucionais da UTFPR.
   * Converte @professores.utfpr.edu.br e @administrativo.utfpr.edu.br para @utfpr.edu.br.
   *
   * @param username username original (pode conter subdomínios)
   * @return username normalizado
   */
  private String normalizeUsername(String username) {
    if (username.contains("@professores.utfpr.edu.br")) {
      return username.replace("professores.", "");
    } else if (username.contains("@administrativo.utfpr.edu.br")) {
      return username.replace("administrativo.", "");
    }
    return username;
  }

  @Override
  @Transactional(readOnly = true)
  public List<UsuarioResponseDto> usuarioCompleteByUserAndDocAndNome(String query) {
    if (query == null || query.isBlank()) {
      return usuarioRepository.findAllCustom().stream().map(this::convertToDto).toList();
    }
    return usuarioRepository.findUsuarioCompleteCustom("%" + query.toUpperCase() + "%").stream()
        .map(this::convertToDto)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<UsuarioResponseDto> usuarioCompleteLab(String query) {
    if (query == null || query.isBlank()) {
      return usuarioRepository.findAllCustomLab().stream().map(this::convertToDto).toList();
    }
    return usuarioRepository.findUsuarioCompleteCustomLab("%" + query.toUpperCase() + "%").stream()
        .map(this::convertToDto)
        .toList();
  }

  @Override
  @Transactional
  public Usuario updateUsuario(Usuario usuario) {
    if (usuario
        .getUsername()
        .equals(SecurityContextHolder.getContext().getAuthentication().getPrincipal())) {
      Usuario usuarioTmp = usuarioRepository.findByUsername(usuario.getUsername());
      usuarioTmp.setTelefone(usuario.getTelefone());
      usuarioTmp.setDocumento(usuario.getDocumento());

      return usuarioRepository.save(usuarioTmp);
    }
    return null;
  }

  @Override
  @Transactional
  public Usuario save(Usuario usuario) {
    if (usuario.getPassword() != null && !Util.isPasswordEncoded(usuario.getPassword()))
      usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

    // Normaliza permissões para evitar NPE se a coleção for null
    Set<Permissao> permissoes = new HashSet<>();
    Set<Permissao> permissoesInput = usuario.getPermissoes();
    if (permissoesInput != null) {
      permissoesInput.forEach(
          permissao -> permissoes.add(permissaoService.findOne(permissao.getId())));
    }
    usuario.setPermissoes(permissoes);

    if (usuario.getId() != null) {
      Usuario usuarioTmp = usuarioRepository.findByUsername(usuario.getUsername());
      usuario.setEmailVerificado(usuarioTmp.getEmailVerificado());
    }
    return super.save(usuario);
  }

  public UsuarioResponseDto convertToDto(Usuario entity) {
    return modelMapper.map(entity, UsuarioResponseDto.class);
  }

  public Usuario convertToEntity(UsuarioResponseDto entityDto) {
    return modelMapper.map(entityDto, Usuario.class);
  }

  @Override
  @Transactional
  public String resendEmail(ConfirmEmailRequestDto confirmEmailRequestDto) {
    Usuario usuario = usuarioRepository.findByEmail(confirmEmailRequestDto.getEmail());
    try {
      sendEmailNewUser(usuario);
    } catch (Exception e) {
      throw new RuntimeException("Ocorreu um erro. O email de confirmação não pode ser enviado.");
    }
    return "Uma requisição foi enviada ao seu email.";
  }

  @Override
  @Transactional
  public GenericResponse sendEmailCodeRecoverPassword(String email) {
    Usuario usuario = usuarioRepository.findByEmail(email);
    if (Objects.isNull(usuario))
      throw new EntityNotFoundException(
          "Email não encontrado na base de dados. Por favor, crie uma nova conta.");

    RecoverPassword recoverPassword = new RecoverPassword();
    recoverPassword.setEmail(email);
    recoverPassword.setCode(UUID.randomUUID().toString());
    recoverPassword.setDateTime(LocalDateTime.now());

    Map<String, Object> body = new HashMap<>();
    body.put("usuario", usuario.getNome());
    body.put("url", frontBaseUrl + "/recupear-senha/" + recoverPassword.getCode());

    EmailDto emailDto =
        EmailDto.builder()
            .usuario(usuario.getNome())
            .emailTo(recoverPassword.getEmail())
            .url(frontBaseUrl + "/recupear-senha/" + recoverPassword.getCode())
            .subject("Laboratório DAINF-PB - Recuperar senha")
            .subjectBody("Laboratório DAINF-PB - Recuperar senha")
            .contentBody("")
            .body(body)
            .build();

    recoverPasswordRepository.save(recoverPassword);
    emailService.sendEmailWithTemplate(
        emailDto, emailDto.getEmailTo(), emailDto.getSubject(), "templateRecoverPassword");

    return GenericResponse.builder()
        .message("Uma solicitação foi enviada para o seu email.")
        .build();
  }

  @Override
  @Transactional
  public GenericResponse confirmEmail(ConfirmEmailRequestDto confirmEmailRequestDto) {
    Usuario usuario = usuarioRepository.findByCodigoVerificacao(confirmEmailRequestDto.getCode());
    if (usuario != null) {
      usuario.setEmailVerificado(true);
      usuarioRepository.save(usuario);
      return GenericResponse.builder().message("O email do usuário foi confirmado.").build();
    } else {
      throw new RecoverCodeInvalidException("Código inválido. Por favor, solicite um novo código.");
    }
  }

  @Override
  @Transactional
  public GenericResponse resetPassword(RecoverPasswordRequestDto recoverPasswordRequestDto) {
    RecoverPassword recoverPassword =
        recoverPasswordRepository.findByCode(recoverPasswordRequestDto.getCode());
    if (recoverPassword != null) {
      Usuario usuario = usuarioRepository.findByEmail(recoverPassword.getEmail());
      if (recoverPasswordRequestDto
          .getPassword()
          .equals(recoverPasswordRequestDto.getRepeatPassword())) {
        usuario.setPassword(passwordEncoder.encode(recoverPasswordRequestDto.getPassword()));
        usuarioRepository.save(usuario);
      } else {
        throw new RuntimeException("As senhas devem ser iguais.");
      }
      return GenericResponse.builder()
          .message("Senha alterada. Já é possível autenticar-se com a nova senha.")
          .build();
    } else {
      throw new RecoverCodeInvalidException(
          "O código de recuperação de senha expirou. Por favor, solicite um novo código.");
    }
  }

  @Override
  @Transactional
  public Usuario updatePassword(Usuario usuario, String senhaAtual) {
    Usuario userTemp = this.findOne(usuario.getId());
    usuario.setEmailVerificado(userTemp.getEmailVerificado());
    if (passwordEncoder.matches(senhaAtual, userTemp.getPassword())) {
      userTemp.setPassword(passwordEncoder.encode(usuario.getPassword()));
      return usuarioRepository.save(userTemp);
    }
    throw new RuntimeException("Senha incorreta");
  }

  @Override
  public Usuario saveNewUser(Usuario usuario) {
    if (!Util.isPasswordEncoded(usuario.getPassword())) {
      usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
    }
    try {
      usuario.setPermissoes(new HashSet<>());
      usuario.setUsername(usuario.getEmail());
      if (usuario.getEmail().contains("@utfpr.edu.br")) {
        usuario.getPermissoes().add(permissaoService.findByNome("ROLE_PROFESSOR"));
      } else {
        usuario.getPermissoes().add(permissaoService.findByNome("ROLE_ALUNO"));
      }
      usuario.setCodigoVerificacao(UUID.randomUUID().toString());
      usuario.setEmailVerificado(false);
      usuarioRepository.save(usuario);

      EmailDto emailDto = new EmailDto();
      emailDto.setEmailTo(usuario.getEmail());
      emailDto.setUsuario(usuario.getNome());
      emailDto.setUrl(frontBaseUrl + "/confirmar-email/" + usuario.getCodigoVerificacao());
      emailDto.setSubject(CONFIRMAÇÃO_DE_EMAIL_LABORATÓRIO_DAINF_PB_UTFPR);
      emailDto.setSubjectBody(CONFIRMAÇÃO_DE_EMAIL_LABORATÓRIO_DAINF_PB_UTFPR);

      emailService.sendEmailWithTemplate(
          emailDto, emailDto.getEmailTo(), emailDto.getSubject(), "templateConfirmacaoCadastro");

      return usuario;
    } catch (Exception ex) {
      LOGGER.error("Erro ao salvar novo usuário: ", ex);
      throw new RuntimeException("Erro ao salvar novo usuário.");
    }
  }

  private void sendEmailNewUser(Usuario usuario) {
    EmailDto emailDto = new EmailDto();
    emailDto.setEmailTo(usuario.getEmail());
    emailDto.setUsuario(usuario.getNome());
    emailDto.setUrl(frontBaseUrl + "/confirmar-email/" + usuario.getCodigoVerificacao());
    emailDto.setSubject(CONFIRMAÇÃO_DE_EMAIL_LABORATÓRIO_DAINF_PB_UTFPR);
    emailDto.setSubjectBody(CONFIRMAÇÃO_DE_EMAIL_LABORATÓRIO_DAINF_PB_UTFPR);
    Map<String, Object> body = new HashMap<>();
    body.put("usuario", usuario.getNome());
    body.put("url", emailDto.getUrl());

    emailService.sendEmailWithTemplate(
        emailDto, emailDto.getEmailTo(), emailDto.getSubject(), "templateConfirmacaoCadastro");
  }
}
