package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.dto.ReservaListDto;
import br.com.utfpr.gerenciamento.server.dto.ReservaResponseDto;
import br.com.utfpr.gerenciamento.server.model.Reserva;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservaService extends CrudService<Reserva, Long, ReservaResponseDto> {

  /**
   * Busca paginada para listagem com DTO simplificado.
   *
   * @param filter Filtro textual opcional
   * @param pageable Configuração de paginação
   * @return Página de DTOs simplificados
   */
  Page<ReservaListDto> findAllPagedList(String filter, Pageable pageable);

  /**
   * Busca paginada para listagem por usuário com DTO simplificado.
   *
   * @param filter Filtro textual opcional
   * @param pageable Configuração de paginação
   * @param username Username do usuário
   * @return Página de DTOs simplificados do usuário
   */
  Page<ReservaListDto> findAllPagedListByUser(String filter, Pageable pageable, String username);

  List<ReservaResponseDto> findAllByAuthenticatedUser();

  List<ReservaResponseDto> findAllByIdItem(Long idItem);

  void finalizarReserva(Long idReserva);

  void sendEmailConfirmacaoReserva(Reserva reserva);
}
