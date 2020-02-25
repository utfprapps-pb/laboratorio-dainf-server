package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("usuario")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

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
        return usuarioService.save(usuario);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable("id") Long id) {
        usuarioService.delete(id);
    }
}
