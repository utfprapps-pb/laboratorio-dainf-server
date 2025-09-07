package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.ItemImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ItemImageRepository extends JpaRepository<ItemImage, Long>, JpaSpecificationExecutor<ItemImage> {
  List<ItemImage> findItemImageByNameImage(String imageName);
}
