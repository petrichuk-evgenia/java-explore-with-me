package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.EndpointHitEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface StatisticsRepository extends JpaRepository<EndpointHitEntity, Long> {

    /**
     * Получить статистику по количеству хитов (без учета уникальности IP)
     */
    @Query("SELECT e.app as app, e.uri as uri, COUNT(e) as hits " +
            "FROM EndpointHitEntity e " +
            "WHERE e.timestamp BETWEEN :start AND :end " +
            "AND (:uris IS NULL OR e.uri IN :uris) " +
            "GROUP BY e.app, e.uri " +
            "ORDER BY hits DESC")
    List<Object[]> getStats(@Param("start") LocalDateTime start,
                            @Param("end") LocalDateTime end,
                            @Param("uris") List<String> uris);

    /**
     * Получить статистику по уникальным хитам (учитываются уникальные IP)
     */
    @Query("SELECT e.app as app, e.uri as uri, COUNT(DISTINCT e.ip) as hits " +
            "FROM EndpointHitEntity e " +
            "WHERE e.timestamp BETWEEN :start AND :end " +
            "AND (:uris IS NULL OR e.uri IN :uris) " +
            "GROUP BY e.app, e.uri " +
            "ORDER BY hits DESC")
    List<Object[]> getUniqueStats(@Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end,
                                  @Param("uris") List<String> uris);
}