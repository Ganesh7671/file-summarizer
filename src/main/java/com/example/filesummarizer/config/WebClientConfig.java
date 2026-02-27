package com.example.filesummarizer.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
public class WebClientConfig {


    @Value("${app.ollama.base-url}")
    private String ollamaBaseUrl;


    @Bean
    public WebClient ollamaWebClient() {
        return WebClient.builder()
                .baseUrl(ollamaBaseUrl)
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        // ✅ FIX: Increase to 100MB — llava sends/receives large base64 payloads
                        .maxInMemorySize(100 * 1024 * 1024))
                .build();
    }
}


