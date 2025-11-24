package br.com.utfpr.gerenciamento.server.util;

import static org.junit.jupiter.api.Assertions.*;

import br.com.utfpr.gerenciamento.server.exception.ArquivoException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class FileUtilTest {

  @Test
  void sanitizeFileName_DeveRemoverCaracteresInseguros() {
    // Given
    String nomeInseguro = "arquivo<>*?.txt";

    // When
    String resultado = FileUtil.sanitizeFileName(nomeInseguro);

    // Then
    assertEquals("arquivo____.txt", resultado);
  }

  @Test
  void sanitizeFileName_DeveRemoverPathTraversal() {
    // Given
    String nomeComPathTraversal = "../../etc/passwd";

    // When
    String resultado = FileUtil.sanitizeFileName(nomeComPathTraversal);

    // Then
    assertEquals("etcpasswd", resultado);
  }

  @Test
  void sanitizeFileName_DeveLancarExcecaoParaNomeVazio() {
    // Given
    String nomeVazio = "";

    // When & Then
    ArquivoException exception =
        assertThrows(ArquivoException.class, () -> FileUtil.sanitizeFileName(nomeVazio));
    assertEquals(FileUtil.MSG_NOME_VAZIO, exception.getMessage());
  }

  @Test
  void sanitizeFileName_DeveLancarExcecaoParaNomeNull() {
    // Given
    String nomeNull = null;

    // When & Then
    ArquivoException exception =
        assertThrows(ArquivoException.class, () -> FileUtil.sanitizeFileName(nomeNull));
    assertEquals(FileUtil.MSG_NOME_VAZIO, exception.getMessage());
  }

  @Test
  void sanitizeFileName_DeveLancarExcecaoParaNomeInvalido() {
    // Given
    String nomeInvalido = "..";

    // When & Then
    ArquivoException exception =
        assertThrows(ArquivoException.class, () -> FileUtil.sanitizeFileName(nomeInvalido));
    assertEquals(FileUtil.MSG_NOME_INVALIDO_PREFIX + nomeInvalido, exception.getMessage());
  }

  @Test
  void sanitizeFileName_DeveLancarExcecaoParaNomeApenasPontos() {
    // Given
    String nomeApenasPontos = ".";

    // When & Then
    ArquivoException exception =
        assertThrows(ArquivoException.class, () -> FileUtil.sanitizeFileName(nomeApenasPontos));
    assertEquals(FileUtil.MSG_NOME_INVALIDO_PREFIX + nomeApenasPontos, exception.getMessage());
  }

  @Test
  void hasValidExtension_DeveRetornarTrueParaExtensaoValida() {
    // Given
    String arquivo = "relatorio.jrxml";

    // When
    boolean resultado = FileUtil.hasValidExtension(arquivo, "jrxml", "pdf");

    // Then
    assertTrue(resultado);
  }

  @Test
  void hasValidExtension_DeveRetornarFalseParaExtensaoInvalida() {
    // Given
    String arquivo = "script.sh";

    // When
    boolean resultado = FileUtil.hasValidExtension(arquivo, "jrxml", "pdf");

    // Then
    assertFalse(resultado);
  }

  @Test
  void hasValidExtension_DeveSerCaseInsensitive() {
    // Given
    String arquivo = "RELATORIO.JRXML";

    // When
    boolean resultado = FileUtil.hasValidExtension(arquivo, "jrxml");

    // Then
    assertTrue(resultado);
  }

  @Test
  void hasValidExtension_DeveRetornarFalseParaNomeVazio() {
    // Given
    String arquivoVazio = "";

    // When
    boolean resultado = FileUtil.hasValidExtension(arquivoVazio, "jrxml");

    // Then
    assertFalse(resultado);
  }

  @Test
  void hasValidExtension_DeveRetornarFalseParaNomeNull() {
    // Given
    String arquivoNull = null;

    // When
    boolean resultado = FileUtil.hasValidExtension(arquivoNull, "jrxml");

    // Then
    assertFalse(resultado);
  }

  @Test
  void getAbsolutePathRaiz_DeveRetornarCaminhoValido() {
    // When
    String resultado = FileUtil.getAbsolutePathRaiz();

    // Then
    assertNotNull(resultado);
    assertFalse(resultado.isEmpty());
  }

  @Test
  void sanitizeFileName_DeveManterNomeValidoIntacto() {
    // Given
    String nomeValido = "arquivo_valido.txt";

    // When
    String resultado = FileUtil.sanitizeFileName(nomeValido);

    // Then
    assertEquals(nomeValido, resultado);
  }

  @Test
  void sanitizeFileName_DeveRemoverEspacosExcessivos() {
    // Given
    String nomeComEspacos = "  arquivo  com  espacos  .txt  ";

    // When
    String resultado = FileUtil.sanitizeFileName(nomeComEspacos);

    // Then
    assertEquals("arquivo  com  espacos  .txt", resultado);
  }

  @Test
  void getSecureReportPath_DeveCriarCaminhoSeguro() {
    // Given
    String nomeArquivo = "relatorio.jrxml";

    // When
    Path resultado = FileUtil.getSecureReportPath(nomeArquivo);

    // Then
    assertNotNull(resultado);
    assertTrue(resultado.toString().contains("reports"));
    assertTrue(resultado.toString().endsWith("relatorio.jrxml"));
  }

  @Test
  void getSecurePath_DeveCriarCaminhoComSubdiretorioCorreto() {
    // Given
    String subdir = "test";
    String nomeArquivo = "arquivo.txt";

    // When
    Path resultado = FileUtil.getSecurePath(subdir, nomeArquivo);

    // Then
    assertNotNull(resultado);
    assertTrue(resultado.toString().contains("test"));
    assertTrue(resultado.toString().endsWith("arquivo.txt"));
  }

  @Test
  void getSecurePath_DeveLancarExcecaoParaPathTraversal() {
    // Given
    String nomePerigoso = "../../../etc/passwd";

    // When & Then
    ArquivoException exception =
        assertThrows(ArquivoException.class, () -> FileUtil.getSecurePath("reports", nomePerigoso));
    assertEquals(FileUtil.MSG_ACESSO_INSEGURO_PREFIX + nomePerigoso, exception.getMessage());
  }

  @Test
  void getSecureReportPath_DeveLancarExcecaoParaPathTraversal() {
    // Given
    String nomePerigoso = "../../config.properties";

    // When & Then
    ArquivoException exception =
        assertThrows(ArquivoException.class, () -> FileUtil.getSecureReportPath(nomePerigoso));
    assertEquals(FileUtil.MSG_ACESSO_INSEGURO_PREFIX + nomePerigoso, exception.getMessage());
  }

  @Test
  void getSecurePath_DeveLancarExcecaoParaBarraSimples() {
    // Given
    String nomeComBarra = "pasta/arquivo.txt";

    // When & Then
    ArquivoException exception =
        assertThrows(ArquivoException.class, () -> FileUtil.getSecurePath("reports", nomeComBarra));
    assertEquals(FileUtil.MSG_ACESSO_INSEGURO_PREFIX + nomeComBarra, exception.getMessage());
  }

  @Test
  void getSecurePath_DeveLancarExcecaoParaBarraInvertida() {
    // Given
    String nomeComBarraInvertida = "pasta\\arquivo.txt";

    // When & Then
    ArquivoException exception =
        assertThrows(
            ArquivoException.class, () -> FileUtil.getSecurePath("reports", nomeComBarraInvertida));
    assertEquals(
        FileUtil.MSG_ACESSO_INSEGURO_PREFIX + nomeComBarraInvertida, exception.getMessage());
  }
}
