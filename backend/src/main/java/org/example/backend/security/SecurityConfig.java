package org.example.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @SuppressWarnings("java:S4502")
    @Bean
    public SecurityFilterChain customSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        // CSRF is disabled for API endpoints because authentication is handled via OAuth2 login with secure cookies
                        .ignoringRequestMatchers("/api/**")
                        .csrfTokenRepository(new CookieCsrfTokenRepository())
                )
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.GET,"/api/comment/{cityName}").authenticated()
                        .requestMatchers(HttpMethod.GET,"/api/comment/getId/{id}").authenticated()
                        .requestMatchers(HttpMethod.POST,"/api/addcomment").authenticated()
                        .requestMatchers(HttpMethod.PUT,"/api/comment/{id}").authenticated()
                        .requestMatchers(HttpMethod.DELETE,"/api/comment/{id}").authenticated()
                        .requestMatchers(HttpMethod.GET,"/api/favorites").authenticated()
                        .requestMatchers(HttpMethod.POST,"/api/addToFavorites").authenticated()
                        .requestMatchers(HttpMethod.DELETE,"/api/deleteFromFav/{cityName}").authenticated()
                        .requestMatchers("/api/auth/me").authenticated()
                        .anyRequest().permitAll()
                )
                .logout(l -> l.logoutSuccessUrl("/"))
                .oauth2Login(o -> o.defaultSuccessUrl("/search", true));

        return http.build();
    }
}
