package ru.practicum.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.practicum.EndpointHit;
import ru.practicum.client.StatisticsClient;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class StatisticsFilter implements Filter {

    private final StatisticsClient statisticsClient;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpServletRequest) {
            String uri = httpServletRequest.getRequestURI();
            String method = httpServletRequest.getMethod();

            log.debug("Processing request: {} {}", method, uri);

            // Сохраняем статистику только для GET запросов к определенным эндпоинтам
            if ("GET".equalsIgnoreCase(method) && isTrackedUri(uri)) {
                log.debug("Saving statistics for URI: {}", uri);
                try {
                    EndpointHit hit = new EndpointHit();
                    hit.setApp("ewm-main-service");
                    hit.setUri(uri);
                    hit.setIp(httpServletRequest.getRemoteAddr());
                    hit.setTimestamp(LocalDateTime.now());

                    log.debug("Calling statisticsClient.saveHit for URI: {}", uri);
                    statisticsClient.saveHit(hit);
                    log.debug("Statistics saved successfully for URI: {}", uri);
                } catch (Exception e) {
                    log.warn("Failed to save statistics for URI: {} (non-critical)", uri, e);
                }
            } else {
                log.debug("Skipping statistics for URI: {} (method={})", uri, method);
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isTrackedUri(String uri) {
        // Отслеживаем только определенные эндпоинты
        return uri != null && (
                uri.equals("/events") ||
                uri.startsWith("/events/") ||
                uri.equals("/categories") ||
                uri.startsWith("/categories/") ||
                uri.equals("/compilations") ||
                uri.startsWith("/compilations/")
        );
    }
}
