package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ViewStats;
import ru.practicum.client.StatisticsClient;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventState;
import ru.practicum.dto.UpdateEventAdminRequest;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.Location;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.AdminEventService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminEventServiceImpl implements AdminEventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final StatisticsClient statisticsClient;

    @Override
    public List<EventFullDto> getEvents(List<Long> users, List<String> states, List<Long> categories,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
        log.info("Admin getting events with filters: users={}, states={}, categories={}, rangeStart={}, rangeEnd={}",
                users, states, categories, rangeStart, rangeEnd);

        Pageable pageable = PageRequest.of(from / size, size);

        // Преобразуем строковые состояния в enum
        List<EventState> eventStates = null;
        if (states != null && !states.isEmpty()) {
            eventStates = states.stream()
                    .map(EventState::valueOf)
                    .collect(Collectors.toList());
        }

        // Если диапазон дат не указан, используем текущее время как rangeStart
        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }

        Page<Event> eventsPage = eventRepository.findEventsByAdminCriteria(
                users, eventStates, categories, rangeStart, rangeEnd, pageable);

        return eventsPage.stream()
                .map(event -> {
                    EventFullDto dto = eventMapper.toFullDto(event);
                    Long views = getEventViews(event.getId());
                    dto.setViews(views);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateRequest) {
        log.info("Admin updating event: id={}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        // Обновляем поля
        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }

        if (updateRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id=" + updateRequest.getCategory() + " was not found"));
            event.setCategory(category);
        }

        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }

        if (updateRequest.getEventDate() != null) {
            // Проверяем, что дата события не раньше чем через час от текущего момента
            LocalDateTime now = LocalDateTime.now();
            if (updateRequest.getEventDate().isBefore(now.plusHours(1))) {
                throw new ConflictException("Event date must be at least 1 hour from now");
            }
            event.setEventDate(updateRequest.getEventDate());
        }

        if (updateRequest.getLocation() != null) {
            Location location = new Location();
            location.setLat(updateRequest.getLocation().getLat());
            location.setLon(updateRequest.getLocation().getLon());
            event.setLocation(location);
        }

        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }

        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }

        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }

        if (updateRequest.getTitle() != null) {
            event.setTitle(updateRequest.getTitle());
        }

        // Обрабатываем изменение состояния (публикация или отклонение)
        if (updateRequest.getStateAction() != null) {
            switch (updateRequest.getStateAction()) {
                case PUBLISH_EVENT:
                    if (event.getState() != EventState.PENDING) {
                        throw new ConflictException("Cannot publish the event because it's not in the right state: " + event.getState());
                    }
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case REJECT_EVENT:
                    if (event.getState() == EventState.PUBLISHED) {
                        throw new ConflictException("Cannot reject the event because it's already published");
                    }
                    event.setState(EventState.CANCELED);
                    break;
            }
        }

        Event updatedEvent = eventRepository.save(event);
        log.info("Event updated successfully by admin: id={}", updatedEvent.getId());

        EventFullDto dto = eventMapper.toFullDto(updatedEvent);
        Long views = getEventViews(eventId);
        dto.setViews(views);

        return dto;
    }

    private Long getEventViews(Long eventId) {
        try {
            LocalDateTime start = LocalDateTime.now().minusYears(1);
            LocalDateTime end = LocalDateTime.now();

            List<ViewStats> stats = statisticsClient.getStats(start, end, List.of("/events/" + eventId), true);

            if (stats != null && !stats.isEmpty()) {
                return stats.get(0).getHits();
            }
        } catch (Exception e) {
            log.warn("Failed to get views for event {}: {}", eventId, e.getMessage());
        }
        return 0L;
    }
}