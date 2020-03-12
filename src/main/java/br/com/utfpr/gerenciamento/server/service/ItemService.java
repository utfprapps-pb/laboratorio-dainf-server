package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.model.Item;

import java.util.List;

public interface ItemService extends CrudService<Item, Long> {

    List<Item> cidadeComplete(String query);
}
