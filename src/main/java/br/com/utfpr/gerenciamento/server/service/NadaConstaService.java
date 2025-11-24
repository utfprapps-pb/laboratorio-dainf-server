package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.dto.NadaConstaResponseDto;
import br.com.utfpr.gerenciamento.server.model.NadaConsta;
import java.util.List;

/**
 * Interface de serviço para operações relacionadas ao Nada Consta.
 *
 * <p>Disponibiliza métodos para solicitação, verificação de pendências, invalidação e conversão de
 * entidades.
 */
public interface NadaConstaService extends CrudService<NadaConsta, Long, NadaConstaResponseDto> {
  /**
   * Busca todas as solicitações de Nada Consta pelo username do usuário.
   *
   * @param username Username do usuário
   * @return Lista de NadaConstaResponseDto
   */
  List<NadaConstaResponseDto> findAllByUsername(String username);

  /**
   * Solicita uma declaração de Nada Consta para o usuário informado.
   *
   * @param documento Documento do usuário
   * @return Dados da solicitação de Nada Consta
   */
  NadaConstaResponseDto solicitarNadaConsta(String documento);

  /**
   * Invalida uma declaração de Nada Consta emitida.
   *
   * @param id Identificador da solicitação
   * @return Dados atualizados da solicitação
   */
  NadaConstaResponseDto invalidarNadaConsta(Long id);

  /**
   * Converte uma entidade NadaConsta para o DTO de resposta.
   *
   * @param entity Entidade NadaConsta
   * @return NadaConstaResponseDto correspondente
   */
  NadaConstaResponseDto convertToDto(NadaConsta entity);

  /**
   * Verifica se as pendências do usuário foram resolvidas e atualiza o status da solicitação.
   *
   * @param id Identificador da solicitação de Nada Consta
   * @return Dados atualizados da solicitação
   */
  NadaConstaResponseDto verificarPendenciasNadaConsta(Long id);

  /**
   * Reenvia o email de Nada Consta utilizando os dados originais de emissão.
   *
   * @param id Identificador da solicitação de Nada Consta
   * @return true se o reenvio foi realizado com sucesso
   */
  boolean reenviarNadaConsta(Long id);
}
