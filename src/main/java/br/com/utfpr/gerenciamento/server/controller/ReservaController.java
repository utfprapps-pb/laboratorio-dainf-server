package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.model.Reserva;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import br.com.utfpr.gerenciamento.server.service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("reserva")
public class ReservaController extends CrudController<Reserva, Long> {

    @Autowired
    private ReservaService reservaService;

    @Override
    protected CrudService<Reserva, Long> getService() {
        return reservaService;
    }

    @GetMapping("find-all-by-username/{username}")
    public List<Reserva> findAllByUsername(@PathVariable("username") String username) {
        return reservaService.findAllByUsername(username);
    }

    @GetMapping("find-all-by-item/{idItem}")
    public List<Reserva> findAllByIdItem(@PathVariable("idItem") Long idItem) {
        return reservaService.findAllByIdItem(idItem);
    }
}
