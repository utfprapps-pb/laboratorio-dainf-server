package br.com.utfpr.gerenciamento.server.minio.util;

import cn.hutool.core.io.FileTypeUtil;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
public class FileTypeUtils {

  private static final String IMAGE_TYPE = "image/";
  private static final String AUDIO_TYPE = "audio/";
  private static final String VIDEO_TYPE = "video/";
  private static final String APPLICATION_TYPE = "application/";
  private static final String TXT_TYPE = "text/";

  public static String getFileType(MultipartFile multipartFile) {

    InputStream inputStream;
    String type;

    try {
      inputStream = multipartFile.getInputStream();
      type = FileTypeUtil.getType(inputStream);

      if (type.equalsIgnoreCase("JPG")
          || type.equalsIgnoreCase("JPEG")
          || type.equalsIgnoreCase("GIF")
          || type.equalsIgnoreCase("PNG")
          || type.equalsIgnoreCase("BMP")
          || type.equalsIgnoreCase("PCX")
          || type.equalsIgnoreCase("TGA")
          || type.equalsIgnoreCase("PSD")
          || type.equalsIgnoreCase("TIFF")
          || type.equalsIgnoreCase("WEBP")) {

        return IMAGE_TYPE + type;
      }

      if (type.equalsIgnoreCase("mp3")
          || type.equalsIgnoreCase("OGG")
          || type.equalsIgnoreCase("WAV")
          || type.equalsIgnoreCase("REAL")
          || type.equalsIgnoreCase("APE")
          || type.equalsIgnoreCase("MODULE")
          || type.equalsIgnoreCase("MIDI")
          || type.equalsIgnoreCase("VQF")
          || type.equalsIgnoreCase("CD")) {

        return AUDIO_TYPE + type;
      }
      if (type.equalsIgnoreCase("mp4")
          || type.equalsIgnoreCase("avi")
          || type.equalsIgnoreCase("MPEG-1")
          || type.equalsIgnoreCase("RM")
          || type.equalsIgnoreCase("ASF")
          || type.equalsIgnoreCase("WMV")
          || type.equalsIgnoreCase("qlv")
          || type.equalsIgnoreCase("MPEG-2")
          || type.equalsIgnoreCase("MPEG4")
          || type.equalsIgnoreCase("mov")
          || type.equalsIgnoreCase("3gp")) {

        return VIDEO_TYPE + type;
      }
      if (type.equalsIgnoreCase("doc")
          || type.equalsIgnoreCase("docx")
          || type.equalsIgnoreCase("ppt")
          || type.equalsIgnoreCase("pptx")
          || type.equalsIgnoreCase("xls")
          || type.equalsIgnoreCase("xlsx")
          || type.equalsIgnoreCase("zip")
          || type.equalsIgnoreCase("jar")) {

        return APPLICATION_TYPE + type;
      }
      if (type.equalsIgnoreCase("txt")) {

        return TXT_TYPE + type;
      }

    } catch (IOException e) {
      log.error(e.getMessage());
    }

    return null;
  }
}
