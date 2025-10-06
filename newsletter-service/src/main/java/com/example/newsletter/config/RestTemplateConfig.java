package com.example.newsletter.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        // RestTemplate will be automatically instrumented for distributed tracing
        // by Micrometer Tracing auto-configuration
        return builder.build();
    }
}
