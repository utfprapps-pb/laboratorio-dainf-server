package br.com.utfpr.gerenciamento.server.factory;

import br.com.utfpr.gerenciamento.server.dto.FornecedorResponseDto;
import br.com.utfpr.gerenciamento.server.model.Fornecedor;

/**
 * Factory para criação de objetos de teste relacionados a Fornecedor. Elimina código duplicado e
 * padroniza a criação de objetos de teste.
 */
public class FornecedorFactory {

  public static Fornecedor createFornecedorPadrao() {
    Fornecedor fornecedor = new Fornecedor();
    fornecedor.setId(1L);
    fornecedor.setNomeFantasia("Fornecedor Teste");
    fornecedor.setRazaoSocial("Razão Social Teste LTDA");
    fornecedor.setCnpj("12345678901234");
    fornecedor.setEmail("contato@fornecedor.com");
    fornecedor.setTelefone("1122334455");
    return fornecedor;
  }

  public static Fornecedor createFornecedor(Long id, String nomeFantasia) {
    Fornecedor fornecedor = new Fornecedor();
    fornecedor.setId(id);
    fornecedor.setNomeFantasia(nomeFantasia);
    fornecedor.setRazaoSocial("Razão Social " + nomeFantasia + " LTDA");
    fornecedor.setCnpj("1234567890123" + id);
    fornecedor.setEmail("contato@" + nomeFantasia.toLowerCase().replace(" ", "") + ".com");
    fornecedor.setTelefone("11223344" + id);
    return fornecedor;
  }

  public static FornecedorResponseDto createFornecedorResponseDtoPadrao() {
    FornecedorResponseDto dto = new FornecedorResponseDto();
    dto.setId(1L);
    dto.setNomeFantasia("Fornecedor Teste");
    dto.setRazaoSocial("Razão Social Teste LTDA");
    dto.setCnpj("12345678901234");
    dto.setEmail("contato@fornecedor.com");
    dto.setTelefone("1122334455");
    return dto;
  }

  public static FornecedorResponseDto createFornecedorResponseDto(Long id, String nomeFantasia) {
    FornecedorResponseDto dto = new FornecedorResponseDto();
    dto.setId(id);
    dto.setNomeFantasia(nomeFantasia);
    dto.setRazaoSocial("Razão Social " + nomeFantasia + " LTDA");
    dto.setCnpj("1234567890123" + id);
    dto.setEmail("contato@" + nomeFantasia.toLowerCase().replace(" ", "") + ".com");
    dto.setTelefone("11223344" + id);
    return dto;
  }

  public static java.util.List<Fornecedor> createListaFornecedores(int quantidade) {
    return java.util.stream.IntStream.range(0, quantidade)
        .mapToObj(i -> createFornecedor((long) (i + 1), "Fornecedor " + (i + 1)))
        .toList();
  }

  public static java.util.List<FornecedorResponseDto> createListaFornecedoresDto(int quantidade) {
    return java.util.stream.IntStream.range(0, quantidade)
        .mapToObj(i -> createFornecedorResponseDto((long) (i + 1), "Fornecedor " + (i + 1)))
        .toList();
  }
}
