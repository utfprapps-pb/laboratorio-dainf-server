package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.model.Reserva;

import java.util.List;

public interface ReservaService extends CrudService<Reserva, Long> {

    List<Reserva> findAllByUsername(String username);
}
