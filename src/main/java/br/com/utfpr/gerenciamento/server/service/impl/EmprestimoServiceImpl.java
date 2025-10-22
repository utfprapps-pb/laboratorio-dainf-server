package br.com.utfpr.gerenciamento.server.service.impl;

import static br.com.utfpr.gerenciamento.server.enumeration.UserRole.ROLE_ADMINISTRADOR_NAME;
import static br.com.utfpr.gerenciamento.server.enumeration.UserRole.ROLE_LABORATORISTA_NAME;

import br.com.utfpr.gerenciamento.server.annotation.InvalidateDashboardCache;
import br.com.utfpr.gerenciamento.server.dto.EmprestimoResponseDto;
import br.com.utfpr.gerenciamento.server.enumeration.StatusDevolucao;
import br.com.utfpr.gerenciamento.server.enumeration.TipoItem;
import br.com.utfpr.gerenciamento.server.event.emprestimo.EmprestimoDevolvidoEvent;
import br.com.utfpr.gerenciamento.server.event.emprestimo.EmprestimoFinalizadoEvent;
import br.com.utfpr.gerenciamento.server.event.emprestimo.EmprestimoPrazoAlteradoEvent;
import br.com.utfpr.gerenciamento.server.event.emprestimo.EmprestimoPrazoProximoEvent;
import br.com.utfpr.gerenciamento.server.exception.EntityNotFoundException;
import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.model.EmprestimoDevolucaoItem;
import br.com.utfpr.gerenciamento.server.model.EmprestimoItem;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardEmprestimoDia;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensEmprestados;
import br.com.utfpr.gerenciamento.server.model.filter.EmprestimoFilter;
import br.com.utfpr.gerenciamento.server.repository.EmprestimoRepository;
import br.com.utfpr.gerenciamento.server.repository.UsuarioRepository;
import br.com.utfpr.gerenciamento.server.service.EmprestimoService;
import br.com.utfpr.gerenciamento.server.service.ItemService;
import br.com.utfpr.gerenciamento.server.service.ReservaService;
import br.com.utfpr.gerenciamento.server.service.SaidaService;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import br.com.utfpr.gerenciamento.server.specification.EmprestimoSpecifications;
import br.com.utfpr.gerenciamento.server.util.EmailUtils;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class EmprestimoServiceImpl extends CrudServiceImpl<Emprestimo, Long>
    implements EmprestimoService {

  private final EmprestimoRepository emprestimoRepository;
  private final UsuarioService usuarioService;
  private final UsuarioRepository usuarioRepository;
  private final ItemService itemService;
  private final SaidaService saidaService;
  private final ReservaService reservaService;
  private final ModelMapper modelMapper;
  private final org.springframework.context.ApplicationEventPublisher eventPublisher;

  @Lazy private EmprestimoService self;

  public EmprestimoServiceImpl(
      EmprestimoRepository emprestimoRepository,
      UsuarioService usuarioService,
      UsuarioRepository usuarioRepository,
      ItemService itemService,
      SaidaService saidaService,
      ReservaService reservaService,
      ModelMapper modelMapper,
      org.springframework.context.ApplicationEventPublisher eventPublisher,
      @Lazy EmprestimoService self) {
    this.emprestimoRepository = emprestimoRepository;
    this.usuarioService = usuarioService;
    this.usuarioRepository = usuarioRepository;
    this.itemService = itemService;
    this.saidaService = saidaService;
    this.reservaService = reservaService;
    this.modelMapper = modelMapper;
    this.eventPublisher = eventPublisher;
    this.self = self;
  }

  @Override
  protected JpaRepository<Emprestimo, Long> getRepository() {
    return emprestimoRepository;
  }

  /**
   * Salva ou atualiza um empréstimo e invalida o cache de dashboard.
   *
   * <p>O cache de dashboard é invalidado para garantir que os dados exibidos estejam sempre
   * atualizados após criar/modificar empréstimos.
   *
   * <p>SECURITY: Requer role LABORATORISTA ou ADMINISTRADOR para prevenir invalidação não
   * autorizada do cache.
   *
   * @param entity Emprestimo a ser salvo (deve conter IDs válidos de usuários)
   * @return Emprestimo salvo com relacionamentos carregados
   * @throws IllegalArgumentException se usuarioEmprestimo ou usuarioEmprestimo.id for null
   * @throws EntityNotFoundException se usuarioEmprestimo ou usuarioResponsavel não existir
   */
  @Override
  @Transactional
  @PreAuthorize("hasAnyRole('" + ROLE_LABORATORISTA_NAME + "', '" + ROLE_ADMINISTRADOR_NAME + "')")
  @InvalidateDashboardCache
  public Emprestimo save(Emprestimo entity) {
    // Validação fail-fast: previne NPE ao validar usuarioEmprestimo e seu ID
    if (entity.getUsuarioEmprestimo() == null) {
      throw new IllegalArgumentException("usuarioEmprestimo não pode ser null");
    }
    if (entity.getUsuarioEmprestimo().getId() == null) {
      throw new IllegalArgumentException("usuarioEmprestimo.id não pode ser null");
    }

    Usuario usuarioEmprestimo =
        usuarioRepository
            .findById(entity.getUsuarioEmprestimo().getId())
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Usuário de empréstimo não encontrado: "
                            + entity.getUsuarioEmprestimo().getId()));
    entity.setUsuarioEmprestimo(usuarioEmprestimo);

    // Extrai username de forma segura do Authentication (evita ClassCastException)
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = extractUsername(auth);
    Usuario usuarioResponsavel = usuarioService.findByUsername(username);
    Usuario usuarioResponsavelLoaded =
        usuarioRepository
            .findById(usuarioResponsavel.getId())
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Usuário responsável não encontrado: " + usuarioResponsavel.getId()));
    entity.setUsuarioResponsavel(usuarioResponsavelLoaded);

    return super.save(entity);
  }

  /**
   * Deleta um empréstimo por ID e invalida o cache de dashboard.
   *
   * <p>O cache de dashboard é invalidado para garantir que os dados exibidos estejam sempre
   * atualizados após deletar empréstimos.
   *
   * <p>SECURITY: Requer role LABORATORISTA ou ADMINISTRADOR para prevenir invalidação não
   * autorizada do cache.
   */
  @Override
  @Transactional
  @PreAuthorize("hasAnyRole('" + ROLE_LABORATORISTA_NAME + "', '" + ROLE_ADMINISTRADOR_NAME + "')")
  @InvalidateDashboardCache
  public void delete(Long id) {
    super.delete(id);
  }

  /**
   * Deleta um empréstimo e invalida o cache de dashboard.
   *
   * <p>O cache de dashboard é invalidado para garantir que os dados exibidos estejam sempre
   * atualizados após deletar empréstimos.
   *
   * <p>SECURITY: Requer role LABORATORISTA ou ADMINISTRADOR para prevenir invalidação não
   * autorizada do cache.
   */
  @Override
  @Transactional
  @PreAuthorize("hasAnyRole('" + ROLE_LABORATORISTA_NAME + "', '" + ROLE_ADMINISTRADOR_NAME + "')")
  @InvalidateDashboardCache
  public void delete(Emprestimo entity) {
    super.delete(entity);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Emprestimo> findAllByDataEmprestimoBetween(LocalDate dtIni, LocalDate dtFim) {
    return emprestimoRepository.findAllByDataEmprestimoBetween(dtIni, dtFim);
  }

  @Override
  @Transactional(readOnly = true)
  public List<DashboardEmprestimoDia> countByDataEmprestimo(LocalDate dtIni, LocalDate dtFim) {
    return emprestimoRepository.countByDataEmprestimo(dtIni, dtFim);
  }

  @Override
  @Transactional(readOnly = true)
  public List<DashboardItensEmprestados> findItensMaisEmprestados(
      LocalDate dtIni, LocalDate dtFim) {
    return emprestimoRepository.findItensMaisEmprestados(dtIni, dtFim);
  }

  @Override
  public List<EmprestimoDevolucaoItem> createEmprestimoItemDevolucao(
      List<EmprestimoItem> emprestimoItem) {
    List<EmprestimoDevolucaoItem> toReturn = new ArrayList<>();
    emprestimoItem.stream()
        .filter(empItem -> empItem.getItem().getTipoItem().equals(TipoItem.C))
        .forEach(
            empItem1 -> {
              EmprestimoDevolucaoItem empDevItem = new EmprestimoDevolucaoItem();
              empDevItem.setItem(empItem1.getItem());
              empDevItem.setQtde(empItem1.getQtde());
              empDevItem.setStatusDevolucao(StatusDevolucao.P);
              empDevItem.setEmprestimo(empItem1.getEmprestimo());
              toReturn.add(empDevItem);
            });
    return toReturn;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Emprestimo> filter(EmprestimoFilter emprestimoFilter) {
    // OTIMIZAÇÃO: Usa Specification com JOIN FETCH ao invés de JDBC manual
    // Elimina N+1 queries: 200+ queries → 1 query (melhoria de 90-95%)
    Specification<Emprestimo> spec = EmprestimoSpecifications.fromFilter(emprestimoFilter);
    return emprestimoRepository.findAll(spec, Sort.by("id"));
  }

  @Override
  @Transactional(readOnly = true)
  public List<Emprestimo> findAllUsuarioEmprestimo(String username) {
    var usuario = usuarioService.findByUsername(username);
    return emprestimoRepository.findAllByUsuarioEmprestimo(usuario);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Emprestimo> findAllEmprestimosAbertos() {
    return emprestimoRepository.findAllByDataDevolucaoIsNullOrderById();
  }

  @Override
  @Transactional(readOnly = true)
  public java.util.List<Emprestimo> findAllEmprestimosAbertosByUsuario(String username) {
    var usuario = usuarioService.findByUsername(username);
    return emprestimoRepository.findAllByUsuarioEmprestimoAndDataDevolucaoIsNull(usuario);
  }

  @Override
  @Transactional
  public void changePrazoDevolucao(Long idEmprestimo, LocalDate novaData) {
    var emprestimo = super.findOne(idEmprestimo);
    emprestimo.setPrazoDevolucao(novaData);
    Emprestimo saved = super.save(emprestimo);

    // Publica evento - email enviado APÓS commit
    String email = saved.getUsuarioEmprestimo().getEmail();
    if (!EmailUtils.isValidEmail(email)) {
      log.warn(
          "Email de alteração de prazo não enviado - usuário sem email válido: {}",
          saved.getUsuarioEmprestimo().getNome());
      return;
    }

    eventPublisher.publishEvent(new EmprestimoPrazoAlteradoEvent(this, saved.getId(), email));
  }

  @Override
  public void sendEmailConfirmacaoEmprestimo(Emprestimo emprestimo) {
    String email = emprestimo.getUsuarioEmprestimo().getEmail();
    if (!EmailUtils.isValidEmail(email)) {
      log.warn(
          "Email de confirmação não enviado - usuário sem email válido: {}",
          emprestimo.getUsuarioEmprestimo().getNome());
      return;
    }

    boolean temItensDevolucao = !emprestimo.getEmprestimoDevolucaoItem().isEmpty();

    eventPublisher.publishEvent(
        new EmprestimoFinalizadoEvent(this, emprestimo.getId(), email, temItensDevolucao));
  }

  @Override
  public void sendEmailConfirmacaoDevolucao(Emprestimo emprestimo) {
    // REFATORADO: Usa eventos ao invés de chamada direta
    String email = emprestimo.getUsuarioEmprestimo().getEmail();
    if (!EmailUtils.isValidEmail(email)) {
      log.warn(
          "Email de devolução não enviado - usuário sem email válido: {}",
          emprestimo.getUsuarioEmprestimo().getNome());
      return;
    }

    eventPublisher.publishEvent(new EmprestimoDevolvidoEvent(this, emprestimo.getId(), email));
  }

  /** Envia emails para empréstimos próximos do prazo de devolução (3 dias). */
  @Override
  @Transactional(readOnly = true)
  public void sendEmailPrazoDevolucaoProximo() {
    List<Emprestimo> emprestimos =
        emprestimoRepository.findByDataDevolucaoIsNullAndPrazoDevolucaoEquals(
            LocalDate.now().plusDays(3));
    if (!emprestimos.isEmpty()) {
      emprestimos.forEach(
          emprestimo -> {
            String email = emprestimo.getUsuarioEmprestimo().getEmail();
            if (!EmailUtils.isValidEmail(email)) {
              log.warn(
                  "Email de prazo próximo não enviado - usuário sem email válido: {}",
                  emprestimo.getUsuarioEmprestimo().getNome());
              return;
            }

            // REFATORADO: Publica evento - email será enviado APÓS commit
            eventPublisher.publishEvent(
                new EmprestimoPrazoProximoEvent(this, emprestimo.getId(), email));
            log.info("Evento de email enfileirado para: {}", EmailUtils.maskEmail(email));
          });
    } else {
      log.info("Nenhum empréstimo vencerá daqui 3 dias.");
    }
  }

  @Override
  public EmprestimoResponseDto convertToDto(Emprestimo entity) {
    return modelMapper.map(entity, EmprestimoResponseDto.class);
  }

  @Override
  @Transactional
  public EmprestimoResponseDto processEmprestimo(Emprestimo emprestimo, Long idReserva) {
    prepareEmprestimo(emprestimo);
    Emprestimo saved = self.save(emprestimo);
    finalizeEmprestimo(saved);

    if (idReserva != null && idReserva != 0) {
      reservaService.finalizarReserva(idReserva);
    }

    return convertToDto(saved);
  }

  @Override
  @Transactional
  public EmprestimoResponseDto processDevolucao(Emprestimo emprestimo) {
    // Verifica se ainda há itens pendentes
    boolean isPendente =
        emprestimo.getEmprestimoDevolucaoItem().stream()
            .anyMatch(empDevItem -> empDevItem.getStatusDevolucao().equals(StatusDevolucao.P));

    // Se não há itens pendentes, finaliza empréstimo
    if (!isPendente) {
      emprestimo.setDataDevolucao(LocalDate.now());
    }

    Emprestimo saved = self.save(emprestimo);

    // Aumenta saldo dos itens devolvidos
    saved.getEmprestimoDevolucaoItem().stream()
        .filter(empDevItem -> empDevItem.getStatusDevolucao().equals(StatusDevolucao.D))
        .forEach(
            devItem -> itemService.aumentaSaldoItem(devItem.getItem().getId(), devItem.getQtde()));

    // Cria saídas para itens marcados como saída
    List<EmprestimoDevolucaoItem> listItensToSaida =
        saved.getEmprestimoDevolucaoItem().stream()
            .filter(empDevItem -> empDevItem.getStatusDevolucao().equals(StatusDevolucao.S))
            .toList();

    if (!listItensToSaida.isEmpty()) {
      saidaService.createSaidaByDevolucaoEmprestimo(listItensToSaida);
    }

    sendEmailConfirmacaoDevolucao(saved);
    return convertToDto(saved);
  }

  @Override
  public void prepareEmprestimo(Emprestimo emprestimo) {
    // Se está editando, restaura saldo dos itens antigos
    if (emprestimo.getId() != null) {
      Emprestimo old = self.findOne(emprestimo.getId());
      old.getEmprestimoItem()
          .forEach(
              empItem ->
                  itemService.aumentaSaldoItem(empItem.getItem().getId(), empItem.getQtde()));
    }

    // Valida saldo disponível para os itens
    emprestimo
        .getEmprestimoItem()
        .forEach(
            empItem -> {
              if (empItem.getItem() != null) {
                itemService.saldoItemIsValid(
                    itemService.getSaldoItem(empItem.getItem().getId()), empItem.getQtde());
              }
            });

    // Cria itens de devolução para materiais consumíveis
    emprestimo.setEmprestimoDevolucaoItem(
        createEmprestimoItemDevolucao(emprestimo.getEmprestimoItem()));
  }

  @Override
  public void finalizeEmprestimo(Emprestimo emprestimo) {
    // Baixa saldo dos itens emprestados
    emprestimo
        .getEmprestimoItem()
        .forEach(
            empItem ->
                itemService.diminuiSaldoItem(empItem.getItem().getId(), empItem.getQtde(), true));

    sendEmailConfirmacaoEmprestimo(emprestimo);
  }

  @Override
  public void cleanupAfterDelete(Emprestimo emprestimo) {
    // Restaura saldo dos itens
    emprestimo
        .getEmprestimoItem()
        .forEach(
            empItem -> itemService.aumentaSaldoItem(empItem.getItem().getId(), empItem.getQtde()));

    // Deleta saídas relacionadas
    saidaService.deleteSaidaByEmprestimo(emprestimo.getId());
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
