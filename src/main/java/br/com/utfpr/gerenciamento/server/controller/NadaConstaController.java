package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.dto.NadaConstaRequestDto;
import br.com.utfpr.gerenciamento.server.dto.NadaConstaResponseDto;
import br.com.utfpr.gerenciamento.server.model.NadaConsta;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import br.com.utfpr.gerenciamento.server.service.NadaConstaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/nadaconsta")
public class NadaConstaController extends CrudController<NadaConsta, Long> {

  private final NadaConstaService nadaConstaService;

  public NadaConstaController(NadaConstaService nadaConstaService) {
    this.nadaConstaService = nadaConstaService;
  }

  @Override
  protected CrudService<NadaConsta, Long> getService() {
    return nadaConstaService;
  }

  @PostMapping("/solicitar")
  public ResponseEntity<NadaConstaResponseDto> solicitarNadaConsta(
      @RequestBody NadaConstaRequestDto request) {
    NadaConstaResponseDto response = nadaConstaService.solicitarNadaConsta(request.getDocumento());
    return ResponseEntity.ok(response);
  }
}
