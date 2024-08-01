package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.minio.config.MinioConfig;
import br.com.utfpr.gerenciamento.server.minio.payload.FileResponse;
import br.com.utfpr.gerenciamento.server.minio.service.MinioService;
import br.com.utfpr.gerenciamento.server.minio.util.FileTypeUtils;
import br.com.utfpr.gerenciamento.server.model.Email;
import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.model.ItemImage;
import br.com.utfpr.gerenciamento.server.repository.ItemImageRepository;
import br.com.utfpr.gerenciamento.server.repository.ItemRepository;
import br.com.utfpr.gerenciamento.server.service.EmailService;
import br.com.utfpr.gerenciamento.server.service.ItemService;
import br.com.utfpr.gerenciamento.server.service.RelatorioService;
import net.sf.jasperreports.engine.JasperExportManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ItemServiceImpl extends CrudServiceImpl<Item, Long> implements ItemService {
    private final ItemRepository itemRepository;
    private final EmailService emailService;
    private final RelatorioService relatorioService;
    private final MinioService minioService;
    private final MinioConfig minioConfig;
    private final ItemImageRepository itemImageRepository;

    public ItemServiceImpl(ItemRepository itemRepository, EmailService emailService, RelatorioService relatorioService,
                           MinioService minioService, MinioConfig minioConfig, ItemImageRepository itemImageRepository) {
        this.itemRepository = itemRepository;
        this.emailService = emailService;
        this.relatorioService = relatorioService;
        this.minioService = minioService;
        this.minioConfig = minioConfig;
        this.itemImageRepository = itemImageRepository;
    }

    @Override
    protected JpaRepository<Item, Long> getRepository() {
        return itemRepository;
    }

    @Override
    public List<Item> itemComplete(String query, Boolean hasEstoque) {
        BigDecimal zero = new BigDecimal(0);
        if ("".equalsIgnoreCase(query)) {
            if (hasEstoque) return itemRepository.findAllBySaldoIsGreaterThanOrderByNome(zero);
            else return itemRepository.findAllByOrderByNome();
        } else {
            if (hasEstoque) {
                return itemRepository
                        .findByNomeLikeIgnoreCaseAndSaldoIsGreaterThanOrderByNome("%" + query + "%", zero);
            } else return itemRepository.findByNomeLikeIgnoreCaseOrderByNome("%" + query + "%");
        }
    }

    @Override
    public List<Item> findByGrupo(Long id) {
        return itemRepository.findByGrupoIdOrderByNome(id);
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
        List<ItemImage> list = new ArrayList<>();
        for (MultipartFile anexo : anexos) {
            String fileType = FileTypeUtils.getFileType(anexo);
            if (fileType != null) {
                FileResponse fileResponse = minioService.putObject(anexo, minioConfig.getBucketName(), fileType);
                ItemImage image = new ItemImage();
                image.setContentType(fileResponse.getContentType());
                image.setNameImage(fileResponse.getFilename());
                image.setItem(item);
                list.add(image);

            }
        }
        item.getImageItem().addAll(list);
        this.save(item);
    }

    @Override
    public List<ItemImage> getImagesItem(Long idItem) {
        return this.findOne(idItem).getImageItem();
    }

    @Override
    public void deleteImage(ItemImage image, Long idItem) {
        File file = new File(image.getContentType() + File.separator + image.getNameImage());
        if (file.exists()) {
            file.delete();
        }
        // Only remove the file if the image is associated to one item.
        if (itemImageRepository.findItemImageByNameImage(image.getNameImage()).size() == 1) {
            minioService.removeObject(minioConfig.getBucketName(), image.getNameImage());
        }
        Item i = this.findOne(idItem);
        i.getImageItem().removeIf(itemImage -> itemImage.getId().equals(image.getId()));
        this.save(i);
    }

    @Override
    public void sendNotificationItensAtingiramQtdeMin() {
        if (itemRepository.countAllByQtdeMinimaIsLessThanSaldo() > 0) {
            try {
                byte[] report = JasperExportManager.exportReportToPdf(
                        relatorioService.generateReport(6L, null)
                );
                Email email = Email.builder()
                        .para("dainf.labs@gmail.com")
                        .de("dainf.labs@gmail.com")
                        .titulo("Notificação: Itens que atingiram o estoque mínimo")
                        .conteudo(emailService.buildTemplateEmail(null, "templateNotificacaoEstoqueMinimo")).build();
                email.addFile("itensAtingiramEstoqueMin.pdf", report);
                emailService.enviar(email);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * This method is used when an item is duplicated, so the image array can also be transfered to the new item
     *
     * @param itemImages
     * @param id
     */
    @Override
    public void copyImagesItem(List<ItemImage> itemImages, Long id) {
        var item = this.findOne(id);
        List<ItemImage> toReturn = new ArrayList<>();
        itemImages.stream().forEach(itemImage -> {
            ItemImage image = new ItemImage();
            image.setContentType(itemImage.getContentType());
            image.setNameImage(itemImage.getNameImage());
            image.setItem(item);
            toReturn.add(image);
        });
        item.setImageItem(toReturn);
        this.save(item);
    }

}
