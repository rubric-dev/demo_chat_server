package com.example.chat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class WebSecurityConfig {

    @Value("${application.Environment}")
    private String environment;
    private static final String[] SWAGGER_URL = {
        "/v3/api-docs",
        "/swagger-resources",
        "/swagger-resources/configuration/ui",
        "/swagger-ui.html",
        "/configuration/security",
        "/webjars/**",
        "/swagger-ui/**",
        "/v3/api-docs/**"
    };

    private static final String[] TEST_URLS = {
        "/test",
        "/test/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        if ("dev".equals(environment) || "local".equals(environment)) {
            http
                .authorizeRequests()
                .antMatchers(TEST_URLS).permitAll()
                .antMatchers(SWAGGER_URL).permitAll()
            ;
        }

        http
            // api only
            .sessionManagement((sessionManagement) ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf().disable()
            .cors();

        http
            // auth by url
            .authorizeRequests(
                (authorizeRequests) ->
                    authorizeRequests
                        .antMatchers(HttpMethod.OPTIONS, "/**").permitAll() // permit options
                        .antMatchers("/", "/robots.txt").permitAll() // health check && robots.txt && error
                        .antMatchers("/auth/**", "/regi/**").anonymous()
                        .antMatchers("/oauth2/*/code", "/oauth2/*/authorization").permitAll()
                        .anyRequest().authenticated()
            );

        http
            .formLogin().disable()
            .logout().disable()
            .exceptionHandling();

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) ->
            web
                .ignoring()
                .antMatchers(SWAGGER_URL);
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}