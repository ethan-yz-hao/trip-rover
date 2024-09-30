package org.ethanhao.triprover.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cors.allowed.origins:http://localhost:3000}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping("/**")
                // Set allowed origins from environment variable
                .allowedOrigins(allowedOrigins)
                // Allow cookies
                .allowCredentials(true)
                // Set allowed request methods
                .allowedMethods("GET", "POST", "DELETE", "PUT", "OPTIONS")
                // Set allowed headers
                .allowedHeaders("*")
                // Cross-domain allow time
                .maxAge(3600);
    }
}
