package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.model.Reserva;
import br.com.utfpr.gerenciamento.server.repository.ReservaRepository;
import br.com.utfpr.gerenciamento.server.service.ReservaService;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservaServiceImpl extends CrudServiceImpl<Reserva, Long> implements ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;
    @Autowired
    private UsuarioService usuarioService;

    @Override
    protected JpaRepository<Reserva, Long> getRepository() {
        return reservaRepository;
    }

    @Override
    public List<Reserva> findAllByUsername(String username) {
        var usuario = usuarioService.findByUsername(username);
        return reservaRepository.findAllByUsuario(usuario);
    }

    @Override
    public List<Reserva> findAllByIdItem(Long idItem) {
        return reservaRepository.findReservaByIdItem(idItem);
    }
}
