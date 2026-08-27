package mb.cpo.sistema.c3pomail.config;

import tools.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bucket;
import mb.cpo.sistema.c3pomail.filter.ApiKeyAuthenticationFilter;
import mb.cpo.sistema.c3pomail.filter.RateLimitFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuração de segurança da API.
 *
 * <p>Registra os filtros de rate limiting e autenticação por API Key,
 * desabilita CSRF (API stateless), sessions e formLogin,
 * e injeta o cabeçalho HSTS para forçar HTTPS.</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${api.email.chave}")
    private String chaveApi;

    private final Bucket bucket;
    private final ObjectMapper objectMapper;

    public SecurityConfig(Bucket bucket, ObjectMapper objectMapper) {
        this.bucket = bucket;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain cadeiaFiltrosSeguranca(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // CSRF desabilitado — API stateless sem cookies de sessão
                .csrf(csrf -> csrf.disable())

                // Sessões desabilitadas — cada requisição é independente
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // FormLogin desabilitado — autenticação exclusivamente via X-API-KEY
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // HSTS — Strict-Transport-Security: max-age=31536000; includeSubDomains
                .headers(cabecalhos -> cabecalhos
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)
                        )
                )

                // Regras de autorização
                .authorizeHttpRequests(autorizacao -> autorizacao
                        // Rotas públicas (sem autenticação)
                        .requestMatchers(
                                "/actuator/health",
                                "/",
                                "/index.html",
                                "/lib/**",
                                "/css/**",
                                "/js/**",
                                "/*.ico"
                        ).permitAll()
                        // Todas as demais rotas exigem autenticação
                        .anyRequest().authenticated()
                )

                // Registra filtros customizados ANTES do filtro padrão do Spring Security
                .addFilterBefore(
                        new RateLimitFilter(bucket, objectMapper),
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilterAfter(
                        new ApiKeyAuthenticationFilter(chaveApi, objectMapper),
                        RateLimitFilter.class
                );

        return httpSecurity.build();
    }
}
