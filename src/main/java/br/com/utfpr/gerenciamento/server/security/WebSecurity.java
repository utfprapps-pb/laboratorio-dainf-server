package br.com.utfpr.gerenciamento.server.security;

import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import br.com.utfpr.gerenciamento.server.service.impl.UsuarioServiceImpl;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

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
                .passwordEncoder( passwordEncoder() );
        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

        http.cors()
                .and()
                .csrf().disable().authorizeRequests()
                .antMatchers("/cidade/**",
                        "/estado/**",
                        "/pais/**",
                        "/relatorio/**",
                        "/fornecedor/**",
                        "/compra/**",
                        "/entrada/**",
                        "/grupo/**",
                        "/saida/**").hasAnyRole("LABORATORISTA", "ADMINISTRADOR")
                .antMatchers(HttpMethod.POST, "/item/**").hasAnyRole("LABORATORISTA", "ADMINISTRADOR")
                .antMatchers(HttpMethod.DELETE, "/item/**").hasAnyRole("LABORATORISTA", "ADMINISTRADOR")

                .antMatchers(HttpMethod.POST, "/usuario/new-user/**").permitAll()
                .antMatchers(HttpMethod.POST, "/usuario/resend-confirm-email/**").permitAll()
                .antMatchers(HttpMethod.POST, "/usuario/confirm-email/**").permitAll()
                .antMatchers(HttpMethod.POST, "/usuario/reset-password/**").permitAll()
                .antMatchers(HttpMethod.POST, "/usuario/request-code-reset-password/**").permitAll()

                .antMatchers(HttpMethod.POST, "/usuario/update-user").authenticated()

                .antMatchers(HttpMethod.POST, "/usuario/**").hasRole("ADMINISTRADOR")
                .antMatchers(HttpMethod.DELETE, "/usuario/**").hasRole("ADMINISTRADOR")
                .antMatchers(HttpMethod.POST, "/emprestimo/save-emprestimo", "/emprestimo/save-devolucao").hasAnyRole("LABORATORISTA", "ADMINISTRADOR")
                .antMatchers(HttpMethod.DELETE, "/emprestimo/**").hasAnyRole("LABORATORISTA", "ADMINISTRADOR")

                .antMatchers(HttpMethod.GET, "/usuario/user-info").permitAll()


                .antMatchers(HttpMethod.POST, "/auth").permitAll()
                .antMatchers(HttpMethod.GET, "/test").permitAll()
                .anyRequest().authenticated()
                .and()
                .authenticationManager(authenticationManager)
                //Filtro da Autenticação
                .addFilter(new JWTAuthenticationFilter(authenticationManager, usuarioService, env) )
                //Filtro da Autorizaçao
                .addFilter(new JWTAuthorizationFilter(authenticationManager, usuarioService, env) )
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        return http.build();
    }

    @Bean
    protected PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
