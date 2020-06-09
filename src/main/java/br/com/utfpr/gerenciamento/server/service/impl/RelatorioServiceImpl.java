package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.model.Relatorio;
import br.com.utfpr.gerenciamento.server.repository.RelatorioRepository;
import br.com.utfpr.gerenciamento.server.service.RelatorioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class RelatorioServiceImpl extends CrudServiceImpl<Relatorio, Long> implements RelatorioService {

    @Autowired
    private RelatorioRepository relatorioRepository;
    @Autowired
    private ApplicationContext applicationContext;

    @Override
    protected JpaRepository<Relatorio, Long> getRepository() {
        return this.relatorioRepository;
    }

    @Override
    public void saveFileReport(MultipartHttpServletRequest file,
                               HttpServletRequest request,
                               Long idRelatorio) throws IOException {
        Relatorio relatorio = relatorioRepository.getOne(idRelatorio);

        File dir = applicationContext.getResource("classpath:/static/report").getFile();
        var fileUpload = file.getFile("anexo");

        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = fileUpload.getOriginalFilename();

        try {
            FileOutputStream fileOut = new FileOutputStream(
                    new File(dir + File.separator + fileName)
            );
            BufferedOutputStream stream = new BufferedOutputStream(fileOut);
            stream.write(fileUpload.getBytes());
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        relatorio.setNameReport(fileName);
        this.save(relatorio);
    }
}
