package br.com.utfpr.gerenciamento.server.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import br.com.utfpr.gerenciamento.server.model.SystemConfig;
import br.com.utfpr.gerenciamento.server.repository.SystemConfigRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SystemConfigServiceImplTest {
  private SystemConfigRepository repository;
  private SystemConfigServiceImpl service;

  @BeforeEach
  void setup() {
    repository = Mockito.mock(SystemConfigRepository.class);
    service = new SystemConfigServiceImpl(repository);
  }

  @Test
  void saveConfigShouldUpdateExistingConfig() {
    SystemConfig existing = new SystemConfig();
    existing.setId(1L);
    existing.setNadaConstaEmail("old@utfpr.edu.br");
    SystemConfig newConfig = new SystemConfig();
    newConfig.setNadaConstaEmail("new@utfpr.edu.br");
    Mockito.when(repository.findFirstByIsActiveTrue()).thenReturn(Optional.of(existing));
    Mockito.when(repository.save(Mockito.any()))
        .thenAnswer(
            inv -> {
              SystemConfig arg = inv.getArgument(0);
              if (arg.getId() == null) arg.setId(1L);
              return arg;
            });
    SystemConfig saved = service.saveConfig(newConfig);
    assertEquals("new@utfpr.edu.br", saved.getNadaConstaEmail());
    assertEquals(1L, saved.getId());
  }

  @Test
  void saveConfigShouldCreateConfigIfNotExists() {
    // Simula usuário autenticado
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken("testuser", "password"));
    SystemConfig newConfig = new SystemConfig();
    newConfig.setNadaConstaEmail("new@utfpr.edu.br");
    Mockito.when(repository.findFirstByIsActiveTrue()).thenReturn(Optional.empty());
    ArgumentCaptor<SystemConfig> configCaptor = ArgumentCaptor.forClass(SystemConfig.class);
    Mockito.when(repository.save(Mockito.any()))
        .thenAnswer(
            inv -> {
              SystemConfig config = inv.getArgument(0);
              // Simula auditoria
              config.setCreatedAt(java.time.LocalDateTime.now());
              config.setUpdatedAt(config.getCreatedAt());
              config.setCreatedBy("testuser");
              config.setUpdatedBy("testuser");
              return config;
            });

    SystemConfig saved = service.saveConfig(newConfig);
    Mockito.verify(repository).save(configCaptor.capture());
    SystemConfig captured = configCaptor.getValue();
    assertNull(captured.getId(), "Id should be null before persistence");
    assertTrue(captured.getIsActive(), "Config should be active");
    assertEquals("new@utfpr.edu.br", captured.getNadaConstaEmail());
    // Valida auditoria
    assertNotNull(captured.getCreatedAt(), "createdAt should be set");
    assertNotNull(captured.getUpdatedAt(), "updatedAt should be set");
    assertEquals("testuser", captured.getCreatedBy(), "createdBy should match authenticated user");
    assertEquals("testuser", captured.getUpdatedBy(), "updatedBy should match authenticated user");
    // Limpa contexto
    SecurityContextHolder.clearContext();
  }

  @Test
  void getConfigShouldReturnConfigIfExists() {
    SystemConfig config = new SystemConfig();
    Mockito.when(repository.findFirstByIsActiveTrue()).thenReturn(Optional.of(config));
    Optional<SystemConfig> result = service.getConfig();
    assertTrue(result.isPresent());
    assertEquals(config, result.get());
  }

  @Test
  void getConfigShouldReturnEmptyIfNotExists() {
    Mockito.when(repository.findFirstByIsActiveTrue()).thenReturn(Optional.empty());
    Optional<SystemConfig> result = service.getConfig();
    assertFalse(result.isPresent());
  }

  @Test
  void deleteConfigShouldRemoveIfExists() {
    SystemConfig config = new SystemConfig();
    Mockito.when(repository.findFirstByIsActiveTrue()).thenReturn(Optional.of(config));
    service.deleteConfig();
    Mockito.verify(repository).delete(config);
  }

  @Test
  void deleteConfigShouldDoNothingIfNotExists() {
    Mockito.when(repository.findFirstByIsActiveTrue()).thenReturn(Optional.empty());
    service.deleteConfig();
    Mockito.verify(repository, Mockito.never()).delete(Mockito.any());
  }
}
