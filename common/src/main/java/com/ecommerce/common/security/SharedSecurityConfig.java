package com.ecommerce.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SharedSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/v1/auth/**", "/api/v1/search/**", "/api/v1/migration/**", "/api/v1/categories/**").permitAll()
//                        .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
//                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
//                        .requestMatchers("/api/v1/payments/callback/**", "/api/v1/payments/webhook/**").permitAll()
//                        .requestMatchers("/api/v1/returns/all", "/api/v1/returns/*/status", "/api/v1/returns/*/approve", "/api/v1/returns/*/reject").hasAnyAuthority("ROLE_ADMIN", "ROLE_SELLER")
//
//                        .requestMatchers("/api/v1/orders/**", "/api/v1/returns/upload-evidence", "/api/v1/returns/request", "/api/v1/returns/my-requests/**").authenticated()
//                        .requestMatchers("/api/v1/chat/**").permitAll()
//                        .requestMatchers("/api/v1/returns").hasAnyAuthority("ROLE_SELLER")
//                        .requestMatchers("/ws-chat/**").permitAll() //
//                        .anyRequest().authenticated()
//                );
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/api/v1/search/**",
                                "/api/v1/migration/**",
                                "/api/v1/categories/**",
                                "/api/v1/embed/**",
                                "/api/v1/rerank/**",
                                "/api/v1/payments/callback/**",
                                "/api/v1/payments/webhook/**",
                                "/ws-chat/**"
                        ).permitAll()
                        .requestMatchers("/api/v1/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/v1/seller/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_SELLER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                        .requestMatchers("/api/v1/chat/**").hasAnyAuthority("ROLE_CUSTOMER", "ROLE_SELLER")
                        .requestMatchers("/api/v1/user/profile/**").authenticated()

                        .requestMatchers("/api/v1/returns/all", "/api/v1/returns/*/status",
                                "/api/v1/returns/*/approve", "/api/v1/returns/*/reject").hasAnyAuthority("ROLE_ADMIN", "ROLE_SELLER")
                        .requestMatchers("/api/v1/returns").hasAnyAuthority("ROLE_SELLER")

                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}