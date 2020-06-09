package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.model.Relatorio;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import br.com.utfpr.gerenciamento.server.service.RelatorioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

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
            relatorioService.saveFileReport(file, request, idItem);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
