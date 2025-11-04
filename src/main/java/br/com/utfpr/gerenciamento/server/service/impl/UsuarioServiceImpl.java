package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.dto.*;
import br.com.utfpr.gerenciamento.server.enumeration.NadaConstaStatus;
import br.com.utfpr.gerenciamento.server.enumeration.UserRole;
import br.com.utfpr.gerenciamento.server.event.usuario.UsuarioCriadoEvent;
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
import br.com.utfpr.gerenciamento.server.util.SecurityUtils;
import br.com.utfpr.gerenciamento.server.util.Util;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UsuarioServiceImpl extends CrudServiceImpl<Usuario, Long, UsuarioResponseDto>
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

  private final ApplicationEventPublisher eventPublisher;

  public UsuarioServiceImpl(
      UsuarioRepository usuarioRepository,
      ModelMapper modelMapper,
      RecoverPasswordRepository recoverPasswordRepository,
      PasswordEncoder passwordEncoder,
      EmailService emailService,
      PermissaoService permissaoService,
      NadaConstaRepository nadaConstaRepository,
      ApplicationEventPublisher eventPublisher) {
    this.usuarioRepository = usuarioRepository;
    this.modelMapper = modelMapper;
    this.recoverPasswordRepository = recoverPasswordRepository;
    this.passwordEncoder = passwordEncoder;
    this.emailService = emailService;
    this.permissaoService = permissaoService;
    this.nadaConstaRepository = nadaConstaRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  protected JpaRepository<Usuario, Long> getRepository() {
    return usuarioRepository;
  }

  @Override
  public UsuarioResponseDto toDto(Usuario entity) {
    return modelMapper.map(entity, UsuarioResponseDto.class);
  }

  @Override
  public Usuario toEntity(UsuarioResponseDto usuarioResponseDto) {
    return modelMapper.map(usuarioResponseDto, Usuario.class);
  }

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
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
  public UsuarioResponseDto findByUsername(String username) {
    username = normalizeUsername(username);
    // Usa versão SEM permissoes (LAZY) - mais performática para uso geral
    return toDto(usuarioRepository.findByUsernameOrEmail(username, username));
  }

  @Override
  @Transactional(readOnly = true)
  public UsuarioResponseDto findByUsernameForAuthentication(String username) {
    username = normalizeUsername(username);
    // Usa versão COM permissoes (@EntityGraph) - necessário para autenticação
    return toDto(usuarioRepository.findWithPermissoesByUsernameOrEmail(username, username));
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
    // Usa searchByTextWithRoles que cria apenas 1 JOIN (query pode ser null/blank)
    Specification<Usuario> spec =
        UsuarioSpecifications.distinctResults()
            .and(
                UsuarioSpecifications.searchByTextWithRoles(
                    query, UserRole.PROFESSOR, UserRole.ALUNO));

    return usuarioRepository.findAll(spec, pageable).map(this::convertToDto);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<UsuarioResponseDto> usuarioCompleteLab(String query, Pageable pageable) {
    // Usa searchByTextWithRoles que cria apenas 1 JOIN (query pode ser null/blank)
    Specification<Usuario> spec =
        UsuarioSpecifications.distinctResults()
            .and(
                UsuarioSpecifications.searchByTextWithRoles(
                    query, UserRole.ADMINISTRADOR, UserRole.LABORATORISTA));

    return usuarioRepository.findAll(spec, pageable).map(this::convertToDto);
  }

  @Override
  @Transactional
  public UsuarioResponseDto updateUsuario(Usuario usuario) {
    String usernameAutenticado = SecurityUtils.getAuthenticatedUsername();
    String usernameAlvo = normalizeUsername(usuario.getUsername());

    if (!usernameAlvo.equals(normalizeUsername(usernameAutenticado))) {
      log.warn(
          "Tentativa de atualização não autorizada: usuário {} tentou modificar {}",
          usernameAutenticado,
          usernameAlvo);
      throw new AccessDeniedException("Usuário não autorizado a modificar este perfil");
    }

    Usuario usuarioExistente = usuarioRepository.findByUsername(usuario.getUsername());
    if (usuarioExistente == null) {
      throw new EntityNotFoundException("Usuário não encontrado: " + usuario.getUsername());
    }

    usuarioExistente.setTelefone(usuario.getTelefone());
    usuarioExistente.setDocumento(usuario.getDocumento());

    return toDto(usuarioRepository.save(usuarioExistente));
  }

  @Override
  @Transactional
  public UsuarioResponseDto save(Usuario usuario) {
    if (usuario.getId() != null) {
      // Se for update, busca o usuário atual do banco
      Usuario usuarioExistente =
          usuarioRepository
              .findById(usuario.getId())
              .orElseThrow(
                  () ->
                      new EntityNotFoundException(
                          "Usuário não encontrado com ID: " + usuario.getId()));

      // Se a nova senha é nula ou vazia, mantém a senha antiga
      if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
        usuario.setPassword(usuarioExistente.getPassword());
      } else if (!Util.isPasswordEncoded(usuario.getPassword())) {
        // Se veio uma nova senha não codificada, codifica
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
      }

      // Preserva campos que não devem ser sobrescritos
      usuario.setEmailVerificado(usuarioExistente.getEmailVerificado());
    } else {
      // Novo usuário → codifica a senha normalmente
      if (usuario.getPassword() != null && !Util.isPasswordEncoded(usuario.getPassword())) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
      }
    }

    // Normaliza permissões para evitar NPE e usa batch fetching
    Set<Permissao> permissoesInput = usuario.getPermissoes();
    if (permissoesInput != null && !permissoesInput.isEmpty()) {
      Set<Long> permissaoIds =
          permissoesInput.stream()
              .filter(Objects::nonNull)
              .map(Permissao::getId)
              .filter(Objects::nonNull)
              .collect(Collectors.toSet());

      if (!permissaoIds.isEmpty()) {
        Set<Permissao> permissoes =
            new HashSet<>(
                permissaoService.findAllById(permissaoIds).stream()
                    .map(permissaoService::toEntity)
                    .collect(Collectors.toSet()));
        usuario.setPermissoes(permissoes);
      } else {
        usuario.setPermissoes(new HashSet<>());
      }
    } else {
      usuario.setPermissoes(new HashSet<>());
    }

    return toDto(usuarioRepository.save(usuario));
  }

  public UsuarioResponseDto convertToDto(Usuario entity) {
    return modelMapper.map(entity, UsuarioResponseDto.class);
  }

  public Usuario convertToEntity(UsuarioResponseDto entityDto) {
    return modelMapper.map(entityDto, Usuario.class);
  }

  @Override
  @Transactional(readOnly = true)
  public String resendEmail(ConfirmEmailRequestDto confirmEmailRequestDto) {
    String email = confirmEmailRequestDto.getEmail();
    Usuario usuario = usuarioRepository.findByEmail(email);

    if (usuario == null) {
      // Log sem PII, resposta genérica para evitar enumeração
      log.info("Reenvio solicitado para email não cadastrado");
      return "Se o email existir, um novo link de confirmação será enviado.";
    }

    if (usuario.getEmailVerificado()) {
      log.info(
          "Email já verificado: {}",
          br.com.utfpr.gerenciamento.server.util.EmailUtils.maskEmail(email));
      return "Este email já foi confirmado. Você pode fazer login normalmente.";
    }

    try {
      enviarEmailConfirmacao(usuario);
      log.info(
          "Email de confirmação reenviado para: {}",
          br.com.utfpr.gerenciamento.server.util.EmailUtils.maskEmail(email));
      return "Email de confirmação reenviado. Verifique sua caixa de entrada.";
    } catch (Exception e) {
      log.error(
          "Falha ao reenviar email de confirmação para {}: {}",
          br.com.utfpr.gerenciamento.server.util.EmailUtils.maskEmail(email),
          e.getMessage(),
          e);
      throw new EmailException(
          "Não foi possível enviar o email. Tente novamente em alguns minutos.", e);
    }
  }

  @Override
  @Transactional
  public GenericResponse sendEmailCodeRecoverPassword(String email) {
    Usuario usuario = usuarioRepository.findByEmail(email);
    if (usuario == null) {
      // Log sem PII, resposta genérica para evitar enumeração
      log.info("Solicitação de recuperação recebida");
      return GenericResponse.builder()
          .message("Se o email existir, uma solicitação foi enviada para sua caixa de entrada.")
          .build();
    }

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

    try {
      emailService.sendEmailWithTemplate(
          emailDto, emailDto.getEmailTo(), emailDto.getSubject(), "templateRecoverPassword");
      log.info(
          "Código de recuperação de senha enviado para: {}",
          br.com.utfpr.gerenciamento.server.util.EmailUtils.maskEmail(email));
    } catch (Exception e) {
      log.error(
          "Falha ao enviar email de recuperação para {}: {}",
          br.com.utfpr.gerenciamento.server.util.EmailUtils.maskEmail(email),
          e.getMessage(),
          e);
      throw new EmailException("Erro ao enviar email de recuperação", e);
    }

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
  public GenericResponse resetPassword(RecoverPasswordRequestDto request) {
    validarSenhasIguais(request.getPassword(), request.getRepeatPassword());

    RecoverPassword recoverPassword = recoverPasswordRepository.findByCode(request.getCode());
    if (recoverPassword == null) {
      throw new RecoverCodeInvalidException(
          "Código de recuperação inválido ou expirado. Solicite um novo código.");
    }

    validarCodigoNaoExpirado(recoverPassword);

    Usuario usuario = usuarioRepository.findByEmail(recoverPassword.getEmail());
    if (usuario == null) {
      log.error(
          "RecoverPassword encontrado mas usuário não existe: {}",
          br.com.utfpr.gerenciamento.server.util.EmailUtils.maskEmail(recoverPassword.getEmail()));
      throw new EntityNotFoundException("Usuário não encontrado");
    }

    usuario.setPassword(passwordEncoder.encode(request.getPassword()));
    usuarioRepository.save(usuario);

    // Limpa código usado para evitar reutilização
    recoverPasswordRepository.delete(recoverPassword);

    log.info(
        "Senha redefinida com sucesso para usuário: {}",
        br.com.utfpr.gerenciamento.server.util.EmailUtils.maskEmail(usuario.getEmail()));
    return GenericResponse.builder()
        .message("Senha alterada com sucesso. Você já pode fazer login com a nova senha.")
        .build();
  }

  @Override
  @Transactional
  public UsuarioResponseDto updatePassword(Usuario usuario, String senhaAtual) {
    Usuario userTemp =
        usuarioRepository
            .findById(usuario.getId())
            .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
    usuario.setEmailVerificado(userTemp.getEmailVerificado());
    if (passwordEncoder.matches(senhaAtual, userTemp.getPassword())) {
      userTemp.setPassword(passwordEncoder.encode(usuario.getPassword()));
      return toDto(usuarioRepository.save(userTemp));
    }
    throw new InvalidPasswordException("Senha incorreta");
  }

  /**
   * Cria novo usuário e publica evento para envio de email de confirmação.
   *
   * <p><b>PADRÃO EVENT-DRIVEN:</b> Email será enviado APÓS commit da transação via {@link
   * UsuarioCriadoEvent}. Benefícios:
   *
   * <ul>
   *   <li>✅ Falha no email NÃO causa rollback da criação do usuário
   *   <li>✅ Usuário sempre é criado, independente de problemas de SMTP
   *   <li>✅ Email pode ser reenviado posteriormente via endpoint /resend-email
   *   <li>✅ Processamento assíncrono (não bloqueia requisição do usuário)
   * </ul>
   *
   * @param usuario Dados do usuário a ser criado
   * @return Usuario criado e salvo no banco de dados
   * @throws IllegalStateException se houver erro ao salvar usuário no banco
   */
  @Override
  @Transactional
  public UsuarioResponseDto saveNewUser(Usuario usuario) {
    try {
      if (!Util.isPasswordEncoded(usuario.getPassword())) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
      }
      Usuario usuarioSalvo = prepareAndSaveNewUser(usuario);
      // Publica evento para envio de email APÓS commit da transação
      eventPublisher.publishEvent(
          new UsuarioCriadoEvent(
              this,
              usuarioSalvo.getId(),
              usuarioSalvo.getEmail(),
              usuarioSalvo.getCodigoVerificacao()));

      log.info(
          "Novo usuário criado: {} (email de confirmação será enviado após commit)",
          usuarioSalvo.getEmail());
      return toDto(usuarioSalvo);
    } catch (Exception ex) {
      log.error("Erro ao criar novo usuário: email={}", usuario.getEmail(), ex);
      throw new IllegalStateException("Erro ao criar novo usuário. Tente novamente.", ex);
    }
  }

  /**
   * Constrói o EmailDto para email de confirmação de cadastro.
   *
   * @param usuario usuário para o qual o email será enviado
   * @return EmailDto configurado com dados de confirmação
   */
  private EmailDto construirEmailConfirmacao(Usuario usuario) {
    String urlConfirmacao =
        String.format("%s/confirmar-email/%s", frontBaseUrl, usuario.getCodigoVerificacao());

    return EmailDto.builder()
        .emailTo(usuario.getEmail())
        .usuario(usuario.getNome())
        .url(urlConfirmacao)
        .subject(EMAIL_SUBJECT_CONFIRMACAO)
        .subjectBody(EMAIL_SUBJECT_CONFIRMACAO)
        .build();
  }

  /**
   * Determina a permissão do usuário baseado no domínio do email.
   *
   * @param email email do usuário
   * @return Permissão correspondente (PROFESSOR para @utfpr.edu.br, ALUNO caso contrário)
   */
  private Permissao determinarPermissaoPorEmail(String email) {
    UserRole role = email.contains("@utfpr.edu.br") ? UserRole.PROFESSOR : UserRole.ALUNO;
    return permissaoService.findByNome(role.getAuthority());
  }

  /**
   * Prepara e persiste um novo usuário no sistema.
   *
   * @param usuario dados do usuário a ser criado
   * @return usuário salvo com ID gerado
   */
  private Usuario prepareAndSaveNewUser(Usuario usuario) {
    if (!Util.isPasswordEncoded(usuario.getPassword())) {
      usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
    }

    usuario.setPermissoes(new HashSet<>());
    usuario.setUsername(usuario.getEmail());

    Permissao permissao = determinarPermissaoPorEmail(usuario.getEmail());
    usuario.getPermissoes().add(permissao);

    usuario.setCodigoVerificacao(UUID.randomUUID().toString());
    usuario.setEmailVerificado(false);

    return usuarioRepository.save(usuario);
  }

  /**
   * Envia email de confirmação de cadastro para o usuário.
   *
   * @param usuario usuário para o qual enviar o email
   * @throws EmailException se o envio falhar
   */
  private void enviarEmailConfirmacao(Usuario usuario) {
    EmailDto emailDto = construirEmailConfirmacao(usuario);
    emailService.sendEmailWithTemplate(
        emailDto, emailDto.getEmailTo(), emailDto.getSubject(), "templateConfirmacaoCadastro");
  }

  /**
   * Valida se o código de recuperação não expirou (limite: 24 horas).
   *
   * @param recoverPassword código de recuperação a validar
   * @throws RecoverCodeInvalidException se o código estiver expirado
   */
  private void validarCodigoNaoExpirado(RecoverPassword recoverPassword) {
    LocalDateTime limiteExpiracao = LocalDateTime.now().minusHours(24);
    if (recoverPassword.getDateTime().isBefore(limiteExpiracao)) {
      recoverPasswordRepository.delete(recoverPassword); // Cleanup
      throw new RecoverCodeInvalidException(
          "Este código expirou. Solicite um novo código de recuperação.");
    }
  }

  /**
   * Valida se as senhas coincidem e atendem requisitos mínimos.
   *
   * @param senha senha fornecida
   * @param confirmacao confirmação da senha
   * @throws InvalidPasswordException se as senhas não coincidirem ou não atenderem requisitos
   */
  private void validarSenhasIguais(String senha, String confirmacao) {
    if (senha == null || senha.trim().isEmpty()) {
      throw new InvalidPasswordException("A senha não pode ser vazia.");
    }

    if (confirmacao == null || confirmacao.trim().isEmpty()) {
      throw new InvalidPasswordException("A confirmação de senha não pode ser vazia.");
    }

    if (senha.length() < 8) {
      throw new InvalidPasswordException("A senha deve ter no mínimo 8 caracteres.");
    }

    if (!Objects.equals(senha, confirmacao)) {
      throw new InvalidPasswordException("As senhas não coincidem. Tente novamente.");
    }
  }

  @Override
  public UsuarioResponseDto findByDocumento(String documento) {
    return toDto(usuarioRepository.findByDocumento(documento).orElse(null));
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
