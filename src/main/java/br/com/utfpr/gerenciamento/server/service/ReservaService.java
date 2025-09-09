package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.dto.ReservaResponseDto;
import br.com.utfpr.gerenciamento.server.model.Reserva;
import java.util.List;

public interface ReservaService extends CrudService<Reserva, Long> {

  List<ReservaResponseDto> findAllByUsername(String username);

  List<ReservaResponseDto> findAllByIdItem(Long idItem);

  void finalizarReserva(Long idReserva);

  void sendEmailConfirmacaoReserva(Reserva reserva);

  ReservaResponseDto convertToDto(Reserva entity);
}
