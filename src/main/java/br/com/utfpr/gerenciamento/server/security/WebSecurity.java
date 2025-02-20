package br.com.utfpr.gerenciamento.server.security;

import br.com.utfpr.gerenciamento.server.service.impl.UsuarioServiceImpl;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(usuarioService)
                .passwordEncoder(passwordEncoder());
        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(cors -> corsConfigurationSource());

        http.authorizeHttpRequests((authorize) -> authorize
                .requestMatchers("/cidade/**",
                        "/estado/**",
                        "/pais/**",
                        "/relatorio/**",
                        "/fornecedor/**",
                        "/compra/**",
                        "/entrada/**",
                        "/grupo/**",
                        "/saida/**").hasAnyRole("LABORATORISTA", "ADMINISTRADOR")
                .requestMatchers(HttpMethod.POST, "/item/**").hasAnyRole("LABORATORISTA", "ADMINISTRADOR")
                .requestMatchers(HttpMethod.DELETE, "/item/**").hasAnyRole("LABORATORISTA", "ADMINISTRADOR")

                .requestMatchers(HttpMethod.POST, "/usuario/new-user/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/usuario/resend-confirm-email/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/usuario/confirm-email/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/usuario/reset-password/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/usuario/request-code-reset-password/**").permitAll()

                .requestMatchers(HttpMethod.POST, "/usuario/update-user").authenticated()





                .requestMatchers(HttpMethod.PUT, "/usuario/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.PATCH, "/usuario/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.POST, "/usuario/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.DELETE, "/usuario/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.POST, "/emprestimo/save-emprestimo", "/emprestimo/save-devolucao").hasAnyRole("LABORATORISTA", "ADMINISTRADOR")
                .requestMatchers(HttpMethod.DELETE, "/emprestimo/**").hasAnyRole("LABORATORISTA", "ADMINISTRADOR")

                .requestMatchers(HttpMethod.GET, "/usuario/user-info").permitAll()
                .requestMatchers(HttpMethod.GET, "/usuario/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.GET, "/usuario/user-info").permitAll()
                .requestMatchers(HttpMethod.POST, "/usuario/find-by-username").authenticated()

                .requestMatchers(HttpMethod.POST, "/auth").permitAll()
                .requestMatchers(HttpMethod.GET, "/test").permitAll()
                .anyRequest().authenticated()
        );

        http.authenticationManager(authenticationManager)
                .addFilter(
                        new JWTAuthenticationFilter(authenticationManager, usuarioService, env)
                )
                .addFilter(
                        new JWTAuthorizationFilter(authenticationManager, usuarioService, env)
                )
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS)
                );


        return http.build();
    }

    @Bean
    protected PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "TRACE", "CONNECT"));

        configuration.setAllowedHeaders(List.of("Authorization", "x-xsrf-token",
                "Access-Control-Allow-Headers", "Origin",
                "Accept", "X-Requested-With", "Content-Type",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers", "Auth-Id-Token"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
