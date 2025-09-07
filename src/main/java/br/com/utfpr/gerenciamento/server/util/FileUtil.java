package br.com.utfpr.gerenciamento.server.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import org.springframework.util.StringUtils;
import br.com.utfpr.gerenciamento.server.exception.ArquivoException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileUtil {

  private FileUtil() {
    throw new IllegalStateException("Classe Utilitária");
  }

  private static final Pattern CARACTERES_INSEGUROS = Pattern.compile("[\\\\/:*?\"<>|\\x00-\\x1F]");
  private static final Path RAIZ_PROJETO = Paths.get(System.getProperty("user.dir")).normalize();

  static final String MSG_NOME_VAZIO = "Nome do arquivo não pode ser vazio ou nulo";
  static final String MSG_NOME_INVALIDO_PREFIX = "Nome de arquivo inválido: ";
  static final String MSG_ACESSO_INSEGURO_PREFIX = "Tentativa de acesso inseguro ao arquivo: ";
  static final String MSG_ERRO_CRIAR_DIRETORIO_PREFIX = "Não foi possível criar o diretório: ";

  /**
   * Retorna o caminho absoluto da raiz do projeto. Mantido apenas para retrocompatibilidade com
   * código legado.
   *
   * @return caminho absoluto da raiz do projeto
   * @deprecated Use getSecurePath() ou os metodos específicos ao invés deste
   */
  @Deprecated(since = "31/08/2025", forRemoval = false)
  public static String getAbsolutePathRaiz() {
    return RAIZ_PROJETO.toString();
  }

  /**
   * Cria um caminho seguro num subdiretório especificado, realizando sanitização do nome do arquivo
   * e validação contra path traversal.
   *
   * @param subdirectory nome do subdiretório onde o arquivo será armazenado
   * @param fileName nome do arquivo a ser sanitizado e validado
   * @return Path seguro e validado para o arquivo
   * @throws ArquivoException se detectar tentativa de path traversal ou nome inválido
   * @throws RuntimeException se não conseguir criar o diretório de destino
   */
  public static Path getSecurePath(String subdirectory, String fileName) {
    if (!StringUtils.hasText(fileName)) {
      throw new ArquivoException(MSG_NOME_VAZIO);
    }

    if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
      log.warn("Tentativa de path traversal detectada para arquivo: {}", fileName);
      throw new ArquivoException(MSG_ACESSO_INSEGURO_PREFIX + fileName);
    }

    String sanitized = sanitizeFileName(fileName);

    Path targetDir = RAIZ_PROJETO.resolve(subdirectory).normalize();
    Path fullPath = targetDir.resolve(sanitized).normalize();

    if (!fullPath.startsWith(targetDir)) {
      log.warn("Caminho final fora do diretório permitido: {}", fullPath);
      throw new ArquivoException(MSG_ACESSO_INSEGURO_PREFIX + fileName);
    }

    try {
      Files.createDirectories(targetDir);
    } catch (IOException e) {
      log.error("Erro ao criar diretório: {}", subdirectory, e);
      throw new ArquivoException(MSG_ERRO_CRIAR_DIRETORIO_PREFIX + subdirectory, e);
    }

    return fullPath;
  }

  /**
   * Cria um caminho seguro para arquivos de relatórios. Os arquivos serão armazenados no
   * subdiretório 'reports' da raiz do projeto.
   *
   * @param fileName nome do arquivo de relatório
   * @return Path seguro para o arquivo de relatório
   * @throws ArquivoException se o nome do arquivo for inseguro
   */
  public static Path getSecureReportPath(String fileName) {
    return getSecurePath("reports", fileName);
  }

  /**
   * Cria um caminho seguro para arquivos de imagem. Os arquivos serão armazenados no subdiretório
   * 'images' da raiz do projeto.
   *
   * @param fileName nome do arquivo de imagem
   * @return Path seguro para o arquivo de imagem
   * @throws ArquivoException se o nome do arquivo for inseguro
   */
  public static Path getSecureImagePath(String fileName) {
    return getSecurePath("images", fileName);
  }

  /**
   * Sanitiza um nome de arquivo removendo caracteres perigosos e sequências que poderiam ser usadas
   * para path traversal.
   *
   * @param fileName nome do arquivo a ser sanitizado
   * @return nome do arquivo sanitizado e seguro
   * @throws ArquivoException se o nome do arquivo for vazio, nulo ou inválido
   */
  public static String sanitizeFileName(String fileName) {
    if (!StringUtils.hasText(fileName)) {
      throw new ArquivoException(MSG_NOME_VAZIO);
    }

    String clean = fileName.replaceAll("\\.{2,}", "").replaceAll("[/\\\\]", "").trim();
    clean = CARACTERES_INSEGUROS.matcher(clean).replaceAll("_");

    if (clean.isEmpty() || ".".equals(clean) || "..".equals(clean)) {
      log.warn("Nome de arquivo inválido após sanitização: {}", fileName);
      throw new ArquivoException(MSG_NOME_INVALIDO_PREFIX + fileName);
    }

    return clean;
  }

  /**
   * Verifica se um arquivo possui uma extensão válida entre as permitidas. A comparação é realizada
   * de forma case-insensitive.
   *
   * @param fileName nome do arquivo a ser verificado
   * @param allowed array de extensões permitidas (sem o ponto)
   * @return true se a extensão for válida, false caso contrário
   */
  public static boolean hasValidExtension(String fileName, String... allowed) {
    if (!StringUtils.hasText(fileName)) return false;
    String lower = fileName.toLowerCase();
    return java.util.Arrays.stream(allowed)
        .anyMatch(ext -> lower.endsWith("." + ext.toLowerCase()));
  }
}
