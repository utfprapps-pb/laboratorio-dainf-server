package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.repository.ItemRepository;
import br.com.utfpr.gerenciamento.server.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class ItemServiceImpl extends CrudServiceImpl<Item, Long> implements ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Override
    protected JpaRepository<Item, Long> getRepository() {
        return itemRepository;
    }
}
