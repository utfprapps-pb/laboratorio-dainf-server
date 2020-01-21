package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.model.Reserva;
import br.com.utfpr.gerenciamento.server.repository.ReservaRepository;
import br.com.utfpr.gerenciamento.server.service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class ReservaServiceImpl extends CrudServiceImpl<Reserva, Long> implements ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Override
    protected JpaRepository<Reserva, Long> getRepository() {
        return reservaRepository;
    }
}
