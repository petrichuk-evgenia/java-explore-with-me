package ru.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findByInitiatorId(Long userId, org.springframework.data.domain.Pageable pageable);

    boolean existsByCategoryId(Long categoryId);
}