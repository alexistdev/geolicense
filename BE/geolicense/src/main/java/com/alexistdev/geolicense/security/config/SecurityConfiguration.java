package com.alexistdev.geolicense.security.config;

import com.alexistdev.geolicense.models.entity.Role;
import com.alexistdev.geolicense.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    public SecurityConfiguration(JwtAuthenticationFilter jwtAuthFilter, AuthenticationProvider authenticationProvider) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req -> req
                        .requestMatchers("/api/v1/auth/**","/api/v1/licenses/activate",
                                "/api/v1/licenses/verify").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/users").hasAuthority(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/products").hasAuthority(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/products").hasAnyAuthority(Role.ADMIN.toString(),Role.USER.toString())
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/v1/products").hasAuthority(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/products/search").hasAuthority(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/licenses_type").hasAnyAuthority(Role.ADMIN.toString(), Role.USER.toString())
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/licenses_type/search").hasAnyAuthority(Role.ADMIN.toString(),Role.USER.toString())
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/licenses_type").hasAuthority(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/v1/licenses_type").hasAuthority(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/v1/licenses_type").hasAuthority(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/licenses/user/*").hasAuthority(Role.USER.toString())
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/licenses/*/user/*").hasAuthority(Role.USER.toString())
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/marketplace/products").hasAuthority(Role.USER.toString())
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/marketplace/products/*").hasAuthority(Role.USER.toString())
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/orders").hasAuthority(Role.USER.toString())
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/orders").hasAuthority(Role.USER.toString())
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/invoices").hasAuthority(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/invoices/me").hasAuthority(Role.USER.toString())
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/invoices/me/*").hasAuthority(Role.USER.toString())
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/invoices/*").hasAuthority(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/v1/invoices/*/validate").hasAuthority(Role.ADMIN.toString())
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
