package br.com.utfpr.gerenciamento.server.fixture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Factory para configuração de mocks comuns usados em múltiplos testes. Fornece métodos
 * convenientes para configurar mocks de serviços frequentemente utilizados.
 *
 * <p>Uso recomendado em testes unitários:
 *
 * <pre>
 * MockFactory mockFactory = new MockFactory();
 * Usuario usuario = new UsuarioFactory().criarAluno("111111", "João");
 * mockFactory.configurarUsuarioService(usuarioService, usuario);
 * mockFactory.configurarEventPublisher(eventPublisher);
 * </pre>
 *
 * @author Rodrigo Izidoro
 * @since 2025-11-08
 */
public class MockFactory {

  private final UsuarioFactory usuarioFactory;

  public MockFactory() {
    this.usuarioFactory = new UsuarioFactory();
  }

  /** Configura mocks básicos para UsuarioService. */
  public void configurarUsuarioService(UsuarioService usuarioService, Usuario usuario) {
    when(usuarioService.findByDocumento(anyString()))
        .thenReturn(usuarioFactory.criarUsuarioResponseDto(usuario));
    when(usuarioService.toEntity(any(UsuarioResponseDto.class))).thenReturn(usuario);
    when(usuarioService.save(any(Usuario.class)))
        .thenReturn(usuarioFactory.criarUsuarioResponseDto(usuario));
  }

  /** Configura mocks para UsuarioService com diferentes documentos. */
  public void configurarUsuarioServicePorDocumento(
      UsuarioService usuarioService, String documento, Usuario usuario) {
    when(usuarioService.findByDocumento(documento))
        .thenReturn(usuarioFactory.criarUsuarioResponseDto(usuario));
    when(usuarioService.toEntity(any(UsuarioResponseDto.class))).thenReturn(usuario);
    when(usuarioService.save(any(Usuario.class)))
        .thenReturn(usuarioFactory.criarUsuarioResponseDto(usuario));
  }

  /** Configura mock para EventPublisher para não fazer nada. */
  public void configurarEventPublisher(ApplicationEventPublisher eventPublisher) {
    doNothing().when(eventPublisher).publishEvent(any());
  }

  /** Configura mock para ModelMapper com conversões básicas. */
  @SuppressWarnings("unchecked")
  public <S, T> void configurarModelMapper(ModelMapper modelMapper, S source, T target) {
    when(modelMapper.map(source, (Class<T>) target.getClass())).thenReturn(target);
  }

  /** Configura mock para ModelMapper com conversão de DTO para Entity. */
  public void configurarModelMapperDtoParaEntity(
      ModelMapper modelMapper, UsuarioResponseDto dto, Usuario entity) {
    when(modelMapper.map(dto, Usuario.class)).thenReturn(entity);
  }

  /** Configura mock para ModelMapper com conversão de Entity para DTO. */
  public void configurarModelMapperEntityParaDto(
      ModelMapper modelMapper, Usuario entity, UsuarioResponseDto dto) {
    when(modelMapper.map(entity, UsuarioResponseDto.class)).thenReturn(dto);
  }

  /** Cria usuário mockado com documento específico. */
  public Usuario criarUsuarioMock(String documento, String nome, Long id) {
    Usuario usuario = usuarioFactory.criarAluno(documento, nome);
    usuario.setId(id);
    return usuario;
  }

  /** Cria múltiplos usuários mockados para testes. */
  public Usuario[] criarUsuariosMock(String[] documentos, String[] nomes, Long[] ids) {
    if (documentos.length != nomes.length || nomes.length != ids.length) {
      throw new IllegalArgumentException("Arrays devem ter o mesmo tamanho");
    }

    Usuario[] usuarios = new Usuario[documentos.length];
    for (int i = 0; i < documentos.length; i++) {
      usuarios[i] = criarUsuarioMock(documentos[i], nomes[i], ids[i]);
    }
    return usuarios;
  }

  /** Configura mocks para um cenário completo de usuário. */
  public void configurarCenarioUsuarioCompleto(
      UsuarioService usuarioService,
      ModelMapper modelMapper,
      ApplicationEventPublisher eventPublisher,
      String documento,
      String nome,
      Long id) {

    Usuario usuario = criarUsuarioMock(documento, nome, id);
    UsuarioResponseDto dto = usuarioFactory.criarUsuarioResponseDto(usuario);

    configurarUsuarioServicePorDocumento(usuarioService, documento, usuario);
    configurarModelMapperDtoParaEntity(modelMapper, dto, usuario);
    configurarModelMapperEntityParaDto(modelMapper, usuario, dto);
    configurarEventPublisher(eventPublisher);
  }
}
