package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.model.Reserva;
import br.com.utfpr.gerenciamento.server.model.Usuario;

import java.util.List;

public interface ReservaService extends CrudService<Reserva, Long> {

    List<Reserva> findAllByUsername(String username);

    List<Reserva> findAllByIdItem(Long idItem);

    void finalizarReserva(Long idReserva);

    void sendEmailConfirmacaoReserva(Reserva reserva);
}
