package com.bowlingpoints.config;

import com.bowlingpoints.config.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                //  CSRF protection intentionally disabled:
                // This backend uses stateless JWT authentication (no session cookies),
                // so CSRF tokens are not required or applicable.
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        //  Auth & públicos generales
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/dashboard").permitAll()

                        //  Rutas públicas (frontend sin login)
                        .requestMatchers(HttpMethod.GET, "/ambits/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/tournaments/*").permitAll()
                        .requestMatchers("/results/by-modality").permitAll()
                        .requestMatchers("/results/tournament-table").permitAll()
                        .requestMatchers("/results/all-player-ranking").permitAll()
                        .requestMatchers("/results/by-ambit").permitAll()
                        .requestMatchers("/api/user-stats/public-summary").permitAll()

                        //  Protegidas (requieren JWT válido)
                        .requestMatchers("/jugadores/upload").authenticated()

                        //  Todo lo demás
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "http://bowlingpoints-frontend.s3-website.us-east-2.amazonaws.com"
        ));// Cambiar en producción
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
