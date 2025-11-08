package br.com.utfpr.gerenciamento.server.fixture;

import br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto;
import br.com.utfpr.gerenciamento.server.model.Permissao;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import java.util.HashSet;

/**
 * Factory para criação de dados de teste para entidade Usuario e DTOs relacionados. Fornece métodos
 * convenientes para construir cenários comuns de usuários.
 *
 * <p>Uso recomendado em testes unitários e de integração:
 *
 * <pre>
 * UsuarioFactory factory = new UsuarioFactory();
 * Usuario aluno = factory.criarAluno("111111", "João Silva");
 * UsuarioResponseDto dto = factory.criarUsuarioResponseDto(aluno);
 * </pre>
 *
 * @author Rodrigo Izidoro
 * @since 2025-11-08
 */
public class UsuarioFactory {

  /** Cria um usuário aluno com documento e nome especificados. */
  public Usuario criarAluno(String documento, String nome) {
    return criarUsuario(documento, nome, "aluno@utfpr.edu.br", true);
  }

  /** Cria um usuário responsável com documento e nome especificados. */
  public Usuario criarResponsavel(String documento, String nome) {
    return criarUsuario(documento, nome, "responsavel@utfpr.edu.br", true);
  }

  /** Cria um usuário básico com documento e nome especificados. */
  public Usuario criarUsuarioBasico(String documento, String nome) {
    return criarUsuario(documento, nome, "user@utfpr.edu.br", true);
  }

  /** Cria um usuário sem email (para testes de validação). */
  public Usuario criarUsuarioSemEmail(String documento, String nome) {
    return criarUsuario(documento, nome, null, true);
  }

  /** Cria um usuário inativo. */
  public Usuario criarUsuarioInativo(String documento, String nome) {
    return criarUsuario(documento, nome, "user@utfpr.edu.br", false);
  }

  /** Cria um usuário com dados personalizados. */
  public Usuario criarUsuario(String documento, String nome, String email, boolean ativo) {
    Usuario usuario = new Usuario();
    usuario.setId(System.currentTimeMillis()); // ID único para testes
    usuario.setDocumento(documento);
    usuario.setNome(nome);
    usuario.setEmail(email);
    usuario.setAtivo(ativo);
    usuario.setEmailVerificado(true);
    usuario.setPassword("senha123");
    usuario.setTelefone("41999999999");
    usuario.setPermissoes(new HashSet<>());
    return usuario;
  }

  /** Cria um UsuarioResponseDto a partir de um Usuario. */
  public UsuarioResponseDto criarUsuarioResponseDto(Usuario usuario) {
    UsuarioResponseDto usuarioDto = new UsuarioResponseDto();
    usuarioDto.setId(usuario.getId());
    usuarioDto.setNome(usuario.getNome());
    usuarioDto.setUsername(usuario.getEmail()); // Username é o email
    usuarioDto.setDocumento(usuario.getDocumento());
    usuarioDto.setEmail(usuario.getEmail());
    // usuarioDto.setAtivo(usuario.getAtivo()); // UsuarioResponseDto doesn't have ativo field
    usuarioDto.setEmailVerificado(usuario.getEmailVerificado());
    usuarioDto.setTelefone(usuario.getTelefone());
    usuarioDto.setFotoUrl(usuario.getFotoUrl());
    // usuarioDto.setPermissoes(usuario.getPermissoes()); // Type mismatch - needs
    // PermissaoResponseDTO
    return usuarioDto;
  }

  /** Cria uma permissão com nome especificado. */
  public Permissao criarPermissao(String nome) {
    Permissao permissao = new Permissao();
    permissao.setNome(nome);
    return permissao;
  }

  /** Cria um usuário com permissão ALUNO. */
  public Usuario criarAlunoComPermissao(String documento, String nome) {
    Usuario usuario = criarAluno(documento, nome);
    Permissao permissao = criarPermissao("ALUNO");
    usuario.getPermissoes().add(permissao);
    return usuario;
  }

  /** Cria um usuário com permissão SERVIDOR. */
  public Usuario criarServidorComPermissao(String documento, String nome) {
    Usuario usuario = criarResponsavel(documento, nome);
    Permissao permissao = criarPermissao("SERVIDOR");
    usuario.getPermissoes().add(permissao);
    return usuario;
  }
}
