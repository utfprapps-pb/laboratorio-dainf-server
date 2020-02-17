package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.model.Grupo;

import java.util.List;

public interface GrupoService extends CrudService<Grupo, Long> {

    List<Grupo> completeGrupo(String query);
}
