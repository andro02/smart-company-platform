package com.smartcompany.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
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
            .authorizeHttpRequests((requests) -> requests
                    .requestMatchers(HttpMethod.POST, "/login").permitAll()
                    .requestMatchers(HttpMethod.POST, "/register").permitAll()
                    .requestMatchers(HttpMethod.POST, "/register/customer").permitAll()
                    // .requestMatchers(HttpMethod.POST, "api/files/upload").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/files/presigned-url/get").permitAll()
                    .requestMatchers(HttpMethod.PUT, "/api/files/presigned-url/put").permitAll()
                    // .requestMatchers(HttpMethod.GET, "/users/*").permitAll()
                    // .requestMatchers(HttpMethod.GET, "/api/accommodations").permitAll()
                    // .requestMatchers(HttpMethod.GET, "/api/accommodations/*").permitAll()
                    // .requestMatchers(HttpMethod.PUT, "/api/accommodations/reservations/*").hasAuthority("ROLE_Owner")
                    // .requestMatchers(HttpMethod.GET, "/api/ownerReviews").permitAll()
            .anyRequest().authenticated());
        return http.build();
    }

}
