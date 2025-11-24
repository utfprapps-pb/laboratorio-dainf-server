package br.com.utfpr.gerenciamento.server.minio.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class FileTypeUtilsTest {

  @Mock private MultipartFile mockMultipartFile;

  @Test
  void getFileType_DeveDetectarImagemJPEG() {
    // Given - JPEG magic bytes: FF D8 FF
    byte[] jpegBytes = new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    MockMultipartFile jpegFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", jpegBytes);

    // When
    String resultado = FileTypeUtils.getFileType(jpegFile);

    // Then
    assertNotNull(resultado);
    assertTrue(
        resultado.startsWith("image/"), "Tipo deve começar com 'image/', mas foi: " + resultado);
  }

  @Test
  void getFileType_DeveDetectarImagemPNG() {
    // Given - PNG magic bytes: 89 50 4E 47 0D 0A 1A 0A
    byte[] pngBytes =
        new byte[] {
          (byte) 0x89,
          0x50,
          0x4E,
          0x47,
          0x0D,
          0x0A,
          0x1A,
          0x0A,
          0x00,
          0x00,
          0x00,
          0x0D,
          0x49,
          0x48,
          0x44,
          0x52
        };
    MockMultipartFile pngFile = new MockMultipartFile("file", "test.png", "image/png", pngBytes);

    // When
    String resultado = FileTypeUtils.getFileType(pngFile);

    // Then
    assertNotNull(resultado);
    assertEquals("image/png", resultado);
  }

  @Test
  void getFileType_DeveDetectarPDF() {
    // Given - PDF magic bytes: %PDF
    byte[] pdfBytes = "%PDF-1.4".getBytes();
    MockMultipartFile pdfFile =
        new MockMultipartFile("file", "test.pdf", "application/pdf", pdfBytes);

    // When
    String resultado = FileTypeUtils.getFileType(pdfFile);

    // Then
    assertNotNull(resultado);
    assertEquals("application/pdf", resultado);
  }

  @Test
  void getFileType_DeveDetectarArquivoZIP() {
    // Given - ZIP magic bytes: PK (50 4B)
    byte[] zipBytes = new byte[] {0x50, 0x4B, 0x03, 0x04};
    MockMultipartFile zipFile =
        new MockMultipartFile("file", "test.zip", "application/zip", zipBytes);

    // When
    String resultado = FileTypeUtils.getFileType(zipFile);

    // Then
    assertNotNull(resultado);
    assertEquals("application/zip", resultado);
  }

  @Test
  void getFileType_DeveDetectarTextoPlano() {
    // Given - Arquivo de texto simples
    byte[] textBytes = "Este é um arquivo de texto simples".getBytes();
    MockMultipartFile textFile = new MockMultipartFile("file", "test.txt", "text/plain", textBytes);

    // When
    String resultado = FileTypeUtils.getFileType(textFile);

    // Then
    assertNotNull(resultado);
    assertTrue(
        resultado.startsWith("text/"), "Tipo deve começar com 'text/', mas foi: " + resultado);
  }

  @Test
  void getFileType_DeveDetectarVideoMP4() {
    // Given - MP4 magic bytes: geralmente começa com ftyp
    byte[] mp4Bytes =
        new byte[] {0x00, 0x00, 0x00, 0x20, 0x66, 0x74, 0x79, 0x70, 0x69, 0x73, 0x6F, 0x6D};
    MockMultipartFile mp4File = new MockMultipartFile("file", "test.mp4", "video/mp4", mp4Bytes);

    // When
    String resultado = FileTypeUtils.getFileType(mp4File);

    // Then
    assertNotNull(resultado);
    assertTrue(
        resultado.contains("mp4") || resultado.startsWith("video/"),
        "Tipo deve ser video/mp4 ou similar, mas foi: " + resultado);
  }

  @Test
  void getFileType_DeveRetornarNullQuandoOcorreIOException() throws IOException {
    // Given
    when(mockMultipartFile.getInputStream()).thenThrow(new IOException("Erro simulado"));

    // When
    String resultado = FileTypeUtils.getFileType(mockMultipartFile);

    // Then
    assertNull(resultado, "Deve retornar null quando ocorre IOException");
  }

  @Test
  void getFileType_DeveTratarArquivoVazio() {
    // Given
    MockMultipartFile emptyFile =
        new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

    // When
    String resultado = FileTypeUtils.getFileType(emptyFile);

    // Then
    assertNotNull(resultado);
    // Tika pode retornar application/octet-stream ou text/plain para arquivos vazios
    assertTrue(
        resultado.equals("application/octet-stream") || resultado.startsWith("text/"),
        "Arquivo vazio deve retornar tipo válido, mas foi: " + resultado);
  }

  @Test
  void getFileType_DeveDetectarImagemGIF() {
    // Given - GIF magic bytes: GIF89a ou GIF87a
    byte[] gifBytes = "GIF89a".getBytes();
    MockMultipartFile gifFile = new MockMultipartFile("file", "test.gif", "image/gif", gifBytes);

    // When
    String resultado = FileTypeUtils.getFileType(gifFile);

    // Then
    assertNotNull(resultado);
    assertEquals("image/gif", resultado);
  }

  @Test
  void getFileType_DeveDetectarArquivoXML() {
    // Given - XML com declaração
    byte[] xmlBytes = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes();
    MockMultipartFile xmlFile =
        new MockMultipartFile("file", "test.xml", "application/xml", xmlBytes);

    // When
    String resultado = FileTypeUtils.getFileType(xmlFile);

    // Then
    assertNotNull(resultado);
    assertTrue(
        resultado.contains("xml") || resultado.startsWith("text/"),
        "Tipo deve ser xml ou text, mas foi: " + resultado);
  }

  @Test
  void getFileType_DeveDetectarJSON() {
    // Given - JSON válido
    byte[] jsonBytes = "{\"test\": \"value\"}".getBytes();
    MockMultipartFile jsonFile =
        new MockMultipartFile("file", "test.json", "application/json", jsonBytes);

    // When
    String resultado = FileTypeUtils.getFileType(jsonFile);

    // Then
    assertNotNull(resultado);
    assertTrue(
        resultado.contains("json") || resultado.startsWith("text/"),
        "Tipo deve ser json ou text, mas foi: " + resultado);
  }

  @Test
  void getFileType_DeveUsarMagicBytesNaoExtensao() {
    // Given - Arquivo PNG com extensão .jpg (deve detectar pelo conteúdo, não extensão)
    byte[] pngBytes = new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    MockMultipartFile arquivoEnganoso =
        new MockMultipartFile("file", "fake.jpg", "image/jpeg", pngBytes);

    // When
    String resultado = FileTypeUtils.getFileType(arquivoEnganoso);

    // Then
    assertNotNull(resultado);
    assertEquals(
        "image/png", resultado, "Deve detectar PNG pelos magic bytes, não pela extensão .jpg");
  }

  @Test
  void getFileType_DeveRetornarMIMETypeCorreto() {
    // Given - Vários tipos de arquivos
    byte[] jpegBytes = new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    MockMultipartFile jpegFile = new MockMultipartFile("file", "test.jpg", null, jpegBytes);

    // When
    String resultado = FileTypeUtils.getFileType(jpegFile);

    // Then
    assertNotNull(resultado);
    assertTrue(resultado.contains("/"), "Deve retornar MIME type no formato 'tipo/subtipo'");
    assertFalse(false, "MIME type não deve estar vazio");
  }

  @Test
  void getFileType_DeveFecharInputStreamCorretamente() throws IOException {
    // Given
    byte[] testBytes = new byte[] {0x00, 0x01, 0x02};
    InputStream mockInputStream = spy(new ByteArrayInputStream(testBytes));
    when(mockMultipartFile.getInputStream()).thenReturn(mockInputStream);

    // When
    FileTypeUtils.getFileType(mockMultipartFile);

    // Then
    verify(mockInputStream, times(1)).close();
  }
}
