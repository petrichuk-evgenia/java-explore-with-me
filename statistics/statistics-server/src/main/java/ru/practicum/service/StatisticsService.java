package ru.practicum.service;

import ru.practicum.EndpointHit;
import ru.practicum.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис для работы со статистикой
 */
public interface StatisticsService {

    /**
     * Сохранить информацию о запросе
     *
     * @param hit данные запроса
     */
    void saveHit(EndpointHit hit);

    /**
     * Получить статистику по запросам за период
     *
     * @param start  начало периода
     * @param end    конец периода
     * @param uris   список URI для фильтрации
     * @param unique флаг уникальности по IP
     * @return список статистики
     */
    List<ViewStats> getStats(LocalDateTime start, LocalDateTime end,
                             List<String> uris, boolean unique);
}