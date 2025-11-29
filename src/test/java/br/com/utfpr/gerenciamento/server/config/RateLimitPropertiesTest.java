package br.com.utfpr.gerenciamento.server.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RateLimitPropertiesTest {

  @Test
  void shouldHaveDefaultValuesForAllEndpoints() {
    RateLimitProperties properties = new RateLimitProperties();

    assertNotNull(properties.getLogin());
    assertNotNull(properties.getAuth());
    assertNotNull(properties.getPasswordResetRequest());
    assertNotNull(properties.getNewUser());
    assertNotNull(properties.getPasswordReset());
    assertNotNull(properties.getResendEmail());
    assertNotNull(properties.getConfirmEmail());
    assertTrue(properties.isEnabled());
  }

  @Test
  void shouldHaveCorrectDefaultLoginLimits() {
    RateLimitProperties properties = new RateLimitProperties();

    assertEquals(5, properties.getLogin().getRequests());
    assertEquals(15, properties.getLogin().getDurationMinutes());
  }

  @Test
  void shouldHaveCorrectDefaultAuthLimits() {
    RateLimitProperties properties = new RateLimitProperties();

    assertEquals(10, properties.getAuth().getRequests());
    assertEquals(15, properties.getAuth().getDurationMinutes());
  }

  @Test
  void shouldHaveCorrectDefaultPasswordResetRequestLimits() {
    RateLimitProperties properties = new RateLimitProperties();

    assertEquals(3, properties.getPasswordResetRequest().getRequests());
    assertEquals(60, properties.getPasswordResetRequest().getDurationMinutes());
  }

  @Test
  void shouldHaveCorrectDefaultNewUserLimits() {
    RateLimitProperties properties = new RateLimitProperties();

    assertEquals(3, properties.getNewUser().getRequests());
    assertEquals(60, properties.getNewUser().getDurationMinutes());
  }

  @Test
  void shouldHaveCorrectDefaultPasswordResetLimits() {
    RateLimitProperties properties = new RateLimitProperties();

    assertEquals(5, properties.getPasswordReset().getRequests());
    assertEquals(15, properties.getPasswordReset().getDurationMinutes());
  }

  @Test
  void shouldHaveCorrectDefaultResendEmailLimits() {
    RateLimitProperties properties = new RateLimitProperties();

    assertEquals(5, properties.getResendEmail().getRequests());
    assertEquals(60, properties.getResendEmail().getDurationMinutes());
  }

  @Test
  void shouldHaveCorrectDefaultConfirmEmailLimits() {
    RateLimitProperties properties = new RateLimitProperties();

    assertEquals(10, properties.getConfirmEmail().getRequests());
    assertEquals(15, properties.getConfirmEmail().getDurationMinutes());
  }

  @Test
  void shouldAllowCustomLimits() {
    RateLimitProperties properties = new RateLimitProperties();
    RateLimitProperties.EndpointLimit customLimit = new RateLimitProperties.EndpointLimit(100, 30);

    properties.setLogin(customLimit);

    assertEquals(100, properties.getLogin().getRequests());
    assertEquals(30, properties.getLogin().getDurationMinutes());
  }

  @Test
  void shouldAllowDisablingRateLimit() {
    RateLimitProperties properties = new RateLimitProperties();
    assertTrue(properties.isEnabled());

    properties.setEnabled(false);

    assertFalse(properties.isEnabled());
  }

  @Test
  void endpointLimitShouldHaveDefaultConstructor() {
    RateLimitProperties.EndpointLimit limit = new RateLimitProperties.EndpointLimit();

    assertEquals(10, limit.getRequests());
    assertEquals(15, limit.getDurationMinutes());
  }

  @Test
  void endpointLimitShouldAllowSettingValues() {
    RateLimitProperties.EndpointLimit limit = new RateLimitProperties.EndpointLimit();

    limit.setRequests(50);
    limit.setDurationMinutes(120);

    assertEquals(50, limit.getRequests());
    assertEquals(120, limit.getDurationMinutes());
  }
}
