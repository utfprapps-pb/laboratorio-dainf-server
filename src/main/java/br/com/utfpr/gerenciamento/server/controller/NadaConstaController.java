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

@RestController
@RequestMapping("/nadaconsta")
public class NadaConstaController extends CrudController<NadaConsta, Long, NadaConstaResponseDto> {

  private final NadaConstaService nadaConstaService;

  public NadaConstaController(NadaConstaService nadaConstaService) {
    this.nadaConstaService = nadaConstaService;
  }

  @Override
  protected CrudService<NadaConsta, Long, NadaConstaResponseDto> getService() {
    return nadaConstaService;
  }

  @PostMapping("/solicitar")
  public ResponseEntity<NadaConstaResponseDto> solicitarNadaConsta(
      @Valid @RequestBody NadaConstaRequestDto request) {
    NadaConstaResponseDto response = nadaConstaService.solicitarNadaConsta(request.getDocumento());
    return ResponseEntity.ok(response);
  }

  @Override
  @DeleteMapping("/{id}")
  public void delete(@PathVariable("id") Long id) {
    throw new ResponseStatusException(
        HttpStatus.METHOD_NOT_ALLOWED, "Não é permitido excluir nada consta.");
  }
}
