package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.dto.*;
import br.com.utfpr.gerenciamento.server.error.EntityNotFoundException;
import br.com.utfpr.gerenciamento.server.error.RecoverCodeInvalidException;
import br.com.utfpr.gerenciamento.server.model.RecoverPassword;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.repository.RecoverPasswordRepository;
import br.com.utfpr.gerenciamento.server.repository.UsuarioRepository;
import br.com.utfpr.gerenciamento.server.service.EmailService;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UsuarioServiceImpl extends CrudServiceImpl<Usuario, Long> implements UsuarioService, UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    @Value("${utfpr.front.url}")
    private String frontBaseUrl;

    private final UsuarioRepository usuarioRepository;

    private final ModelMapper modelMapper;

    private final RecoverPasswordRepository recoverPasswordRepository;

    private final EmailService emailService;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, ModelMapper modelMapper,
                              RecoverPasswordRepository recoverPasswordRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.modelMapper = modelMapper;
        this.recoverPasswordRepository = recoverPasswordRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Override
    protected JpaRepository<Usuario, Long> getRepository() {
        return usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsernameOrEmail(username, username);
        if (usuario == null) {
            throw new UsernameNotFoundException("Usuário não encontrado");
        }
        return usuario;
    }

    @Override
    public List<Usuario> usuarioComplete(String query) {
        if ("".equalsIgnoreCase(query)) {
            return usuarioRepository.findAll();
        }
        return usuarioRepository.findByNomeLikeIgnoreCase("%" + query + "%");
    }

    @Override
    public Usuario findByUsername(String username) {
        if (username.contains("@professores.utfpr.edu.br")) {
            username = username.replace("professores.", "");
        } else if (username.contains("@administrativo.utfpr.edu.br")) {
            username = username.replace("administrativo.", "");
        }
        return usuarioRepository.findByUsernameOrEmail(username, username);
    }

    @Override
    public List<Usuario> usuarioCompleteByUserAndDocAndNome(String query) {
        if ("".equalsIgnoreCase(query)) {
            return usuarioRepository.findAllCustom();
        }
        return usuarioRepository.findUsuarioCompleteCustom("%" + query.toUpperCase() + "%");
    }

    @Override
    public List<Usuario> usuarioCompleteLab(String query) {
        if ("".equalsIgnoreCase(query)) {
            return usuarioRepository.findAllCustomLab();
        }
        return usuarioRepository.findUsuarioCompleteCustomLab("%" + query.toUpperCase() + "%");
    }

    @Override
    public Usuario updateUsuario(Usuario usuario) {
        if (usuario.getUsername().equals(SecurityContextHolder.getContext().getAuthentication().getPrincipal())) {
            Usuario usuarioTmp = usuarioRepository.findByUsername(usuario.getUsername());
            usuarioTmp.setTelefone(usuario.getTelefone());
            usuarioTmp.setDocumento(usuario.getDocumento());
            usuarioRepository.save(usuarioTmp);
            return usuarioTmp;
        }
        return null;
    }

    public UsuarioResponseDto convertToDto(Usuario entity) {
        return modelMapper.map(entity, UsuarioResponseDto.class);
    }

    public Usuario convertToEntity(UsuarioResponseDto entityDto) {
        return modelMapper.map(entityDto, Usuario.class);
    }

    @Override
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
    public GenericResponse sendEmailCodeRecoverPassword(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (Objects.isNull(usuario))
            throw new EntityNotFoundException("Email não encontrado na base de dados. Por favor, crie uma nova conta.");

        RecoverPassword recoverPassword = new RecoverPassword();
        recoverPassword.setEmail(email);
        recoverPassword.setCode(UUID.randomUUID().toString());
        recoverPassword.setDateTime(LocalDateTime.now());

        Map<String, Object> body = new HashMap<>();
        body.put("usuario", usuario.getNome());
        body.put("url", frontBaseUrl + "/recupear-senha/" + recoverPassword.getCode());

        EmailDto emailDto = EmailDto.builder()
                .usuario(usuario.getNome())
                .emailTo(recoverPassword.getEmail())
                .url(frontBaseUrl + "/recupear-senha/" + recoverPassword.getCode())
                .subject("Laboratório DAINF-PB - Recuperar senha")
                .subjectBody("Laboratório DAINF-PB - Recuperar senha")
                .contentBody("")
                .body(body).build();

        recoverPasswordRepository.save(recoverPassword);
        // emailMessageService.sendEmail(emailDto, "templateRecoverPassword");
        emailService.sendEmailWithTemplate(emailDto, emailDto.getEmailTo(), emailDto.getSubject(), "templateRecoverPassword");

        return GenericResponse.builder().message("Uma solicitação foi enviada para o seu email.").build();
    }

    @Override
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
    public GenericResponse resetPassword(RecoverPasswordRequestDto recoverPasswordRequestDto) {
        RecoverPassword recoverPassword = recoverPasswordRepository.findByCode(recoverPasswordRequestDto.getCode());
        if (recoverPassword != null) {
            Usuario usuario = usuarioRepository.findByEmail(recoverPassword.getEmail());
            if (recoverPasswordRequestDto.getPassword().equals(recoverPasswordRequestDto.getRepeatPassword())) {
                usuario.setPassword(passwordEncoder.encode(recoverPasswordRequestDto.getPassword()));
                usuarioRepository.save(usuario);
            } else {
                throw new RuntimeException("As senhas devem ser iguais.");
            }
            return GenericResponse.builder().message("Senha alterada. Já é possível autenticar-se com a nova senha.").build();
        } else {
            throw new RecoverCodeInvalidException("O código de recuperação de senha expirou. Por favor, solicite um novo código.");
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

        emailService.sendEmailWithTemplate(emailDto, emailDto.getEmailTo(), emailDto.getSubject(), "templateConfirmacaoCadastro");
    }

}
