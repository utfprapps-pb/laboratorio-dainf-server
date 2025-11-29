package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.dto.NadaConstaRequestDto;
import br.com.utfpr.gerenciamento.server.dto.NadaConstaResponseDto;
import br.com.utfpr.gerenciamento.server.exception.EntityNotFoundException;
import br.com.utfpr.gerenciamento.server.exception.MethodNotAllowedException;
import br.com.utfpr.gerenciamento.server.model.NadaConsta;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import br.com.utfpr.gerenciamento.server.service.NadaConstaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
public class NadaConstaController extends CrudController<NadaConsta, Long, NadaConstaResponseDto> {
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
  protected CrudService<NadaConsta, Long, NadaConstaResponseDto> getService() {
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
    throw new MethodNotAllowedException("Nao e permitido excluir nada consta.");
  }

  /**
   * Verifica se as pendências do usuário foram resolvidas e atualiza o status da solicitação.
   *
   * @param id Identificador da solicitação de Nada Consta
   * @return Dados atualizados da solicitação
   */
  @PutMapping("/verificar-pendencias/{id}")
  public ResponseEntity<NadaConstaResponseDto> verificarPendencias(@PathVariable("id") Long id) {
    NadaConstaResponseDto response = nadaConstaService.verificarPendenciasNadaConsta(id);
    return ResponseEntity.ok(response);
  }

  /**
   * Invalida uma declaração de Nada Consta emitida.
   *
   * @param id Identificador da solicitação de Nada Consta
   * @return Dados atualizados da solicitação
   */
  @PutMapping("/invalidar/{id}")
  public ResponseEntity<NadaConstaResponseDto> invalidarNadaConsta(@PathVariable("id") Long id) {
    NadaConstaResponseDto response = nadaConstaService.invalidarNadaConsta(id);
    return ResponseEntity.ok(response);
  }

  /**
   * Reenvia o email de Nada Consta utilizando os dados originais de emissão.
   *
   * @param id Identificador da solicitação de Nada Consta
   * @return ResponseEntity com status de sucesso ou erro
   */
  @PostMapping("/{id}/reenvio")
  public ResponseEntity<Void> reenviarNadaConsta(@PathVariable("id") Long id) {
    boolean enviado = nadaConstaService.reenviarNadaConsta(id);
    if (enviado) {
      return ResponseEntity.ok().build();
    } else {
      throw new EntityNotFoundException("Nada Consta não encontrado ou não pode ser reenviado.");
    }
  }

  /**
   * Retorna o PDF da declaração Nada Consta emitida.
   *
   * @param id Identificador da solicitação de Nada Consta
   * @return PDF em bytes
   */
  @GetMapping(value = "/{id}/pdf", produces = "application/pdf")
  public ResponseEntity<byte[]> getNadaConstaPdf(@PathVariable("id") Long id) {
    byte[] pdf = nadaConstaService.gerarNadaConstaPdf(id);
    return ResponseEntity.ok()
        .header("Content-Disposition", "attachment; filename=nada-consta.pdf")
        .body(pdf);
  }
}
