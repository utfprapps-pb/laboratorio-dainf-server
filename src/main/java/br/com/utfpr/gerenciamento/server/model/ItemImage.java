package br.com.utfpr.gerenciamento.server.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@Table(name = "item_image")
public class ItemImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "caminho_image")
    private String caminhoImage;

    @Column(name = "name_image")
    private String nameImage;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "item_id", referencedColumnName = "id")
    private Item item;
}
