package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.dto.ReservaResponseDto;
import br.com.utfpr.gerenciamento.server.model.Reserva;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import br.com.utfpr.gerenciamento.server.service.ReservaService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("reserva")
public class ReservaController extends CrudController<Reserva, Long> {

  private final ReservaService reservaService;

  public ReservaController(ReservaService reservaService) {
    this.reservaService = reservaService;
  }

  @Override
  protected CrudService<Reserva, Long> getService() {
    return reservaService;
  }

  @GetMapping("find-all-by-authenticated-user")
  public List<ReservaResponseDto> findAllByAuthenticatedUser() {
    return reservaService.findAllByAuthenticatedUser();
  }

  @GetMapping("find-all-by-item/{idItem}")
  public List<ReservaResponseDto> findAllByIdItem(@PathVariable("idItem") Long idItem) {
    return reservaService.findAllByIdItem(idItem);
  }

  @Override
  public void postSave(Reserva object) {
    reservaService.sendEmailConfirmacaoReserva(object);
  }
}
