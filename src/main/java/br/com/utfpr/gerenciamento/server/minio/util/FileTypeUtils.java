package br.com.utfpr.gerenciamento.server.minio.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

/**
 * Utilitário para detecção de tipo MIME de arquivos usando Apache Tika.
 *
 * <p>Modernizado com try-with-resources aprimorado (Java 21).
 */
@Slf4j
public final class FileTypeUtils {

  private static final Tika TIKA = new Tika();

  private FileTypeUtils() {
    // Utility class - previne instanciação
  }

  /**
   * Detecta o tipo MIME de um arquivo usando análise de conteúdo (não apenas extensão).
   *
   * @param multipartFile arquivo a ser analisado
   * @return tipo MIME detectado ou null em caso de erro
   */
  public static String getFileType(MultipartFile multipartFile) {
    try (var inputStream = new BufferedInputStream(multipartFile.getInputStream())) {
      return TIKA.detect(inputStream);
    } catch (IOException e) {
      log.error("Erro ao detectar tipo do arquivo: {}", e.getMessage());
      return null;
    }
  }
}
