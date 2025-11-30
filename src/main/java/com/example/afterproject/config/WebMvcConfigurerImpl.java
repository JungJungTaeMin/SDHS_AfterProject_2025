package com.example.afterproject.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfigurerImpl implements WebMvcConfigurer {

    // SecurityConfig에서 CORS를 처리하므로 여기는 비워두거나
    // Security를 안 탈 경우를 대비해 남겨둘 수도 있습니다.
    // 일단 SecurityConfig 코드를 분리하는 것이 핵심입니다.

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true);
    }
}