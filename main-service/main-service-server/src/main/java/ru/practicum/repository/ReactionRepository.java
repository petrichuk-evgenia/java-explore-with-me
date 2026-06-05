package ru.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.dto.ReactionType;
import ru.practicum.model.Reaction;
import ru.practicum.model.ReactionId;

import java.util.List;
import java.util.Optional;

public interface ReactionRepository extends JpaRepository<Reaction, ReactionId> {
    Optional<Reaction> findById_EventIdAndId_UserId(Long eventId, Long userId);

    @Query("SELECT r.id.eventId, COUNT(r) FROM Reaction r " +
            "WHERE r.id.eventId IN :eventIds " +
            "GROUP BY r.id.eventId")
    List<Object[]> countReactionsByEventIds(@Param("eventIds") List<Long> eventIds);

    @Query("SELECT r.id.eventId, COUNT(r) FROM Reaction r " +
            "WHERE r.id.eventId IN :eventIds AND r.reactionType = :type " +
            "GROUP BY r.id.eventId")
    List<Object[]> countReactionsByEventIdsAndType(@Param("eventIds") List<Long> eventIds,
                                                   @Param("type") ReactionType type);

    // Исправленный метод - используем COUNT с условием
    @Query("SELECT COUNT(r) FROM Reaction r " +
            "WHERE r.id.userId = :userId AND r.reactionType = :type")
    Long countByUserIdAndReactionType(@Param("userId") Long userId,
                                      @Param("type") ReactionType type);

    @Query("SELECT r.event.id, " +
            "COUNT(r) as total, " +
            "SUM(CASE WHEN r.reactionType = 'LIKE' THEN 1 ELSE 0 END) as likes, " +
            "SUM(CASE WHEN r.reactionType = 'DISLIKE' THEN 1 ELSE 0 END) as dislikes " +
            "FROM Reaction r " +
            "WHERE r.event.id = :eventId " +
            "GROUP BY r.event.id")
    List<Object[]> getEventRating(@Param("eventId") Long eventId);

    @Query("SELECT r.event.id, " +
            "COUNT(r) as total, " +
            "SUM(CASE WHEN r.reactionType = 'LIKE' THEN 1 ELSE 0 END) as likes, " +
            "SUM(CASE WHEN r.reactionType = 'DISLIKE' THEN 1 ELSE 0 END) as dislikes " +
            "FROM Reaction r " +
            "WHERE r.event.id IN :eventIds " +
            "GROUP BY r.event.id")
    List<Object[]> getEventsRating(@Param("eventIds") List<Long> eventIds);

    Page<Reaction> findById_UserIdOrderByCreatedDesc(Long userId, Pageable pageable);

    @Query("SELECT COUNT(r) FROM Reaction r WHERE r.event.id = :eventId AND r.reactionType = 'LIKE'")
    Long countLikesByEventId(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(r) FROM Reaction r WHERE r.event.id = :eventId AND r.reactionType = 'DISLIKE'")
    Long countDislikesByEventId(@Param("eventId") Long eventId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN TRUE ELSE FALSE END " +
            "FROM Reaction r WHERE r.event.id = :eventId AND r.id.userId = :userId")
    boolean existsByEventIdAndUserId(@Param("eventId") Long eventId, @Param("userId") Long userId);
}
