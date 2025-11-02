package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.dto.SolicitacaoResponseDto;
import br.com.utfpr.gerenciamento.server.model.Solicitacao;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import br.com.utfpr.gerenciamento.server.service.SolicitacaoService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("solicitacao-compra")
public class SolicitacaoController extends CrudController<Solicitacao, Long,SolicitacaoResponseDto> {

  private final SolicitacaoService solicitacaoService;

  public SolicitacaoController(SolicitacaoService solicitacaoService) {
    this.solicitacaoService = solicitacaoService;
  }

  @Override
  protected CrudService<Solicitacao, Long,SolicitacaoResponseDto> getService() {
    return solicitacaoService;
  }

  @GetMapping("find-all-by-username/{username}")
  public List<SolicitacaoResponseDto> findAllByUsername(@PathVariable("username") String username) {
    return solicitacaoService.findAllByUsername(username);
  }
}
