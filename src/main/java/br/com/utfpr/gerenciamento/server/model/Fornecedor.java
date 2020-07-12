package br.com.utfpr.gerenciamento.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@Table(name = "fornecedor")
public class Fornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotEmpty(message = "O campo 'Razão Social' é de preenchimento obrigatório.")
    @Column(name = "razao_social", length = 80, nullable = false)
    private String razaoSocial;

    @NotEmpty(message = "O campo 'Nome Fantasia' é de preenchimento obrigatório.")
    @Column(name = "nome_fantasia", length = 80, nullable = false)
    private String nomeFantasia;

    @NotEmpty(message = "O campo 'CNPJ' é de preenchimento obrigatório.")
    @Column(name = "cnpj", length = 14, nullable = false)
    private String cnpj;

    @NotEmpty(message = "O campo 'Inscrição Estadual' é de preenchimento obrigatório.")
    @Column(name = "ie", length = 14, nullable = false)
    private String ie;

    @Column(name = "endereco", length = 100)
    private String endereco;

    @Column(name = "observacao", length = 2000)
    private String observacao;

    @Column(name = "email")
    private String email;

    @Column(name = "telefone", length = 15)
    private String telefone;

    @NotNull(message = "O campo 'Cidade' deve ser selecionado.")
    @ManyToOne
    @JoinColumn(name = "cidade_id", referencedColumnName = "id")
    private Cidade cidade;

    @NotNull(message = "O campo 'Estado' deve ser selecionado.")
    @ManyToOne
    @JoinColumn(name = "estado_id", referencedColumnName = "id")
    private Estado estado;
}
