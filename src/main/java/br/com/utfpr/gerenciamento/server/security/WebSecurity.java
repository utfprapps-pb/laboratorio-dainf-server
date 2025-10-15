package br.com.utfpr.gerenciamento.server.security;

import static br.com.utfpr.gerenciamento.server.enumeration.UserRole.ROLE_ADMINISTRADOR_NAME;
import static br.com.utfpr.gerenciamento.server.enumeration.UserRole.ROLE_LABORATORISTA_NAME;
import static br.com.utfpr.gerenciamento.server.security.ApiRoutes.*;

import br.com.utfpr.gerenciamento.server.service.impl.UsuarioServiceImpl;
import java.util.List;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@EnableWebSecurity
@Configuration
public class WebSecurity {
  private final UsuarioServiceImpl usuarioService;
  private final Environment env;

  public WebSecurity(@Lazy UsuarioServiceImpl usuarioService, Environment env) {
    this.usuarioService = usuarioService;
    this.env = env;
  }

  @Bean
  @SneakyThrows
  public SecurityFilterChain filterChain(HttpSecurity http) {
    var authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
    authenticationManagerBuilder
        .userDetailsService(usuarioService)
        .passwordEncoder(passwordEncoder());
    var authenticationManager = authenticationManagerBuilder.build();

    return http.csrf(csrf -> csrf.ignoringRequestMatchers("/**"))
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .authorizeHttpRequests(
            authorize ->
                authorize
                    // Endpoints administrativos - requerem LABORATORISTA ou ADMINISTRADOR
                    .requestMatchers(
                        CIDADE, ESTADO, PAIS, RELATORIO, FORNECEDOR, COMPRA, ENTRADA, GRUPO, SAIDA)
                    .hasAnyRole(ROLE_LABORATORISTA_NAME, ROLE_ADMINISTRADOR_NAME)

                    // Item - POST/DELETE requerem LABORATORISTA ou ADMINISTRADOR
                    .requestMatchers(HttpMethod.POST, ITEM)
                    .hasAnyRole(ROLE_LABORATORISTA_NAME, ROLE_ADMINISTRADOR_NAME)
                    .requestMatchers(HttpMethod.DELETE, ITEM)
                    .hasAnyRole(ROLE_LABORATORISTA_NAME, ROLE_ADMINISTRADOR_NAME)

                    // Usuário - endpoints públicos de registro/recuperação
                    .requestMatchers(
                        HttpMethod.POST,
                        USUARIO_NEW_USER,
                        USUARIO_RESEND_CONFIRM,
                        USUARIO_CONFIRM_EMAIL,
                        USUARIO_RESET_PASSWORD,
                        USUARIO_REQUEST_CODE_RESET)
                    .permitAll()

                    // Usuário - endpoints autenticados
                    .requestMatchers(HttpMethod.POST, USUARIO_UPDATE)
                    .authenticated()
                    .requestMatchers(HttpMethod.GET, USUARIO_INFO, USUARIO_FIND_BY_USERNAME)
                    .authenticated()

                    // Usuário - administração requer role ADMINISTRADOR
                    .requestMatchers(HttpMethod.PUT, USUARIO)
                    .hasRole(ROLE_ADMINISTRADOR_NAME)
                    .requestMatchers(HttpMethod.PATCH, USUARIO)
                    .hasRole(ROLE_ADMINISTRADOR_NAME)
                    .requestMatchers(HttpMethod.POST, USUARIO)
                    .hasRole(ROLE_ADMINISTRADOR_NAME)
                    .requestMatchers(HttpMethod.DELETE, USUARIO)
                    .hasRole(ROLE_ADMINISTRADOR_NAME)

                    // Empréstimo - POST/DELETE requerem LABORATORISTA ou ADMINISTRADOR
                    .requestMatchers(HttpMethod.POST, EMPRESTIMO_SAVE, EMPRESTIMO_DEVOLUCAO)
                    .hasAnyRole(ROLE_LABORATORISTA_NAME, ROLE_ADMINISTRADOR_NAME)
                    .requestMatchers(HttpMethod.DELETE, EMPRESTIMO)
                    .hasAnyRole(ROLE_LABORATORISTA_NAME, ROLE_ADMINISTRADOR_NAME)

                    // Endpoints públicos
                    .requestMatchers(HttpMethod.POST, AUTH)
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, TEST)
                    .permitAll()

                    // Actuator endpoints - requerem ADMINISTRADOR
                    .requestMatchers(ACTUATOR)
                    .hasRole(ROLE_ADMINISTRADOR_NAME)

                    // Endpoints adicionais
                    .requestMatchers(HttpMethod.PUT, "/usuario/**")
                    .hasRole("ADMINISTRADOR")
                    .requestMatchers(HttpMethod.PATCH, "/usuario/**")
                    .hasRole("ADMINISTRADOR")
                    .requestMatchers(HttpMethod.POST, "/usuario/**")
                    .hasRole("ADMINISTRADOR")
                    .requestMatchers(HttpMethod.DELETE, "/usuario/**")
                    .hasRole("ADMINISTRADOR")
                    .requestMatchers(HttpMethod.POST, "/auth")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/test")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/config")
                    .hasRole("ADMINISTRADOR")
                    .requestMatchers(HttpMethod.POST, "/config")
                    .hasRole("ADMINISTRADOR")

                    // Demais endpoints requerem autenticação
                    .anyRequest()
                    .authenticated())
        .authenticationManager(authenticationManager)
        .addFilter(new JWTAuthenticationFilter(authenticationManager, usuarioService, env))
        .addFilter(new JWTAuthorizationFilter(authenticationManager, usuarioService, env))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .build();
  }

  @Bean
  protected PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    var frontendUrls = env.getProperty("utfpr.front.url", "http://localhost:4200");
    var origins = java.util.Arrays.stream(frontendUrls.split(",")).map(String::trim).toList();

    var configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(origins);

    configuration.setAllowedMethods(
        List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));

    configuration.setAllowedHeaders(
        List.of(
            "Authorization",
            "x-xsrf-token",
            "Access-Control-Allow-Headers",
            "Origin",
            "Accept",
            "X-Requested-With",
            "Content-Type",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "Auth-Id-Token"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
