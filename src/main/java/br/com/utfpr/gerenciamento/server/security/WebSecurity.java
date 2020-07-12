package br.com.utfpr.gerenciamento.server.security;

import br.com.utfpr.gerenciamento.server.service.impl.UsuarioServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class WebSecurity extends WebSecurityConfigurerAdapter {

    @Autowired
    private UsuarioServiceImpl usuarioService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().authorizeRequests()
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
                .antMatchers(HttpMethod.POST, "/usuario/**").hasRole("ADMINISTRADOR")
                .antMatchers(HttpMethod.DELETE, "/usuario/**").hasRole("ADMINISTRADOR")
                .antMatchers(HttpMethod.POST, "/emprestimo/save-emprestimo", "/emprestimo/save-devolucao").hasAnyRole("LABORATORISTA", "ADMINISTRADOR")
                .antMatchers(HttpMethod.DELETE, "/emprestimo/**").hasAnyRole("LABORATORISTA", "ADMINISTRADOR")
                .antMatchers(HttpMethod.GET, "/usuario/user-info").permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilter(new JWTAuthenticationFilter(authenticationManager(), getApplicationContext()))
                .addFilter(new JWTAuthorizationFilter(authenticationManager(), getApplicationContext()))
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Bean
    @Override
    protected UserDetailsService userDetailsService() {
        return usuarioService;
    }

    @Bean
    protected PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService())
                .passwordEncoder(passwordEncoder());
    }
}
