package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.dto.NadaConstaRequestDto;
import br.com.utfpr.gerenciamento.server.dto.NadaConstaResponseDto;
import br.com.utfpr.gerenciamento.server.model.NadaConsta;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import br.com.utfpr.gerenciamento.server.service.NadaConstaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controller responsável pelos endpoints de Nada Consta.
 *
 * <p>Endpoints disponíveis:
 *
 * <ul>
 *   <li>POST /nadaconsta/solicitar - Solicita uma nova declaração de Nada Consta para o usuário
 *       informado.
 *   <li>PUT /nadaconsta/verificar-pendencias/{id} - Verifica se as pendências do usuário foram
 *       resolvidas e atualiza o status da solicitação.
 *   <li>DELETE /nadaconsta/{id} - Não permitido. Exclusão de Nada Consta não é suportada.
 * </ul>
 */
@RestController
@RequestMapping("/nadaconsta")
public class NadaConstaController extends CrudController<NadaConsta, Long> {
  /** Service para operações de Nada Consta. */
  private final NadaConstaService nadaConstaService;

  /**
   * Construtor do controller de Nada Consta.
   *
   * @param nadaConstaService Service de Nada Consta
   */
  public NadaConstaController(NadaConstaService nadaConstaService) {
    this.nadaConstaService = nadaConstaService;
  }

  /** Retorna o service utilizado pelo controller. */
  @Override
  protected CrudService<NadaConsta, Long> getService() {
    return nadaConstaService;
  }

  /**
   * Solicita uma declaração de Nada Consta para o usuário informado.
   *
   * @param request Dados do usuário (documento)
   * @return Dados da solicitação de Nada Consta
   */
  @PostMapping("/solicitar")
  public ResponseEntity<NadaConstaResponseDto> solicitarNadaConsta(
      @Valid @RequestBody NadaConstaRequestDto request) {
    NadaConstaResponseDto response = nadaConstaService.solicitarNadaConsta(request.getDocumento());
    return ResponseEntity.ok(response);
  }

  /**
   * Exclusão de Nada Consta não é permitida.
   *
   * @param id Identificador da solicitação
   */
  @Override
  @DeleteMapping("/{id}")
  public void delete(@PathVariable("id") Long id) {
    throw new ResponseStatusException(
        HttpStatus.METHOD_NOT_ALLOWED, "Não é permitido excluir nada consta.");
  }

  /**
   * Verifica se as pendências do usuário foram resolvidas e atualiza o status da solicitação.
   *
   * @param id Identificador da solicitação de Nada Consta
   * @return Dados atualizados da solicitação
   */
  @PutMapping("/verificar-pendencias/{id}")
  public ResponseEntity<NadaConstaResponseDto> verificarPendencias(@PathVariable("id") Long id) {
    try {
      NadaConstaResponseDto response = nadaConstaService.verificarPendenciasNadaConsta(id);
      return ResponseEntity.ok(response);
    } catch (RuntimeException ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  /**
   * Invalida uma declaração de Nada Consta emitida.
   *
   * @param id Identificador da solicitação de Nada Consta
   * @return Dados atualizados da solicitação
   */
  @PutMapping("/invalidar/{id}")
  public ResponseEntity<NadaConstaResponseDto> invalidarNadaConsta(@PathVariable("id") Long id) {
    try {
      NadaConstaResponseDto response = nadaConstaService.invalidarNadaConsta(id);
      return ResponseEntity.ok(response);
    } catch (RuntimeException ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }
}
