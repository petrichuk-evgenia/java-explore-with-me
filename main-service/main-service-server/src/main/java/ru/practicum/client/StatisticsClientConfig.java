package ru.practicum.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class StatisticsClientConfig {

    @Bean
    public RestTemplate statisticsRestTemplate(@Value("${statistics.server.url:http://localhost:9090}") String baseUrl,
                                               RestTemplateBuilder builder) {
        return builder
                .rootUri(baseUrl)
                .setConnectTimeout(java.time.Duration.ofSeconds(5))
                .setReadTimeout(java.time.Duration.ofSeconds(10))
                .build();
    }
}