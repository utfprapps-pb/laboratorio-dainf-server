package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.model.Relatorio;
import br.com.utfpr.gerenciamento.server.model.RelatorioParamsValue;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public interface RelatorioService extends CrudService<Relatorio, Long> {

    void saveFileReport(MultipartHttpServletRequest file, HttpServletRequest request, Long idRelatorio) throws IOException;

    JasperPrint generateReport(Long idRelatorio, List<RelatorioParamsValue> paramsRel) throws SQLException, JRException;

    void deleteFileReport(String nameRelatorio);
}
