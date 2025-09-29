package io.jzheaux.spring.cleaning.config;

import io.jzheaux.spring.cleaning.service.JwtAuthFilter;
import io.jzheaux.spring.cleaning.service.JwtUtil;
import io.jzheaux.spring.cleaning.service.MyUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


@Configuration
public class SecurityConfig {
    /**
     * Provides the JWT authentication filter used to validate JWTs on incoming requests.
     *
     * @param jwtUtil Utility class for parsing and verifying JWT tokens.
     * @param userDetailsService Service to load user-specific data from the database.
     * @return Configured {@link JwtAuthFilter} bean.
     */
    @Bean
    public JwtAuthFilter jwtAuthFilter(JwtUtil jwtUtil, MyUserDetailsService userDetailsService) {
        return new JwtAuthFilter(jwtUtil, userDetailsService);
    }

    /**
     * Defines the application's security filter chain.
     * Configures:
     * - CORS handling
     * - CSRF disabled
     * - Public access to /auth/**
     * - JWT-based stateless session management
     *
     * @param http Spring's security builder.
     * @param jwtAuthFilter The filter responsible for validating JWTs.
     * @param corsSource The CORS configuration source.
     * @return A SecurityFilterChain defining access rules and filters.
     * @throws Exception if the security chain setup fails.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter, CorsConfigurationSource corsSource) throws Exception {
        http.cors(cors -> cors.configurationSource(corsSource))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    /**
     * Configures global CORS settings for frontend-backend communication.
     * Allows requests from localhost frontend and enables all standard HTTP methods and headers.
     *
     * @return {@link CorsConfigurationSource} with applied rules for /auth/** and /api/** paths.
     */
    @Bean
    @Primary
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/auth/**", config);
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    /**
     * Provides the AuthenticationManager bean used for authentication logic,
     * typically during login.
     *
     * @param config Spring's AuthenticationConfiguration.
     * @return An instance of AuthenticationManager.
     * @throws Exception if unable to initialize authentication manager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    /**
     * Provides the password encoder used to hash and verify passwords using BCrypt.
     *
     * @return A BCryptPasswordEncoder instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
