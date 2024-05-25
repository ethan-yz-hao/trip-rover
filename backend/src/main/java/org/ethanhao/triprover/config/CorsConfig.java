package org.ethanhao.triprover.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping("/**")
                // set allowed origin
                .allowedOriginPatterns("*")
                // allow cookie
                .allowCredentials(true)
                // set allowed request methods
                .allowedMethods("GET", "POST", "DELETE", "PUT")
                // set allowed header properties
                .allowedHeaders("*")
                // cross-domain allow time
                .maxAge(3600);
    }
}