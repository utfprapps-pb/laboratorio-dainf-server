package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.model.Relatorio;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public interface RelatorioService extends CrudService<Relatorio, Long> {

    void saveFileReport(MultipartHttpServletRequest file, HttpServletRequest request, Long idRelatorio) throws IOException;
}
