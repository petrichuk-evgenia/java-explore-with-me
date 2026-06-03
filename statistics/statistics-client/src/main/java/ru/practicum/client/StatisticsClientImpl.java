package ru.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.EndpointHit;
import ru.practicum.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class StatisticsClientImpl implements StatisticsClient {

    private final RestTemplate restTemplate;
    private final String serverUrl;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public StatisticsClientImpl(@Value("${statistics.server.url:http://localhost:9090}") String serverUrl,
                                RestTemplateBuilder restTemplateBuilder) {
        this.serverUrl = serverUrl;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(java.time.Duration.ofSeconds(5))
                .setReadTimeout(java.time.Duration.ofSeconds(10))
                .build();
    }

    @Override
    public void saveHit(EndpointHit hit) {
        String url = serverUrl + "/hit";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<EndpointHit> requestEntity = new HttpEntity<>(hit, headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    Void.class
            );

            if (response.getStatusCode() == HttpStatus.CREATED) {
                log.info("Hit saved successfully for URI: {}, IP: {}", hit.getUri(), hit.getIp());
            } else {
                log.warn("Unexpected response status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Failed to save hit for URI: {}, IP: {}", hit.getUri(), hit.getIp(), e);
            throw new RuntimeException("Failed to save statistics hit", e);
        }
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end,
                                    List<String> uris, boolean unique) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverUrl + "/stats")
                .queryParam("start", start.format(formatter))
                .queryParam("end", end.format(formatter))
                .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            uris.forEach(uri -> builder.queryParam("uris", uri));
        }

        String url = builder.build().encode().toUriString();
        log.debug("Requesting stats from URL: {}", url);

        try {
            ResponseEntity<List<ViewStats>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<ViewStats>>() {
                    }
            );

            log.info("Retrieved {} stats records", response.getBody() != null ? response.getBody().size() : 0);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to get stats for period: {} to {}", start, end, e);
            throw new RuntimeException("Failed to retrieve statistics", e);
        }
    }
}
