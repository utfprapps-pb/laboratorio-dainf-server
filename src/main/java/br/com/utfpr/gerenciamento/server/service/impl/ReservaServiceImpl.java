package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.dto.ReservaResponseDto;
import br.com.utfpr.gerenciamento.server.model.Reserva;
import br.com.utfpr.gerenciamento.server.model.modelTemplateEmail.ReservaTemplate;
import br.com.utfpr.gerenciamento.server.repository.ReservaRepository;
import br.com.utfpr.gerenciamento.server.service.EmailService;
import br.com.utfpr.gerenciamento.server.service.ReservaService;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import br.com.utfpr.gerenciamento.server.util.DateUtil;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservaServiceImpl extends CrudServiceImpl<Reserva, Long> implements ReservaService {

  private final ReservaRepository reservaRepository;
  private final UsuarioService usuarioService;
  private final EmailService emailService;

  private final ModelMapper modelMapper;

  public ReservaServiceImpl(
      ReservaRepository reservaRepository,
      UsuarioService usuarioService,
      EmailService emailService,
      ModelMapper modelMapper) {
    this.reservaRepository = reservaRepository;
    this.usuarioService = usuarioService;
    this.emailService = emailService;
    this.modelMapper = modelMapper;
  }

  @Override
  protected JpaRepository<Reserva, Long> getRepository() {
    return reservaRepository;
  }

  @Override
  @Transactional
  public Reserva save(Reserva reserva) {
    reserva.setUsuario(
        usuarioService.findByUsername(
            (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal()));
    return super.save(reserva);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReservaResponseDto> findAllByUsername(String username) {
    var usuario =
        usuarioService.findByUsername(
            (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    return reservaRepository.findAllByUsuario(usuario).stream().map(this::convertToDto).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReservaResponseDto> findAllByIdItem(Long idItem) {
    return reservaRepository.findReservaByIdItem(idItem).stream().map(this::convertToDto).toList();
  }

  @Override
  @Transactional
  public void finalizarReserva(Long idReserva) {
    var reserva = this.findOne(idReserva);
    emailService.sendEmailWithTemplate(
        converterObjectToTemplateEmail(reserva),
        reserva.getUsuario().getEmail(),
        "Reserva Finalizada",
        "templateFinalizacaoReserva");
    this.delete(idReserva);
  }

  @Override
  public void sendEmailConfirmacaoReserva(Reserva reserva) {
    emailService.sendEmailWithTemplate(
        converterObjectToTemplateEmail(reserva),
        reserva.getUsuario().getEmail(),
        "Confirmação de Reserva de Materiais",
        "templateConfirmacaoReserva");
  }

  @Override
  public ReservaResponseDto convertToDto(Reserva entity) {
    return modelMapper.map(entity, ReservaResponseDto.class);
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
