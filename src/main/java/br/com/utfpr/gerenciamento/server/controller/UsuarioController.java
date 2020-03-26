package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.model.Permissao;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.service.PermissaoService;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import br.com.utfpr.gerenciamento.server.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("usuario/")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private PermissaoService permissaoService;

    @GetMapping
    public List<Usuario> findAll() {
        return usuarioService.findAll();
    }

    @GetMapping("{id}")
    public Usuario findOne(@PathVariable("id") Long id) {
        return usuarioService.findOne(id);
    }

    @PostMapping
    public Usuario save(@RequestBody Usuario usuario) {
        if (!Util.isPasswordEncoded(usuario.getPassword())) {
            usuario.setPassword(new BCryptPasswordEncoder().encode(usuario.getPassword()));
        }
        Set<Permissao> permissoes = new HashSet<>();
        usuario.getPermissoes().stream().forEach(permissao ->
                permissoes.add(permissaoService.findOne(permissao.getId()))
        );
        usuario.setPermissoes(permissoes);
        return usuarioService.save(usuario);
    }

    @GetMapping("permissao")
    public List<Permissao> findAllPermissao() {
        return permissaoService.findAll();
    }

    @PostMapping("change-senha")
    public Usuario redefinirSenha(@RequestBody Usuario usuario,
                                  @RequestParam("senhaAtual") String senhaAtual) {
        Usuario userTemp = usuarioService.findOne(usuario.getId());
        BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder();

        if (bCrypt.matches(senhaAtual, userTemp.getPassword())) {
            usuario.setPassword(bCrypt.encode(usuario.getPassword()));
            return usuarioService.save(usuario);
        }
        throw new RuntimeException("Senha incorreta");
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable("id") Long id) {
        usuarioService.delete(id);
    }

    @GetMapping("/complete")
    public List<Usuario> complete(@RequestParam("query") String query) {
        return usuarioService.usuarioComplete(query);
    }

    @GetMapping("/find-by-username")
    public Usuario findByUsername(@RequestParam("username") String username) {
        return usuarioService.findByUsername(username);
    }
}
