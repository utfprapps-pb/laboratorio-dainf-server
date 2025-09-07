package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.model.Cidade;
import br.com.utfpr.gerenciamento.server.model.Estado;
import br.com.utfpr.gerenciamento.server.repository.CidadeRepository;
import br.com.utfpr.gerenciamento.server.service.CidadeService;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CidadeServiceImpl extends CrudServiceImpl<Cidade, Long> implements CidadeService {

  private final CidadeRepository cidadeRepository;

  public CidadeServiceImpl(CidadeRepository cidadeRepository) {
    this.cidadeRepository = cidadeRepository;
  }

  @Override
  protected JpaRepository<Cidade, Long> getRepository() {
    return cidadeRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Cidade> cidadeComplete(String query) {
    if ("".equalsIgnoreCase(query)) {
      return this.cidadeRepository.findAll();
    } else {
      return this.cidadeRepository.findByNomeLikeIgnoreCase("%" + query + "%");
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<Cidade> completeByEstado(String query, Estado estado) {
    if ("".equalsIgnoreCase(query)) {
      return this.cidadeRepository.findAllByEstado(estado);
    } else {
      return this.cidadeRepository.findByNomeLikeIgnoreCaseAndEstado("%" + query + "%", estado);
    }
  }
}
