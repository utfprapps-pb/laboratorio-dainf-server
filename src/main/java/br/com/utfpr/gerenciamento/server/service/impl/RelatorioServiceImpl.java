package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.dto.RelatorioResponseDTO;
import br.com.utfpr.gerenciamento.server.exception.ArquivoException;
import br.com.utfpr.gerenciamento.server.model.Relatorio;
import br.com.utfpr.gerenciamento.server.model.RelatorioParamsValue;
import br.com.utfpr.gerenciamento.server.repository.RelatorioRepository;
import br.com.utfpr.gerenciamento.server.service.RelatorioService;
import br.com.utfpr.gerenciamento.server.util.FileUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Service
@Slf4j
public class RelatorioServiceImpl extends CrudServiceImpl<Relatorio, Long, RelatorioResponseDTO>
    implements RelatorioService {

  private final RelatorioRepository relatorioRepository;

  private final JdbcTemplate jdbcTemplate;
  private final ModelMapper modelMapper;

  public RelatorioServiceImpl(
          RelatorioRepository relatorioRepository,
          @Qualifier("jdbcTemplate") JdbcTemplate jdbcTemplate, ModelMapper modelMapper) {
    this.relatorioRepository = relatorioRepository;
    this.jdbcTemplate = jdbcTemplate;
      this.modelMapper = modelMapper;
  }

  @Override
  protected JpaRepository<Relatorio, Long> getRepository() {
    return this.relatorioRepository;
  }

  @Override
  public RelatorioResponseDTO toDto(Relatorio entity) {
    return modelMapper.map(entity, RelatorioResponseDTO.class);
  }

  @Override
  public Relatorio toEntity(RelatorioResponseDTO relatorioResponseDTO) {
    return modelMapper.map(relatorioResponseDTO, Relatorio.class);
  }

  @Override
  @Transactional
  public void saveFileReport(
      MultipartHttpServletRequest file, HttpServletRequest request, Long idRelatorio)
      throws IOException {

    var fileUpload = file.getFile("anexo");
    if (fileUpload == null || fileUpload.isEmpty()) {
      throw new ArquivoException("Nenhum arquivo foi enviado");
    }

    String fileName = fileUpload.getOriginalFilename();
    if (!FileUtil.hasValidExtension(fileName, "jrxml")) {
      throw new ArquivoException("Somente .jrxml são permitidos");
    }

    Relatorio relatorio = relatorioRepository.getOne(idRelatorio);
    deleteFileCurrent(relatorio);

    try {
      Path reportPath = FileUtil.getSecureReportPath(fileName);
      Files.write(reportPath, fileUpload.getBytes());

      relatorio.setNameReport(FileUtil.sanitizeFileName(fileName));
      save(relatorio);
    } catch (ArquivoException e) {
      log.error("Erro de segurança ao salvar relatório: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Erro ao salvar arquivo de relatório: {}", e.getMessage());
      throw new IOException("Falha ao salvar arquivo de relatório", e);
    }
  }

  public void deleteFileCurrent(Relatorio relatorio) {
    if (relatorio.getNameReport() != null) {
      deleteFileReport(relatorio.getNameReport());
    }
  }

  @Override
  @Transactional
  public JasperPrint generateReport(Long idRelatorio, List<RelatorioParamsValue> paramsRel)
      throws SQLException, JRException {
    Relatorio relatorio = toEntity( this.findOne(idRelatorio));
    if (relatorio.getNameReport() == null) {
      throw new ArquivoException("Nenhum arquivo de report foi especificado");
    }

    Path reportPath = FileUtil.getSecureReportPath(relatorio.getNameReport());
    if (!Files.isRegularFile(reportPath)) {
      throw new ArquivoException("Arquivo de report não encontrado");
    }

    try (Connection conn = jdbcTemplate.getDataSource().getConnection()) {
      JasperDesign design = JRXmlLoader.load(reportPath.toString());
      JasperReport jasperReport = JasperCompileManager.compileReport(design);

      Map<String, Object> parameters = new HashMap<>();

      if (paramsRel != null && !paramsRel.isEmpty()) {
        paramsRel.forEach(param -> parameters.put(param.getNameParam(), param.getValueParam()));
      }

      return JasperFillManager.fillReport(jasperReport, parameters, conn);
    } catch (ArquivoException e) {
      log.error("Erro de segurança ao gerar relatório: {}", e.getMessage());
      throw new JRException("Erro de segurança no acesso ao arquivo de relatório", e);
    } catch (Exception e) {
      log.error("Erro de I/O ao acessar arquivo de relatório: {}", e.getMessage());
      throw new JRException("Erro ao acessar arquivo de relatório", e);
    }
  }

  @Override
  public void deleteFileReport(String nameRelatorio) {
    if (!StringUtils.hasText(nameRelatorio)) return;

    try {
      Path reportPath = FileUtil.getSecureReportPath(nameRelatorio);
      Files.deleteIfExists(reportPath);
    } catch (IOException e) {
      log.warn("Erro ao deletar arquivo de relatório: {}", e.getMessage());
    }
  }
}
