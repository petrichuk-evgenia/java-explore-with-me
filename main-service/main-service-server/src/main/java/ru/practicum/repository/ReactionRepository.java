package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.Reaction;

import java.util.List;
import java.util.Optional;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    @Query("SELECT r FROM Reaction r WHERE r.user.id = :userId AND r.event.id = :eventId")
    Optional<Reaction> findByUserIdAndEventIdCustom(@Param("userId") Long userId, @Param("eventId") Long eventId);

    @Query("SELECT COUNT(r) FROM Reaction r WHERE r.event.id = :eventId AND LOWER(r.reactionType) = 'like'")
    Long countByEventIdAndReactionTypeLike(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(r) FROM Reaction r WHERE r.event.id = :eventId AND LOWER(r.reactionType) = 'dislike'")
    Long countByEventIdAndReactionTypeDislike(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(r) FROM Reaction r WHERE r.user.id = :userId AND LOWER(r.reactionType) = 'like'")
    Long countByUserIdAndReactionTypeLike(@Param("userId") Long userId);

    @Query("SELECT COUNT(r) FROM Reaction r WHERE r.user.id = :userId AND LOWER(r.reactionType) = 'dislike'")
    Long countByUserIdAndReactionTypeDislike(@Param("userId") Long userId);

    @Query("SELECT COUNT(r) FROM Reaction r JOIN r.event e WHERE e.initiator.id = :userId AND LOWER(r.reactionType) = 'like'")
    Long countByEventInitiatorIdAndReactionTypeLike(@Param("userId") Long userId);

    @Query("SELECT COUNT(r) FROM Reaction r JOIN r.event e WHERE e.initiator.id = :userId AND LOWER(r.reactionType) = 'dislike'")
    Long countByEventInitiatorIdAndReactionTypeDislike(@Param("userId") Long userId);
}
