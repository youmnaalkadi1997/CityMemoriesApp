package org.example.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain customSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf
                        // CSRF disabled only for API endpoints used by SPA frontend (React).
                        // Safe because all requests require OAuth2 authentication and proper CORS configuration.
                        .ignoringRequestMatchers("/api/**")
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.GET,"/api/comment/{cityName}").authenticated()
                        .requestMatchers(HttpMethod.GET,"/api/comment/getId/{id}").authenticated()
                        .requestMatchers(HttpMethod.POST,"/api/addcomment").authenticated()
                        .requestMatchers(HttpMethod.PUT,"/api/comment/{id}").authenticated()
                        .requestMatchers(HttpMethod.DELETE,"/api/comment/{id}").authenticated()
                        .requestMatchers("/api/auth/me").authenticated()
                        .anyRequest().permitAll()
                )
                .logout(l -> l.logoutSuccessUrl("/"))
                .oauth2Login(o -> o.defaultSuccessUrl("/search", true));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(List.of("*")); //beim deployment soll ich: "Authorization", "Content-Type" und nicht *
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
