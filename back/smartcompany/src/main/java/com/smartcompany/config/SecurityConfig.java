package com.smartcompany.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private UserAuthenticationEntryPoint userAuthenticationEntryPoint;
    @Autowired
    private UserAuthenticationProvider userAuthenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .exceptionHandling(customizer -> customizer.authenticationEntryPoint(userAuthenticationEntryPoint))
            .addFilterBefore(new JwtAuthFilter(userAuthenticationProvider), BasicAuthenticationFilter.class)
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(customizer -> customizer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(requests -> requests
                    // Swagger
                    .requestMatchers("/swagger-ui/**", "/swagger-ui", "/api-docs/**", "/api-docs").permitAll()
                    // Auth
                    .requestMatchers(HttpMethod.POST, "/login").permitAll()
                    .requestMatchers(HttpMethod.POST, "/register/customer").permitAll()
                    // Aktivacija
                    .requestMatchers(HttpMethod.GET, "/activate/**").permitAll()
                    // Fajlovi
                    .requestMatchers(HttpMethod.GET, "/api/files/presigned-url/get").permitAll()
                    .requestMatchers(HttpMethod.PUT, "/api/files/presigned-url/put").permitAll()
                    // Promena lozinke dozvoli svima koji imaju validan token
                    .requestMatchers(HttpMethod.PUT, "/account/change-password").authenticated()
                    // Proizvodi i kategorije javno citanje
                    .requestMatchers(HttpMethod.GET, "/products").permitAll()
                    .requestMatchers(HttpMethod.GET, "/products/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/categories").permitAll()
                    .requestMatchers(HttpMethod.GET, "/categories/**").permitAll()
                    .requestMatchers(HttpMethod.GET,  "/api/orders/my-companies").hasRole("Customer")
                    .requestMatchers(HttpMethod.GET,  "/api/orders/products").hasRole("Customer")
                    .requestMatchers(HttpMethod.POST, "/api/orders").hasRole("Customer")
                    // Menadzeri samo SuperAdmin (dodatno osigurano @PreAuthorize)
                    .requestMatchers(HttpMethod.GET, "/users/managers").hasRole("SuperAdmin")
                    .requestMatchers(HttpMethod.PUT, "/users/*/block").hasRole("SuperAdmin")
                    .requestMatchers(HttpMethod.PUT, "/users/*/unblock").hasRole("SuperAdmin")
                    .requestMatchers(HttpMethod.POST, "/register/manager").hasRole("SuperAdmin")
                    .requestMatchers(HttpMethod.GET,    "/warehouses/**").hasAnyRole("Manager", "SuperAdmin")
                    .requestMatchers(HttpMethod.POST,   "/warehouses").hasAnyRole("Manager", "SuperAdmin")
                    .requestMatchers(HttpMethod.PUT,    "/warehouses/**").hasAnyRole("Manager", "SuperAdmin")
                    .requestMatchers(HttpMethod.DELETE, "/warehouses/**").hasAnyRole("Manager", "SuperAdmin")
                    // Ostalo zahteva autentifikaciju
                    .anyRequest().authenticated()
            );
        return http.build();
    }
}