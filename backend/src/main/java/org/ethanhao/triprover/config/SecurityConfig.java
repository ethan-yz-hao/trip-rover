package org.ethanhao.triprover.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.ethanhao.triprover.filter.JwtAuthenticationTokenFilter;
import org.ethanhao.triprover.filter.OAuth2LoginSuccessHandler;
import org.ethanhao.triprover.handler.SecurityFilterExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity()
public class SecurityConfig {
    @Autowired
    AuthenticationConfiguration authenticationConfiguration;
    @Autowired
    JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
    @Autowired
    SecurityFilterExceptionHandler securityFilterExceptionHandler;
    @Autowired
    OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF protection
                .csrf(AbstractHttpConfigurer::disable)
                // Configure CORS
                .cors(withDefaults())
                // Set session creation policy to stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Configure authorization rules, specify the user/login path, allow anonymous access (cannot access after login), and other paths require authentication
                .authorizeHttpRequests(auth -> auth.requestMatchers("/api/user/login", "/api/user/register", "/oauth2/**", "/testCors")
                        .anonymous()
                        .anyRequest().authenticated())
                // OAuth2 login configuration
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureUrl("/oauth2/failure")
                        .permitAll()
                )
                // Add JWT authentication filter
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
                // Configure exception handling
                .exceptionHandling(exception -> 
                        exception.accessDeniedHandler(securityFilterExceptionHandler)
                        .authenticationEntryPoint(securityFilterExceptionHandler));

        return http.build();
    }
}