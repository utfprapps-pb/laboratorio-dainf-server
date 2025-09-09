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
import java.time.LocalDateTime;
import java.util.*;

import br.com.utfpr.gerenciamento.server.util.Util;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioServiceImpl extends CrudServiceImpl<Usuario, Long>
    implements UsuarioService, UserDetailsService {

  private final PasswordEncoder passwordEncoder;

  @Value("${utfpr.front.url}")
  private String frontBaseUrl;

  private final UsuarioRepository usuarioRepository;

  private final ModelMapper modelMapper;

  private final RecoverPasswordRepository recoverPasswordRepository;

  private final EmailService emailService;

  private final PermissaoService permissaoService;

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
    Usuario usuario = usuarioRepository.findByUsernameOrEmail(username, username);
    if (usuario == null) {
      throw new UsernameNotFoundException("Usuário não encontrado");
    }
    return usuario;
  }

  @Override
  @Transactional(readOnly = true)
  public List<UsuarioResponseDto> usuarioComplete(String query) {
    if ("".equalsIgnoreCase(query)) {
      return usuarioRepository.findAll()
              .stream()
              .map(this::convertToDto)
              .toList();
    }
    return usuarioRepository.findByNomeLikeIgnoreCase("%" + query + "%")
            .stream()
            .map(this::convertToDto)
            .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public Usuario findByUsername(String username) {
    if (username.contains("@professores.utfpr.edu.br")) {
      username = username.replace("professores.", "");
    } else if (username.contains("@administrativo.utfpr.edu.br")) {
      username = username.replace("administrativo.", "");
    }
    return usuarioRepository.findByUsernameOrEmail(username, username);
  }

  @Override
  @Transactional(readOnly = true)
  public List<UsuarioResponseDto> usuarioCompleteByUserAndDocAndNome(String query) {
    if ("".equalsIgnoreCase(query)) {
      return usuarioRepository.findAllCustom()
              .stream()
              .map(this::convertToDto)
              .toList();
    }
    return usuarioRepository.findUsuarioCompleteCustom("%" + query.toUpperCase() + "%")
            .stream()
            .map(this::convertToDto)
            .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<UsuarioResponseDto> usuarioCompleteLab(String query) {
    if ("".equalsIgnoreCase(query)) {
      return usuarioRepository.findAllCustomLab()
              .stream()
              .map(this::convertToDto)
              .toList();
    }
    return usuarioRepository.findUsuarioCompleteCustomLab("%" + query.toUpperCase() + "%")
            .stream()
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
    if (!Util.isPasswordEncoded(usuario.getPassword())) {
      usuario.setPassword(new BCryptPasswordEncoder().encode(usuario.getPassword()));
    }
    Set<Permissao> permissoes = new HashSet<>();
    usuario
            .getPermissoes()
            .forEach(permissao -> permissoes.add(permissaoService.findOne(permissao.getId())));
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
    // emailMessageService.sendEmail(emailDto, "templateRecoverPassword");
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
    BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder();
    usuario.setEmailVerificado(userTemp.getEmailVerificado());
    if (bCrypt.matches(senhaAtual, userTemp.getPassword())) {
      usuario.setPassword(bCrypt.encode(usuario.getPassword()));
      return usuarioRepository.save(usuario);
    }
    throw new RuntimeException("Senha incorreta");
  }

  @Override
  public Usuario saveNewUser(Usuario usuario) {
    if (!Util.isPasswordEncoded(usuario.getPassword())) {
      usuario.setPassword(new BCryptPasswordEncoder().encode(usuario.getPassword()));
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
      // TODO - adicionar constante com o nome do laboratório.
      emailDto.setSubject("Confirmação de email - Laboratório DAINF-PB (UTFPR)");
      emailDto.setSubjectBody("Confirmação de email - Laboratório DAINF-PB (UTFPR)");

      emailService.sendEmailWithTemplate(
              emailDto, emailDto.getEmailTo(), emailDto.getSubject(), "templateConfirmacaoCadastro");

      return usuario;
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    }
  }

  private void sendEmailNewUser(Usuario usuario) {
    EmailDto emailDto = new EmailDto();
    emailDto.setEmailTo(usuario.getEmail());
    emailDto.setUsuario(usuario.getNome());
    emailDto.setUrl(frontBaseUrl + "/confirmar-email/" + usuario.getCodigoVerificacao());
    emailDto.setSubject("Confirmação de email - Laboratório DAINF-PB (UTFPR)");
    emailDto.setSubjectBody("Confirmação de email - Laboratório DAINF-PB (UTFPR)");
    Map<String, Object> body = new HashMap<>();
    body.put("usuario", usuario.getNome());
    body.put("url", emailDto.getUrl());

    emailService.sendEmailWithTemplate(
        emailDto, emailDto.getEmailTo(), emailDto.getSubject(), "templateConfirmacaoCadastro");
  }

}
