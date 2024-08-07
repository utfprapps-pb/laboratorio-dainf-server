package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.model.Reserva;
import br.com.utfpr.gerenciamento.server.model.modelTemplateEmail.ReservaTemplate;
import br.com.utfpr.gerenciamento.server.repository.ReservaRepository;
import br.com.utfpr.gerenciamento.server.service.EmailService;
import br.com.utfpr.gerenciamento.server.service.ReservaService;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import br.com.utfpr.gerenciamento.server.util.DateUtil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservaServiceImpl extends CrudServiceImpl<Reserva, Long> implements ReservaService {

    private final ReservaRepository reservaRepository;
    private final UsuarioService usuarioService;
    private final EmailService emailService;

    public ReservaServiceImpl(ReservaRepository reservaRepository, UsuarioService usuarioService, EmailService emailService) {
        this.reservaRepository = reservaRepository;
        this.usuarioService = usuarioService;
        this.emailService = emailService;
    }

    @Override
    protected JpaRepository<Reserva, Long> getRepository() {
        return reservaRepository;
    }

    @Override
    public Reserva save(Reserva reserva) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        reserva.setUsuario(usuarioService.findByUsername((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal()));
        return super.save(reserva);
    }

    @Override
    public List<Reserva> findAllByUsername(String username) {
        var usuario = usuarioService.findByUsername((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        return reservaRepository.findAllByUsuario(usuario);
    }

    @Override
    public List<Reserva> findAllByIdItem(Long idItem) {
        return reservaRepository.findReservaByIdItem(idItem);
    }

    @Override
    public void finalizarReserva(Long idReserva) {
        var reserva = this.findOne(idReserva);
        emailService.sendEmailWithTemplate(
                converterObjectToTemplateEmail(reserva),
                reserva.getUsuario().getEmail(),
                "Reserva Finalizada",
                "templateFinalizacaoReserva"
        );
        this.delete(idReserva);
    }

    @Override
    public void sendEmailConfirmacaoReserva(Reserva reserva) {
        emailService.sendEmailWithTemplate(
                converterObjectToTemplateEmail(reserva),
                reserva.getUsuario().getEmail(),
                "Confirmação de Reserva de Materiais",
                "templateConfirmacaoReserva"
        );
    }

    public ReservaTemplate converterObjectToTemplateEmail(Reserva reserva) {
        ReservaTemplate toReturn = new ReservaTemplate();
        toReturn.setUsuario(reserva.getUsuario().getNome());
        toReturn.setDtReserva(DateUtil.parseLocalDateToString(reserva.getDataReserva()));
        toReturn.setDtRetirada(DateUtil.parseLocalDateToString(reserva.getDataRetirada()));
        toReturn.setReservaItem(reserva.getReservaItem());
        return toReturn;
    }
}
