package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.repository.ItemRepository;
import br.com.utfpr.gerenciamento.server.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ItemServiceImpl extends CrudServiceImpl<Item, Long> implements ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Override
    protected JpaRepository<Item, Long> getRepository() {
        return itemRepository;
    }

    @Override
    public List<Item> itemComplete(String query, Boolean hasEstoque) {
        BigDecimal zero = new BigDecimal(0);
        if ("".equalsIgnoreCase(query)) {
            if (hasEstoque) return itemRepository.findAllBySaldoIsGreaterThan(zero);
            else return itemRepository.findAll();
        } else {
            if (hasEstoque) {
                return itemRepository
                        .findByNomeLikeIgnoreCaseAndSaldoIsGreaterThan("%" + query + "%", zero);
            } else return itemRepository.findByNomeLikeIgnoreCase("%" + query + "%");
        }
    }

    @Override
    public List<Item> findByGrupo(Long id) {
        return itemRepository.findByGrupoId(id);
    }

    @Override
    public void diminuiSaldoItem(Long idItem, BigDecimal qtde, boolean needValidationSaldo) {
        Item itemToSave = itemRepository.findById(idItem).get();
        if (!needValidationSaldo || this.saldoItemIsValid(itemToSave.getSaldo(), qtde)) {
            itemToSave.setSaldo(itemToSave.getSaldo().subtract(qtde));
            itemRepository.save(itemToSave);
        }
    }

    @Override
    public void aumentaSaldoItem(Long idItem, BigDecimal qtde) {
        Item itemToSave = itemRepository.findById(idItem).get();
        itemToSave.setSaldo(itemToSave.getSaldo().add(qtde));
        itemRepository.save(itemToSave);
    }


    @Override
    public BigDecimal getSaldoItem(Long idItem) {
        return itemRepository.findById(idItem).get().getSaldo();
    }

    @Override
    public Boolean saldoItemIsValid(BigDecimal saldoItem, BigDecimal qtdeVerificar) {
        if (saldoItem.compareTo(new BigDecimal(0)) <= 0) {
            throw new RuntimeException("Saldo menor ou igual a 0");
        } else if (saldoItem.compareTo(qtdeVerificar) < 0) {
            throw new RuntimeException("Saldo menor que a quantidade informada");
        } else {
            return true;
        }
    }
}
