package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.dto.TokenDto;
import br.com.utfpr.gerenciamento.server.model.Permissao;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.security.SecurityConstants;
import br.com.utfpr.gerenciamento.server.service.PermissaoService;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import com.auth0.jwt.JWT;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

@RestController
@RequestMapping("/oauth")
public class OauthController {

    @Value("${google.clientId}")
    String googleClientId;

    @Autowired
    PasswordEncoder passwordEncoder;


    AuthenticationManager authenticationManager;

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    PermissaoService permissaoService;

    @PostMapping("/google")
    public ResponseEntity<TokenDto> google(@RequestBody TokenDto tokenDto) throws IOException {
        final NetHttpTransport transport = new NetHttpTransport();
        final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        GoogleIdTokenVerifier.Builder verifier =
                new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                        .setAudience(Collections.singletonList(googleClientId));
        final GoogleIdToken googleIdToken = GoogleIdToken.parse(verifier.getJsonFactory(), tokenDto.getValue());
        final GoogleIdToken.Payload payload = googleIdToken.getPayload();
        Usuario usuario;
        if( usuarioService.findByUsername(payload.getEmail()) != null )
            usuario = usuarioService.findByUsername(payload.getEmail());
        else
            usuario = saveUsuario(payload.getEmail());
        TokenDto tokenRes = login(usuario);
        return new ResponseEntity(tokenRes, HttpStatus.OK);
    }

    private TokenDto login(Usuario usuario){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(usuario.getEmail(), "123456")
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = JWT.create()
                .withSubject(usuario.getEmail())
                .withExpiresAt(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
                .sign(HMAC512(""));
        TokenDto tokenDto = new TokenDto();
        tokenDto.setValue(jwt);
        return tokenDto;
    }

    private Usuario saveUsuario(String email){
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode("123456"));
        Permissao permissao = new Permissao();
        if (email.contains("@alunos.utfpr.edu.br") || email.contains("@administrativo.utfpr.edu.br") ) {
            permissao = permissaoService.findByNome("ROLE_ALUNO");
        } else if (email.contains("@utfpr.edu.br") || email.contains("@professores.utfpr.edu.br")) {
            permissao = permissaoService.findByNome("ROLE_PROFESSOR");
        }
        usuario.setPermissoes( new HashSet<>());
        usuario.getPermissoes().add(permissao);
        return usuarioService.save(usuario);
    }

}
