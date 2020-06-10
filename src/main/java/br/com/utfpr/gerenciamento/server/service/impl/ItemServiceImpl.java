package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.model.ItemImage;
import br.com.utfpr.gerenciamento.server.repository.ItemImageRepository;
import br.com.utfpr.gerenciamento.server.repository.ItemRepository;
import br.com.utfpr.gerenciamento.server.service.ItemService;
import br.com.utfpr.gerenciamento.server.util.FileUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ItemServiceImpl extends CrudServiceImpl<Item, Long> implements ItemService {

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ItemImageRepository itemImageRepository;

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

    @Override
    public void saveImages(MultipartHttpServletRequest files,
                           HttpServletRequest request,
                           Long idItem) {
        Item item = this.findOne(idItem);
        var anexos = files.getFiles("anexos[]");

        File dir = new File(FileUtil.getAbsolutePathRaiz() + File.separator + "images-item");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        List<ItemImage> list = new ArrayList<>();
        for (MultipartFile anexo : anexos) {
            String extensao = anexo.getOriginalFilename().substring(
                    anexo.getOriginalFilename().lastIndexOf(".")
            );

            String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy'_'HHmm'_'SSSSSS"));
            String nomeArquivo = idItem + "_" + fileName + extensao;

            try {
                FileOutputStream fileOut = new FileOutputStream(
                        new File(dir + File.separator + nomeArquivo)
                );
                BufferedOutputStream stream = new BufferedOutputStream(fileOut);
                stream.write(anexo.getBytes());
                stream.close();
                fileOut.close();

                ItemImage image = new ItemImage();
                image.setCaminhoImage(dir.getAbsolutePath());
                image.setNameImage(nomeArquivo);
                image.setItem(item);
                list.add(image);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        item.getImageItem().addAll(list);
        this.save(item);
    }

    @Override
    public List<ItemImage> getImagesItem(Long idItem) {
        Item i = this.findOne(idItem);
        for (ItemImage image : i.getImageItem()) {
            try {
                image.setBase64(encodeFileToBase64Binary(image.getCaminhoImage() + File.separator + image.getNameImage()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return i.getImageItem();
    }

    @Override
    public void deleteImage(ItemImage image, Long idItem) {
        File file = new File(image.getCaminhoImage() + File.separator + image.getNameImage());
        if (file.exists()) {
            file.delete();
        }
        Item i = this.findOne(idItem);
        i.getImageItem().removeIf(itemImage -> itemImage.getId().equals(image.getId()));
        this.save(i);
    }

    private static String encodeFileToBase64Binary(String fileName) throws IOException {
        File file = new File(fileName);
        FileInputStream fi = new FileInputStream(file);
        byte[] encoded = Base64.encodeBase64(IOUtils.toByteArray(fi));
        fi.close();
        return new String(encoded, StandardCharsets.US_ASCII);
    }
}
