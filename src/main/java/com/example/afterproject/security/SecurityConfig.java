package com.example.afterproject.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // NOTE: Explicitly defined role-based access for specific endpoints.
                        // If 403 errors persist, ensure:
                        // 1. Users have the correct roles assigned (e.g., ADMIN, TEACHER, STUDENT).
                        // 2. JWT tokens correctly carry these role claims.
                        // 3. Frontend sends valid JWT tokens with requests.
                        // Publicly accessible endpoints
                        .requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // Admin specific endpoints
                        .requestMatchers(HttpMethod.POST, "/api/admin/notices").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/admin/surveys").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Teacher specific endpoints
                        .requestMatchers(HttpMethod.POST, "/api/teachers/courses").hasRole("TEACHER")
                        .requestMatchers("/api/teachers/**").hasRole("TEACHER")

                        // Student specific endpoints
                        .requestMatchers(HttpMethod.POST, "/api/students/courses/{courseId}/enroll").hasRole("STUDENT")
                        .requestMatchers("/api/students/**").hasRole("STUDENT")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 프론트엔드 도메인 + 로컬호스트 허용
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "https://your-frontend-domain.com"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}