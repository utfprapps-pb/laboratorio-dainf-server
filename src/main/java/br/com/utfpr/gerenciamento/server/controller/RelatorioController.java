package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.model.Relatorio;
import br.com.utfpr.gerenciamento.server.model.RelatorioParamsValue;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import br.com.utfpr.gerenciamento.server.service.RelatorioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("relatorio")
public class RelatorioController extends CrudController<Relatorio, Long> {

    @Autowired
    private RelatorioService relatorioService;

    @Override
    protected CrudService<Relatorio, Long> getService() {
        return this.relatorioService;
    }

    @PostMapping("upload-file-report")
    public void upload(@RequestParam("idRelatorio") Long idItem,
                       MultipartHttpServletRequest file,
                       HttpServletRequest request) {
        try {
            if (file.getFile("anexo") != null) {
                relatorioService.saveFileReport(file, request, idItem);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostMapping("generate-report")
    public byte[] generateReport(@RequestBody Map<String, Object> params) {
        ObjectMapper mapper = new ObjectMapper();
        Long idRelatorio = mapper.convertValue(params.get("idRel"), Long.class);
        List<RelatorioParamsValue> paramsRel = null;
        if (params.get("params") != null) {
            paramsRel = Arrays.asList(mapper.convertValue(params.get("params"), RelatorioParamsValue[].class));
        }

        try {
            JasperPrint jasperPrint = relatorioService.generateReport(idRelatorio, paramsRel);
            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void postDelete(Relatorio object) {
        relatorioService.deleteFileReport(object.getNameReport());
    }
}
