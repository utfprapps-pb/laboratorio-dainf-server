package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.dto.*;
import br.com.utfpr.gerenciamento.server.enumeration.NadaConstaStatus;
import br.com.utfpr.gerenciamento.server.enumeration.UserRole;
import br.com.utfpr.gerenciamento.server.exception.EmailException;
import br.com.utfpr.gerenciamento.server.exception.EntityNotFoundException;
import br.com.utfpr.gerenciamento.server.exception.InvalidPasswordException;
import br.com.utfpr.gerenciamento.server.exception.RecoverCodeInvalidException;
import br.com.utfpr.gerenciamento.server.model.Permissao;
import br.com.utfpr.gerenciamento.server.model.RecoverPassword;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.repository.NadaConstaRepository;
import br.com.utfpr.gerenciamento.server.repository.RecoverPasswordRepository;
import br.com.utfpr.gerenciamento.server.repository.UsuarioRepository;
import br.com.utfpr.gerenciamento.server.repository.specification.UsuarioSpecifications;
import br.com.utfpr.gerenciamento.server.service.EmailService;
import br.com.utfpr.gerenciamento.server.service.PermissaoService;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import br.com.utfpr.gerenciamento.server.util.Util;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UsuarioServiceImpl extends CrudServiceImpl<Usuario, Long>
    implements UsuarioService, UserDetailsService {

  public static final String EMAIL_SUBJECT_CONFIRMACAO =
      "Confirmação de email - Laboratório DAINF-PB (UTFPR)";
  private final PasswordEncoder passwordEncoder;

  @Value("${utfpr.front.url}")
  private String frontBaseUrl;

  private final UsuarioRepository usuarioRepository;

  private final ModelMapper modelMapper;

  private final RecoverPasswordRepository recoverPasswordRepository;

  private final EmailService emailService;

  private final PermissaoService permissaoService;

  private final NadaConstaRepository nadaConstaRepository;

  public UsuarioServiceImpl(
      UsuarioRepository usuarioRepository,
      ModelMapper modelMapper,
      RecoverPasswordRepository recoverPasswordRepository,
      PasswordEncoder passwordEncoder,
      EmailService emailService,
      PermissaoService permissaoService,
      NadaConstaRepository nadaConstaRepository) {
    this.usuarioRepository = usuarioRepository;
    this.modelMapper = modelMapper;
    this.recoverPasswordRepository = recoverPasswordRepository;
    this.passwordEncoder = passwordEncoder;
    this.emailService = emailService;
    this.permissaoService = permissaoService;
    this.nadaConstaRepository = nadaConstaRepository;
  }

  @Override
  protected JpaRepository<Usuario, Long> getRepository() {
    return usuarioRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // Normaliza e busca diretamente no repositório (evita bypass do proxy Spring)
    username = normalizeUsername(username);
    Usuario usuario = usuarioRepository.findWithPermissoesByUsernameOrEmail(username, username);
    if (usuario == null) {
      throw new UsernameNotFoundException("Usuário não encontrado");
    }
    return usuario;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<UsuarioResponseDto> usuarioComplete(String query, Pageable pageable) {
    // Busca todos os usuários com filtro textual opcional
    Specification<Usuario> spec = UsuarioSpecifications.distinctResults();

    // Adiciona filtro textual se query fornecida
    if (query != null && !query.isBlank()) {
      spec = spec.and(UsuarioSpecifications.searchByText(query));
    }

    // Usa @EntityGraph para evitar N+1 queries ao carregar permissoes
    return usuarioRepository.findAll(spec, pageable).map(this::convertToDto);
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
  public Page<UsuarioResponseDto> usuarioCompleteByUserAndDocAndNome(
      String query, Pageable pageable) {
    // Filtro base: PROFESSOR + ALUNO (usuários acadêmicos)
    Specification<Usuario> spec =
        UsuarioSpecifications.distinctResults()
            .and(UsuarioSpecifications.hasAnyRole(UserRole.PROFESSOR, UserRole.ALUNO));

    // Adiciona filtro textual se query fornecida
    if (query != null && !query.isBlank()) {
      spec =
          spec.and(
              UsuarioSpecifications.searchByTextAndRoles(
                  query, UserRole.PROFESSOR, UserRole.ALUNO));
    }

    return usuarioRepository.findAll(spec, pageable).map(this::convertToDto);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<UsuarioResponseDto> usuarioCompleteLab(String query, Pageable pageable) {
    // Filtro base: ADMINISTRADOR + LABORATORISTA (usuários de laboratório)
    Specification<Usuario> spec =
        UsuarioSpecifications.distinctResults()
            .and(UsuarioSpecifications.hasAnyRole(UserRole.ADMINISTRADOR, UserRole.LABORATORISTA));

    // Adiciona filtro textual se query fornecida
    if (query != null && !query.isBlank()) {
      spec =
          spec.and(
              UsuarioSpecifications.searchByTextAndRoles(
                  query, UserRole.ADMINISTRADOR, UserRole.LABORATORISTA));
    }

    return usuarioRepository.findAll(spec, pageable).map(this::convertToDto);
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

    // Normaliza permissões para evitar NPE e usa batch fetching (1 query em vez de N)
    Set<Permissao> permissoesInput = usuario.getPermissoes();
    if (permissoesInput != null && !permissoesInput.isEmpty()) {
      Set<Long> permissaoIds =
          permissoesInput.stream()
              .filter(Objects::nonNull)
              .map(Permissao::getId)
              .filter(Objects::nonNull)
              .collect(Collectors.toSet());

      if (!permissaoIds.isEmpty()) {
        Set<Permissao> permissoes = new HashSet<>(permissaoService.findAllById(permissaoIds));
        usuario.setPermissoes(permissoes);
      } else {
        usuario.setPermissoes(new HashSet<>());
      }
    } else {
      usuario.setPermissoes(new HashSet<>());
    }

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
      throw new EmailException("Ocorreu um erro. O email de confirmação não pode ser enviado.", e);
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
      usuario.setAtivo(true);
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
        throw new InvalidPasswordException("As senhas devem ser iguais.");
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
    Usuario userTemp =
        usuarioRepository
            .findById(usuario.getId())
            .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
    usuario.setEmailVerificado(userTemp.getEmailVerificado());
    if (passwordEncoder.matches(senhaAtual, userTemp.getPassword())) {
      userTemp.setPassword(passwordEncoder.encode(usuario.getPassword()));
      return usuarioRepository.save(userTemp);
    }
    throw new InvalidPasswordException("Senha incorreta");
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
        usuario.getPermissoes().add(permissaoService.findByNome(UserRole.PROFESSOR.getAuthority()));
      } else {
        usuario.getPermissoes().add(permissaoService.findByNome(UserRole.ALUNO.getAuthority()));
      }
      usuario.setCodigoVerificacao(UUID.randomUUID().toString());
      usuario.setEmailVerificado(false);
      usuarioRepository.save(usuario);

      EmailDto emailDto = new EmailDto();
      emailDto.setEmailTo(usuario.getEmail());
      emailDto.setUsuario(usuario.getNome());
      emailDto.setUrl(frontBaseUrl + "/confirmar-email/" + usuario.getCodigoVerificacao());
      emailDto.setSubject(EMAIL_SUBJECT_CONFIRMACAO);
      emailDto.setSubjectBody(EMAIL_SUBJECT_CONFIRMACAO);

      emailService.sendEmailWithTemplate(
          emailDto, emailDto.getEmailTo(), emailDto.getSubject(), "templateConfirmacaoCadastro");

      return usuario;
    } catch (Exception ex) {
      log.error("Erro ao salvar novo usuário: ", ex);
      throw new EmailException(
          "Erro ao salvar novo usuário. Verifique os dados e tente novamente.", ex);
    }
  }

  private void sendEmailNewUser(Usuario usuario) {
    EmailDto emailDto = new EmailDto();
    emailDto.setEmailTo(usuario.getEmail());
    emailDto.setUsuario(usuario.getNome());
    emailDto.setUrl(frontBaseUrl + "/confirmar-email/" + usuario.getCodigoVerificacao());
    emailDto.setSubject(EMAIL_SUBJECT_CONFIRMACAO);
    emailDto.setSubjectBody(EMAIL_SUBJECT_CONFIRMACAO);
    Map<String, Object> body = new HashMap<>();
    body.put("usuario", usuario.getNome());
    body.put("url", emailDto.getUrl());

    emailService.sendEmailWithTemplate(
        emailDto, emailDto.getEmailTo(), emailDto.getSubject(), "templateConfirmacaoCadastro");
  }

  @Override
  public Usuario findByDocumento(String documento) {
    return usuarioRepository.findByDocumento(documento).orElse(null);
  }

  /** Verifica se o usuário possui solicitação de nada consta em aberto ou concluída. */
  @Transactional
  @Override
  public boolean hasSolicitacaoNadaConstaPendingOrCompleted(String username) {
    // Normaliza e busca diretamente no repositório (evita bypass do proxy Spring)
    username = normalizeUsername(username);
    Usuario usuario = usuarioRepository.findByUsernameOrEmail(username, username);
    if (usuario == null) return false;
    return nadaConstaRepository.existsByUsuarioAndStatusIn(
        usuario, Set.of(NadaConstaStatus.PENDING, NadaConstaStatus.COMPLETED));
  }
}
