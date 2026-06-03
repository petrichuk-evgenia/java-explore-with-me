package ru.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Event;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findByInitiatorId(Long userId, Pageable pageable);

    boolean existsByCategoryId(Long categoryId);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);
}
