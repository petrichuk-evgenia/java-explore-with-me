package ru.practicum.client;

import ru.practicum.EndpointHit;
import ru.practicum.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Интерфейс клиента для работы с сервисом статистики
 */
public interface StatisticsClient {

    /**
     * Сохранить информацию о запросе
     *
     * @param hit данные запроса
     */
    void saveHit(EndpointHit hit);

    /**
     * Получить статистику по запросам
     *
     * @param start  начало диапазона
     * @param end    конец диапазона
     * @param uris   список URI для фильтрации (опционально)
     * @param unique учитывать только уникальные IP
     * @return список статистики
     */
    List<ViewStats> getStats(LocalDateTime start, LocalDateTime end,
                             List<String> uris, boolean unique);
}