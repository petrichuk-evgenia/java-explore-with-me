package ru.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.dto.EventState;
import ru.practicum.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findByInitiatorId(Long userId, Pageable pageable);

    boolean existsByCategoryId(Long categoryId);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    @Query("""
            SELECT DISTINCT e FROM Event e
            LEFT JOIN e.category c
            LEFT JOIN e.initiator u
            WHERE (:text is null or LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%'))
                   or LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%')))
            AND (:categories is null or c.id in :categories)
            AND (:paid is null or e.paid = :paid)
            AND (:rangeStart is null or e.eventDate >= :rangeStart)
            AND (:rangeEnd is null or e.eventDate <= :rangeEnd)
            AND (:onlyAvailable = false or
                 (e.participantLimit = 0 or
                  (SELECT COUNT(r) FROM ParticipationRequest r WHERE r.event = e AND r.status = 'CONFIRMED') < e.participantLimit))
            AND e.state = 'PUBLISHED'
            ORDER BY
              CASE WHEN :sort = 'EVENT_DATE' THEN e.eventDate END DESC,
              CASE WHEN :sort = 'VIEWS' THEN e.views END DESC,
              e.eventDate DESC
            """)
    Page<Event> findEventsPublic(String text, List<Long> categories, Boolean paid,
                                 LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                 Boolean onlyAvailable, String sort, Pageable pageable);

    @Query("""
            SELECT e FROM Event e
            LEFT JOIN e.category c
            WHERE (:users is null or e.initiator.id in :users)
            AND (:states is null or e.state in :states)
            AND (:categories is null or c.id in :categories)
            AND (:rangeStart is null or e.eventDate >= :rangeStart)
            AND (:rangeEnd is null or e.eventDate <= :rangeEnd)
            ORDER BY e.eventDate DESC
            """)
    Page<Event> findEventsByAdminCriteria(List<Long> users, List<EventState> states,
                                          List<Long> categories, LocalDateTime rangeStart,
                                          LocalDateTime rangeEnd, Pageable pageable);

    @Query("SELECT COUNT(r) FROM ParticipationRequest r WHERE r.event.id = :eventId AND r.status = 'CONFIRMED'")
    int countConfirmedRequests(Long eventId);
}
