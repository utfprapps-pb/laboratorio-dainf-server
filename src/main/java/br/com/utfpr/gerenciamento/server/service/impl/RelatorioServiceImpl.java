package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.model.Relatorio;
import br.com.utfpr.gerenciamento.server.model.RelatorioParamsValue;
import br.com.utfpr.gerenciamento.server.repository.RelatorioRepository;
import br.com.utfpr.gerenciamento.server.service.RelatorioService;
import br.com.utfpr.gerenciamento.server.util.FileUtil;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RelatorioServiceImpl extends CrudServiceImpl<Relatorio, Long> implements RelatorioService {

    private final RelatorioRepository relatorioRepository;

    private final JdbcTemplate jdbcTemplate;

    public RelatorioServiceImpl(RelatorioRepository relatorioRepository, @Qualifier("jdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.relatorioRepository = relatorioRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    protected JpaRepository<Relatorio, Long> getRepository() {
        return this.relatorioRepository;
    }

    @Override
    @Transactional
    public void saveFileReport(MultipartHttpServletRequest file,
                               HttpServletRequest request,
                               Long idRelatorio) throws IOException {
        Relatorio relatorio = relatorioRepository.getOne(idRelatorio);
        this.deleteFileCurrent(relatorio);
        File dir = new File(FileUtil.getAbsolutePathRaiz() + File.separator + "report");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        var fileUpload = file.getFile("anexo");
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

    public void deleteFileCurrent(Relatorio relatorio) {
        if (relatorio.getNameReport() != null) {
            deleteFileReport(relatorio.getNameReport());
        }
    }

    @Override
    @Transactional
    public JasperPrint generateReport(Long idRelatorio, List<RelatorioParamsValue> paramsRel) throws SQLException, JRException {
        Relatorio relatorio = this.findOne(idRelatorio);
        Connection conn = jdbcTemplate.getDataSource().getConnection();
        String path = new File(FileUtil.getAbsolutePathRaiz() +
                File.separator +
                "report" +
                File.separator +
                relatorio.getNameReport()).getPath();
        JasperDesign design = JRXmlLoader.load(path);
        JasperReport jasperReport = JasperCompileManager.compileReport(design);
        Map<String, Object> parameters = new HashMap<>();

        if (paramsRel != null && paramsRel.size() > 0) {
            paramsRel.forEach(param -> parameters.put(param.getNameParam(), param.getValueParam()));
        }

        JasperPrint print = JasperFillManager.fillReport(jasperReport,
                parameters, conn);
        conn.close();
        return print;
    }

    @Override
    public void deleteFileReport(String nameRelatorio) {
        File dir = new File(FileUtil.getAbsolutePathRaiz() +
                File.separator +
                "report" +
                File.separator +
                nameRelatorio);
        dir.delete();
    }
}
