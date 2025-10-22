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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
    // Extrai username de forma segura do Authentication (evita ClassCastException)
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = extractUsername(auth);
    reserva.setUsuario(usuarioService.findByUsername(username));
    return super.save(reserva);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReservaResponseDto> findAllByUsername(String username) {
    // Extrai username de forma segura do Authentication (evita ClassCastException)
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String authenticatedUsername = extractUsername(auth);
    var usuario = usuarioService.findByUsername(authenticatedUsername);
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

  /**
   * Extrai o username do Authentication de forma segura.
   *
   * <p>Suporta dois cenários comuns do Spring Security: 1. Principal é String (username direto) 2.
   * Principal é UserDetails (precisa chamar getUsername())
   *
   * <p>Prioriza auth.getName() que funciona em ambos os casos, mas valida com fallback para
   * compatibilidade com diferentes configurações de segurança.
   *
   * @param auth Authentication do SecurityContext (pode ser null)
   * @return Username extraído do authentication
   * @throws IllegalStateException se authentication for null ou username não puder ser extraído
   */
  private String extractUsername(Authentication auth) {
    if (auth == null) {
      throw new IllegalStateException("Authentication não pode ser null");
    }

    // Prioriza getName() - funciona para ambos String e UserDetails
    String username = auth.getName();
    if (username != null && !username.trim().isEmpty()) {
      return username;
    }

    // Fallback: verifica se principal é UserDetails
    Object principal = auth.getPrincipal();
    if (principal instanceof UserDetails userDetails) {
      username = userDetails.getUsername();
      if (username != null && !username.trim().isEmpty()) {
        return username;
      }
    }

    // Fallback final: tenta cast para String (compatibilidade com configurações antigas)
    if (principal instanceof String stringPrincipal && !stringPrincipal.trim().isEmpty()) {
      return stringPrincipal;
    }

    throw new IllegalStateException(
        "Não foi possível extrair username do Authentication. Principal type: "
            + (principal != null ? principal.getClass().getName() : "null"));
  }
}
