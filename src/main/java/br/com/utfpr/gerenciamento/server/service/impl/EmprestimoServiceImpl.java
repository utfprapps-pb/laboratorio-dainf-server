package br.com.utfpr.gerenciamento.server.service.impl;

import static br.com.utfpr.gerenciamento.server.enumeration.UserRole.ROLE_ADMINISTRADOR_NAME;
import static br.com.utfpr.gerenciamento.server.enumeration.UserRole.ROLE_LABORATORISTA_NAME;

import br.com.utfpr.gerenciamento.server.annotation.InvalidateDashboardCache;
import br.com.utfpr.gerenciamento.server.dto.EmprestimoResponseDto;
import br.com.utfpr.gerenciamento.server.enumeration.StatusDevolucao;
import br.com.utfpr.gerenciamento.server.enumeration.TipoItem;
import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.model.EmprestimoDevolucaoItem;
import br.com.utfpr.gerenciamento.server.model.EmprestimoItem;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardEmprestimoDia;
import br.com.utfpr.gerenciamento.server.model.dashboards.DashboardItensEmprestados;
import br.com.utfpr.gerenciamento.server.model.filter.EmprestimoFilter;
import br.com.utfpr.gerenciamento.server.model.modelTemplateEmail.EmprestimoTemplate;
import br.com.utfpr.gerenciamento.server.repository.EmprestimoRepository;
import br.com.utfpr.gerenciamento.server.repository.UsuarioRepository;
import br.com.utfpr.gerenciamento.server.service.EmailService;
import br.com.utfpr.gerenciamento.server.service.EmprestimoService;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import br.com.utfpr.gerenciamento.server.specification.EmprestimoSpecifications;
import br.com.utfpr.gerenciamento.server.util.DateUtil;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmprestimoServiceImpl extends CrudServiceImpl<Emprestimo, Long>
    implements EmprestimoService {

  private final EmprestimoRepository emprestimoRepository;
  private final UsuarioService usuarioService;
  private final EmailService emailService;
  private final UsuarioRepository usuarioRepository;

  private final ModelMapper modelMapper;

  public EmprestimoServiceImpl(
      EmprestimoRepository emprestimoRepository,
      UsuarioService usuarioService,
      EmailService emailService,
      UsuarioRepository usuarioRepository,
      ModelMapper modelMapper) {
    this.emprestimoRepository = emprestimoRepository;
    this.usuarioService = usuarioService;
    this.emailService = emailService;
    this.usuarioRepository = usuarioRepository;
    this.modelMapper = modelMapper;
  }

  private static final Logger LOGGER = Logger.getLogger(EmprestimoServiceImpl.class.getName());

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
   */
  @Override
  @Transactional
  @PreAuthorize("hasAnyRole('" + ROLE_LABORATORISTA_NAME + "', '" + ROLE_ADMINISTRADOR_NAME + "')")
  @InvalidateDashboardCache
  public Emprestimo save(Emprestimo entity) {
    entity.setUsuarioEmprestimo(
        usuarioRepository.getReferenceById(entity.getUsuarioEmprestimo().getId()));
    entity.setUsuarioResponsavel(
        usuarioRepository.getReferenceById(
            usuarioService
                .findByUsername(
                    (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .getId()));

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
  @Transactional
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
  @Transactional
  public void changePrazoDevolucao(Long idEmprestimo, LocalDate novaData) {
    var emprestimo = super.findOne(idEmprestimo);
    emprestimo.setPrazoDevolucao(novaData);
    super.save(emprestimo);
    emailService.sendEmailWithTemplate(
        converterEmprestimoToObjectTemplate(emprestimo),
        emprestimo.getUsuarioEmprestimo().getEmail(),
        "Alteração do prazo de devolução",
        "templateAlteracaoPrazoDevolucao");
  }

  @Override
  public void sendEmailConfirmacaoEmprestimo(Emprestimo emprestimo) {
    String template;
    if (!emprestimo.getEmprestimoDevolucaoItem().isEmpty()) {
      template = "templateConfirmacaoEmprestimo";
    } else {
      template = "templateConfirmacaoFinalizacaoEmprestimo";
    }
    emailService.sendEmailWithTemplate(
        converterEmprestimoToObjectTemplate(emprestimo),
        emprestimo.getUsuarioEmprestimo().getEmail(),
        "Confirmação de Empréstimo",
        template);
  }

  @Override
  public void sendEmailConfirmacaoDevolucao(Emprestimo emprestimo) {
    emailService.sendEmailWithTemplate(
        converterEmprestimoToObjectTemplate(emprestimo),
        emprestimo.getUsuarioEmprestimo().getEmail(),
        "Confirmação de Devolução do Empréstimo",
        "templateDevolucaoEmprestimo");
  }

  @Override
  @Transactional
  public void sendEmailPrazoDevolucaoProximo() {
    List<Emprestimo> emprestimos =
        emprestimoRepository.findByDataDevolucaoIsNullAndPrazoDevolucaoEquals(
            LocalDate.now().plusDays(3));
    if (!emprestimos.isEmpty()) {
      emprestimos.forEach(
          emprestimo -> {
            emailService.sendEmailWithTemplate(
                converterEmprestimoToObjectTemplate(emprestimo),
                emprestimo.getUsuarioEmprestimo().getEmail(),
                "Empréstimo próximo da data de devolução",
                "templateProximoPrazoDevolucaoEmprestimo");
            LOGGER.log(
                Level.INFO,
                "Email de aviso enviado com sucesso para: "
                    + emprestimo.getUsuarioEmprestimo().getEmail());
          });
    } else {
      LOGGER.log(Level.INFO, "Nenhum empréstimo vencerá daqui 3 dias.");
    }
  }

  @Override
  public EmprestimoResponseDto convertToDto(Emprestimo entity) {
    return modelMapper.map(entity, EmprestimoResponseDto.class);
  }

  private EmprestimoTemplate converterEmprestimoToObjectTemplate(Emprestimo e) {
    EmprestimoTemplate toReturn = new EmprestimoTemplate();
    toReturn.setUsuarioEmprestimo(e.getUsuarioEmprestimo().getNome());
    toReturn.setDtEmprestimo(DateUtil.parseLocalDateToString(e.getDataEmprestimo()));
    toReturn.setDtPrazoDevolucao(DateUtil.parseLocalDateToString(e.getPrazoDevolucao()));
    toReturn.setDtDevolucao(
        e.getDataDevolucao() != null
            ? DateUtil.parseLocalDateToString(e.getDataDevolucao())
            : null);
    toReturn.setUsuarioResponsavel(e.getUsuarioResponsavel().getNome());
    toReturn.setEmprestimoItem(e.getEmprestimoItem());
    toReturn.setEmprestimoDevolucaoItem(e.getEmprestimoDevolucaoItem());
    return toReturn;
  }
}
