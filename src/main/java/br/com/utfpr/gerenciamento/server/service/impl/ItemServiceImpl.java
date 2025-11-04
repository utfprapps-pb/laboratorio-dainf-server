package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.dto.ItemResponseDto;
import br.com.utfpr.gerenciamento.server.enumeration.TipoItem;
import br.com.utfpr.gerenciamento.server.event.item.EstoqueMinNotificacaoEvent;
import br.com.utfpr.gerenciamento.server.exception.EntityNotFoundException;
import br.com.utfpr.gerenciamento.server.exception.SaldoInsuficienteException;
import br.com.utfpr.gerenciamento.server.minio.config.MinioConfig;
import br.com.utfpr.gerenciamento.server.minio.payload.FileResponse;
import br.com.utfpr.gerenciamento.server.minio.service.MinioService;
import br.com.utfpr.gerenciamento.server.minio.util.FileTypeUtils;
import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.model.ItemImage;
import br.com.utfpr.gerenciamento.server.repository.ItemImageRepository;
import br.com.utfpr.gerenciamento.server.repository.ItemRepository;
import br.com.utfpr.gerenciamento.server.repository.projection.ItemWithQtdeEmprestada;
import br.com.utfpr.gerenciamento.server.service.ItemService;
import br.com.utfpr.gerenciamento.server.util.FileUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Service
@Slf4j
public class ItemServiceImpl extends CrudServiceImpl<Item, Long, ItemResponseDto>
    implements ItemService {
  public static final String ITEM_NAO_ENCONTRADO_COM_ID = "Item não encontrado com ID: ";

  /**
   * Endereço(s) de email para notificações administrativas.
   *
   * <p>Configurável via propriedade {@code app.email.admin}, com fallback para
   * dainf.labs@gmail.com.
   */
  @Value("${app.email.admin:dainf.labs@gmail.com}")
  private String adminEmail;

  private final ItemRepository itemRepository;
  private final MinioService minioService;
  private final MinioConfig minioConfig;
  private final ItemImageRepository itemImageRepository;
  private final ApplicationEventPublisher eventPublisher;

  private final ModelMapper modelMapper;

  private final ItemService self;

  public ItemServiceImpl(
      ItemRepository itemRepository,
      MinioService minioService,
      MinioConfig minioConfig,
      ItemImageRepository itemImageRepository,
      ApplicationEventPublisher eventPublisher,
      ModelMapper modelMapper,
      @Lazy ItemService self) {
    this.itemRepository = itemRepository;
    this.minioService = minioService;
    this.minioConfig = minioConfig;
    this.itemImageRepository = itemImageRepository;
    this.eventPublisher = eventPublisher;
    this.modelMapper = modelMapper;
    this.self = self;
  }

  @Override
  protected JpaRepository<Item, Long> getRepository() {
    return itemRepository;
  }

  @Override
  public ItemResponseDto toDto(Item entity) {
    return modelMapper.map(entity, ItemResponseDto.class);
  }

  @Override
  public Item toEntity(ItemResponseDto itemResponseDto) {
    return modelMapper.map(itemResponseDto, Item.class);
  }

  @Override
  @Transactional
  public List<ItemResponseDto> itemComplete(String query, boolean hasEstoque) {
    BigDecimal zero = new BigDecimal(0);
    if ("".equalsIgnoreCase(query)) {
      if (hasEstoque)
        return itemRepository.findAllBySaldoIsGreaterThanOrderByNome(zero).stream()
            .map(this::convertToDto)
            .toList();
      else return itemRepository.findAllByOrderByNome().stream().map(this::convertToDto).toList();
    } else {
      if (hasEstoque)
        return itemRepository
            .findByNomeLikeIgnoreCaseAndSaldoIsGreaterThanOrderByNome("%" + query + "%", zero)
            .stream()
            .map(this::convertToDto)
            .toList();
      else
        return itemRepository.findByNomeLikeIgnoreCaseOrderByNome("%" + query + "%").stream()
            .map(this::convertToDto)
            .toList();
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<ItemResponseDto> findByGrupo(Long id) {
    return itemRepository.findByGrupoIdOrderByNome(id).stream().map(this::convertToDto).toList();
  }

  @Override
  @Transactional
  public void diminuiSaldoItem(Long idItem, BigDecimal qtde, boolean needValidationSaldo) {
    Item itemToSave =
        itemRepository
            .findById(idItem)
            .orElseThrow(() -> new EntityNotFoundException(ITEM_NAO_ENCONTRADO_COM_ID + idItem));
    if (!needValidationSaldo
        || Boolean.TRUE.equals(this.saldoItemIsValid(itemToSave.getSaldo(), qtde))) {
      itemToSave.setSaldo(itemToSave.getSaldo().subtract(qtde));
      itemRepository.save(itemToSave);
    }
  }

  @Override
  @Transactional
  public void aumentaSaldoItem(Long idItem, BigDecimal qtde) {
    Item itemToSave =
        itemRepository
            .findById(idItem)
            .orElseThrow(() -> new EntityNotFoundException(ITEM_NAO_ENCONTRADO_COM_ID + idItem));
    itemToSave.setSaldo(itemToSave.getSaldo().add(qtde));
    itemRepository.save(itemToSave);
  }

  @Override
  @Transactional(readOnly = true)
  public BigDecimal getSaldoItem(Long idItem) {
    return itemRepository
        .findById(idItem)
        .orElseThrow(() -> new EntityNotFoundException(ITEM_NAO_ENCONTRADO_COM_ID + idItem))
        .getSaldo();
  }

  @Override
  public Boolean saldoItemIsValid(BigDecimal saldoItem, BigDecimal qtdeVerificar) {
    if (saldoItem.compareTo(new BigDecimal(0)) <= 0) {
      throw new SaldoInsuficienteException("Saldo menor ou igual a 0");
    } else if (saldoItem.compareTo(qtdeVerificar) < 0) {
      throw new SaldoInsuficienteException("Saldo menor que a quantidade informada");
    } else {
      return true;
    }
  }

  @Override
  @Transactional
  public void saveImages(
      MultipartHttpServletRequest files, HttpServletRequest request, Long idItem) {
    Item item = toEntity(self.findOne(idItem));
    var anexos = files.getFiles("anexos[]");
    List<ItemImage> list = new ArrayList<>();
    for (MultipartFile anexo : anexos) {
      String fileType = FileTypeUtils.getFileType(anexo);
      if (fileType != null) {
        FileResponse fileResponse =
            minioService.putObject(anexo, minioConfig.getBucketName(), fileType);
        ItemImage image = new ItemImage();
        image.setContentType(fileResponse.getContentType());
        image.setNameImage(fileResponse.getFilename());
        image.setItem(item);
        list.add(image);
      }
    }
    item.getImageItem().addAll(list);
    self.save(item);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ItemImage> getImagesItem(Long idItem) {
    return self.findOne(idItem).getImageItem();
  }

  @Override
  @Transactional
  public void deleteImage(ItemImage image, Long idItem) {
    if (itemImageRepository.findItemImageByNameImage(image.getNameImage()).size() == 1) {
      try {
        minioService.removeObject(
            minioConfig.getBucketName(), FileUtil.sanitizeFileName(image.getNameImage()));
      } catch (Exception ex) {
        log.error("Erro ao remover imagem do MinIO: {}", ex.getMessage());
      }
    }
    Item i = toEntity(self.findOne(idItem));
    i.getImageItem().removeIf(itemImage -> itemImage.getId().equals(image.getId()));
    self.save(i);
  }

  /**
   * Publica evento de notificação de estoque mínimo se houver itens abaixo do limite.
   *
   * <p>Este método verifica se existem itens que atingiram o estoque mínimo e, caso positivo,
   * publica um evento que será processado de forma assíncrona pelo {@code EmailEventListener} APÓS
   * o commit da transação.
   *
   * <p><b>Padrão Event-Driven:</b>
   *
   * <ul>
   *   <li>✅ Desacoplamento: Service não conhece lógica de email
   *   <li>✅ Transacional: Email enviado apenas após commit com sucesso
   *   <li>✅ Assíncrono: Não bloqueia thread principal (@Async no listener)
   *   <li>✅ Resiliente: Retry automático com exponential backoff (2s, 4s, 8s)
   *   <li>✅ Seguro: Falhas de email não afetam negócio
   * </ul>
   *
   * @see EstoqueMinNotificacaoEvent
   * @see br.com.utfpr.gerenciamento.server.event.email.EmailEventListener
   */
  @Override
  @Transactional
  public void sendNotificationItensAtingiramQtdeMin() {
    // Verifica se existem itens abaixo do estoque mínimo
    if (itemRepository.countAllByQtdeMinimaIsLessThanSaldo() > 0) {
      log.info("Publicando evento de notificação de estoque mínimo");
      eventPublisher.publishEvent(new EstoqueMinNotificacaoEvent(this, adminEmail));
    } else {
      log.debug("Nenhum item abaixo do estoque mínimo - notificação não enviada");
    }
  }

  /**
   * This method is used when an item is duplicated, so the image array can also be transfered to
   * the new item
   *
   * @param itemImages
   * @param id
   */
  @Override
  @Transactional
  public void copyImagesItem(List<ItemImage> itemImages, Long id) {
    var item = toEntity(self.findOne(id));
    List<ItemImage> toReturn = new ArrayList<>();
    itemImages.forEach(
        itemImage -> {
          ItemImage image = new ItemImage();
          image.setContentType(itemImage.getContentType());
          image.setNameImage(itemImage.getNameImage());
          image.setItem(item);
          toReturn.add(image);
        });
    item.setImageItem(toReturn);
    self.save(item);
  }

  @Override
  @Transactional(readOnly = true)
  public Item findOneWithDisponibilidade(Long id) {
    // Busca item com quantidade emprestada via agregação SQL (Spring Data JPA projection)
    ItemWithQtdeEmprestada projection =
        itemRepository
            .findByIdWithQtdeEmprestada(id)
            .orElseThrow(() -> new EntityNotFoundException(ITEM_NAO_ENCONTRADO_COM_ID + id));

    Item item = projection.getItem();
    BigDecimal qtdeEmprestada = projection.getQtdeEmprestada();

    // SEMPRE setar quantidade emprestada (para exibir no frontend)
    item.setQuantidadeEmprestada(qtdeEmprestada != null ? qtdeEmprestada : BigDecimal.ZERO);

    // RN-002: Disponível apenas para TipoItem.P (Permanente)
    if (item.getTipoItem() != TipoItem.P) {
      item.setDisponivelEmprestimoCalculado(null);
      return item;
    }

    // RN-001: Disponível = Saldo - Quantidade Emprestada
    BigDecimal saldo = item.getSaldo() != null ? item.getSaldo() : BigDecimal.ZERO;
    BigDecimal disponivel = saldo.subtract(item.getQuantidadeEmprestada());

    // RN-003: Nunca negativo (log warning para inconsistências)
    if (disponivel.compareTo(BigDecimal.ZERO) < 0) {
      log.warn(
          "Inconsistência detectada: Item {} (Saldo: {}, Emprestado: {}) resulta em disponibilidade negativa",
          item.getId(),
          saldo,
          item.getQuantidadeEmprestada());
      disponivel = BigDecimal.ZERO;
    }

    item.setDisponivelEmprestimoCalculado(disponivel);
    return item;
  }

  @Override
  public ItemResponseDto convertToDto(Item entity) {
    return modelMapper.map(entity, ItemResponseDto.class);
  }
}
