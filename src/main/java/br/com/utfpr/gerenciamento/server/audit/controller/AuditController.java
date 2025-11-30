package br.com.utfpr.gerenciamento.server.audit.controller;

import br.com.utfpr.gerenciamento.server.audit.dto.AuditEntryDto;
import br.com.utfpr.gerenciamento.server.audit.service.AuditService;
import br.com.utfpr.gerenciamento.server.model.*;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.Audited;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para consulta de histórico de auditoria.
 *
 * <p>Todos os endpoints são protegidos por ROLE_ADMIN, garantindo que apenas administradores possam
 * consultar o histórico de alterações das entidades.
 *
 * @author Rodrigo Izidoro
 * @see AuditService
 */
@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
@Validated
@Slf4j
public class AuditController {

  private static final int MAX_PAGE_SIZE = 100;
  private static final int DEFAULT_PAGE_SIZE = 20;

  private final AuditService auditService;

  private static final Map<String, Class<?>> ENTITY_MAP =
      Map.ofEntries(
          Map.entry("emprestimo", Emprestimo.class),
          Map.entry("emprestimo-item", EmprestimoItem.class),
          Map.entry("emprestimo-devolucao-item", EmprestimoDevolucaoItem.class),
          Map.entry("item", Item.class),
          Map.entry("item-image", ItemImage.class),
          Map.entry("usuario", Usuario.class),
          Map.entry("saida", Saida.class),
          Map.entry("saida-item", SaidaItem.class),
          Map.entry("reserva", Reserva.class),
          Map.entry("reserva-item", ReservaItem.class),
          Map.entry("compra", Compra.class),
          Map.entry("compra-item", CompraItem.class),
          Map.entry("solicitacao", Solicitacao.class),
          Map.entry("solicitacao-item", SolicitacaoItem.class),
          Map.entry("grupo", Grupo.class),
          Map.entry("fornecedor", Fornecedor.class),
          Map.entry("nada-consta", NadaConsta.class));

  /**
   * Obtém o histórico de revisões de uma entidade com paginação.
   *
   * @param entidade nome da entidade (ex: emprestimo, item, usuario)
   * @param id identificador da entidade
   * @param page número da página (0-indexed)
   * @param size tamanho da página (máximo 100)
   * @return página de entradas de auditoria
   */
  @GetMapping("/{entidade}/{id}")
  public ResponseEntity<Page<AuditEntryDto>> getHistorico(
      @PathVariable String entidade,
      @PathVariable @Min(1) Long id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    Class<?> entityClass = validarEntidade(entidade);
    if (entityClass == null) {
      return ResponseEntity.notFound().build();
    }

    int safeSize = Math.min(size, MAX_PAGE_SIZE);
    if (safeSize <= 0) {
      safeSize = DEFAULT_PAGE_SIZE;
    }

    PageRequest pageRequest =
        PageRequest.of(page, safeSize, Sort.by(Sort.Direction.DESC, "revisao"));
    Page<AuditEntryDto> historico = auditService.getHistoricoPaginado(entityClass, id, pageRequest);
    return ResponseEntity.ok(historico);
  }

  /**
   * Obtém o número total de revisões de uma entidade.
   *
   * @param entidade nome da entidade
   * @param id identificador da entidade
   * @return contagem de revisões
   */
  @GetMapping("/{entidade}/{id}/count")
  public ResponseEntity<Long> contarRevisoes(
      @PathVariable String entidade, @PathVariable @Min(1) Long id) {

    Class<?> entityClass = validarEntidade(entidade);
    if (entityClass == null) {
      return ResponseEntity.notFound().build();
    }

    long count = auditService.contarRevisoes(entityClass, id);
    return ResponseEntity.ok(count);
  }

  /**
   * Obtém informações de uma revisão específica.
   *
   * @param revisao número da revisão
   * @return informações da revisão
   */
  @GetMapping("/revisao/{revisao}")
  public ResponseEntity<AuditEntryDto> getRevisao(@PathVariable @Min(1) Long revisao) {
    AuditEntryDto revInfo = auditService.getRevisao(revisao);
    if (revInfo == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(revInfo);
  }

  /**
   * Lista as entidades disponíveis para consulta de auditoria.
   *
   * @return lista de nomes de entidades
   */
  @GetMapping("/entidades")
  public ResponseEntity<List<String>> listarEntidades() {
    return ResponseEntity.ok(ENTITY_MAP.keySet().stream().sorted().toList());
  }

  private Class<?> validarEntidade(String entidade) {
    if (entidade == null || !entidade.matches("^[a-z\\-]+$")) {
      log.warn("Nome de entidade inválido: {}", entidade);
      return null;
    }

    Class<?> entityClass = ENTITY_MAP.get(entidade.toLowerCase());
    if (entityClass == null) {
      log.warn("Entidade não encontrada: {}", entidade);
      return null;
    }

    if (!entityClass.isAnnotationPresent(Audited.class)) {
      log.error("SECURITY: Tentativa de acesso a entidade não auditada: {}", entityClass.getName());
      return null;
    }

    return entityClass;
  }
}
