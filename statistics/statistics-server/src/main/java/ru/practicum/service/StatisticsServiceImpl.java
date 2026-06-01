package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHit;
import ru.practicum.ViewStats;
import ru.practicum.mapper.StatisticsMapper;
import ru.practicum.repository.StatisticsRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsServiceImpl implements StatisticsService {

    private final StatisticsRepository statisticsRepository;
    private final StatisticsMapper mapper;

    @Override
    @Transactional
    public void saveHit(EndpointHit hit) {
        log.debug("Saving hit for URI: {}", hit.getUri());
        statisticsRepository.save(mapper.toEntity(hit));
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end,
                                    List<String> uris, boolean unique) {
        log.debug("Getting stats from {} to {}, uris: {}, unique: {}", start, end, uris, unique);

        List<Object[]> results;

        if (unique) {
            results = statisticsRepository.getUniqueStats(start, end, uris);
        } else {
            results = statisticsRepository.getStats(start, end, uris);
        }

        return results.stream()
                .map(row -> ViewStats.builder()
                        .app((String) row[0])
                        .uri((String) row[1])
                        .hits((Long) row[2])
                        .build())
                .collect(Collectors.toList());
    }
}