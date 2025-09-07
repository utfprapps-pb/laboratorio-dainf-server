package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.model.ItemImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

<<<<<<< Updated upstream
import java.util.List;

public interface ItemImageRepository extends JpaRepository<ItemImage, Long> {
    List<ItemImage> findItemImageByNameImage(String imageName);

=======
public interface ItemImageRepository extends JpaRepository<ItemImage, Long>, JpaSpecificationExecutor<ItemImage> {
  List<ItemImage> findItemImageByNameImage(String imageName);
>>>>>>> Stashed changes
}
