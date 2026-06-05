package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ViewStats;
import ru.practicum.client.StatisticsClient;
import ru.practicum.dto.*;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.Location;
import ru.practicum.model.User;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.EventService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final StatisticsClient statisticsClient;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size) {
        log.info("Getting events for user: {}", userId);

        // Проверяем существование пользователя
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        PageRequest pageRequest = PageRequest.of(from / size, size);

        return eventRepository.findByInitiatorId(userId, pageRequest)
                .stream()
                .map(event -> {
                    EventShortDto dto = eventMapper.toShortDto(event);
                    // Получаем количество просмотров из сервиса статистики
                    Long views = getEventViews(event.getId());
                    dto.setViews(views);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto eventDto) {
        log.info("Creating event for user: {}", userId);

        // Проверяем существование пользователя
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        // Проверяем существование категории
        Category category = categoryRepository.findById(eventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category with id=" + eventDto.getCategory() + " was not found"));

        // Проверяем дату события (не раньше чем через 2 часа от текущего момента)
        LocalDateTime now = LocalDateTime.now();
        if (eventDto.getEventDate().isBefore(now.plusHours(2))) {
            throw new ConflictException("Event date must be at least 2 hours from now");
        }

        // Создаем событие
        Event event = eventMapper.toEntity(eventDto);
        event.setInitiator(user);
        event.setCategory(category);
        event.setCreatedOn(now);
        event.setState(EventState.PENDING);

        // Устанавливаем location
        Location location = new Location();
        location.setLat(eventDto.getLocation().getLat());
        location.setLon(eventDto.getLocation().getLon());
        event.setLocation(location);

        Event savedEvent = eventRepository.save(event);
        log.info("Event created successfully: id={}, userId={}", savedEvent.getId(), userId);

        return eventMapper.toFullDto(savedEvent);
    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        log.info("Getting event: userId={}, eventId={}", userId, eventId);

        // Проверяем существование пользователя
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        // Проверяем, что событие принадлежит пользователю
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " not found for user " + userId);
        }

        EventFullDto dto = eventMapper.toFullDto(event);
        Long views = getEventViews(eventId);
        dto.setViews(views);

        return dto;
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        log.info("Updating event: userId={}, eventId={}", userId, eventId);

        // Проверяем существование пользователя
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        // Проверяем, что событие принадлежит пользователю
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " not found for user " + userId);
        }

        // Проверяем, что событие можно редактировать (только PENDING или CANCELED)
        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

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
            LocalDateTime now = LocalDateTime.now();
            if (updateRequest.getEventDate().isBefore(now.plusHours(2))) {
                throw new ConflictException("Event date must be at least 2 hours from now");
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

        // Обрабатываем изменение состояния
        if (updateRequest.getStateAction() != null) {
            switch (updateRequest.getStateAction()) {
                case SEND_TO_REVIEW:
                    if (event.getState() != EventState.CANCELED) {
                        event.setState(EventState.PENDING);
                    }
                    break;
                case CANCEL_REVIEW:
                    event.setState(EventState.CANCELED);
                    break;
            }
        }

        Event updatedEvent = eventRepository.save(event);
        log.info("Event updated successfully: id={}, userId={}", updatedEvent.getId(), userId);

        return eventMapper.toFullDto(updatedEvent);
    }

    @Override
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               Boolean onlyAvailable, String sort, Integer from, Integer size) {
        log.info("Getting public events with filters: text={}, categories={}, paid={}, rangeStart={}, rangeEnd={}, onlyAvailable={}, sort={}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort);

        // Если диапазон дат не указан, используем текущее время как rangeStart
        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }

        int fromIndex = from != null ? from : 0;
        int sizeValue = size != null ? size : 10;
        Pageable pageable = PageRequest.of(fromIndex / sizeValue, sizeValue);

        Page<Event> events = eventRepository.findEventsPublic(
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, pageable);

        return events.stream()
                .map(event -> {
                    EventShortDto dto = eventMapper.toShortDto(event);
                    Long views = getEventViews(event.getId());
                    dto.setViews(views);
                    dto.setConfirmedRequests(Long.valueOf(eventRepository.countConfirmedRequests(event.getId())));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getPublicEvent(Long id, String clientIp) {
        log.info("Getting public event: id={}, clientIp={}", id, clientIp);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event with id=" + id + " was not found"));

        // Проверяем, что событие опубликовано
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event with id=" + id + " is not published");
        }

        EventFullDto dto = eventMapper.toFullDto(event);
        Long views = getEventViews(id);
        dto.setViews(views);
        dto.setConfirmedRequests(Long.valueOf(eventRepository.countConfirmedRequests(id)));

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