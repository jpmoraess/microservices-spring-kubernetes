package br.com.coffeeandit.transaction.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
public class SecurityConfig {

    public static final String ACTUATOR = "/actuator/**";
    public static final String SCOPE_CREDITO_LIMITE_TRANSACAO = "SCOPE_CoffeeAndITRole";


    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http.
                exceptionHandling()
                .authenticationEntryPoint((serverWebExchange, e) ->
                        Mono.fromRunnable(() ->
                                serverWebExchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED)
                        )
                ).accessDeniedHandler((serverWebExchange, e) -> Mono.fromRunnable(() ->
                serverWebExchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN)
        )).and()
                ///.redirectToHttps().and()
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable()
                .authorizeExchange()
                .pathMatchers(ACTUATOR, "/swagger-ui.html", "/webjars/**",
                        "/swagger-resources/**", "/v2/api-docs/**", "/.well-known/**", "/", "/favicon.ico", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .pathMatchers("/v1/transactions/**", "/v2/transactions/**").access((authentication, object) -> authentication.flatMap(flatAuthentication ->
                        Mono.just(new AuthorizationDecision(((JwtAuthenticationToken) flatAuthentication).getAuthorities()
                                .contains(new SimpleGrantedAuthority(SCOPE_CREDITO_LIMITE_TRANSACAO))))

                )


        )
                .anyExchange().authenticated()
                .and()
                .oauth2ResourceServer()
                .jwt();
        return http.build();
    }
}
