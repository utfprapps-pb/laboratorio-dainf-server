package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.dto.RelatorioResponseDTO;
import br.com.utfpr.gerenciamento.server.model.Relatorio;
import br.com.utfpr.gerenciamento.server.model.RelatorioParamsValue;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface RelatorioService extends CrudService<Relatorio, Long, RelatorioResponseDTO> {

  void saveFileReport(
      MultipartHttpServletRequest file, HttpServletRequest request, Long idRelatorio)
      throws IOException;

  JasperPrint generateReport(Long idRelatorio, List<RelatorioParamsValue> paramsRel)
      throws SQLException, JRException;

  void deleteFileReport(String nameRelatorio);
}
