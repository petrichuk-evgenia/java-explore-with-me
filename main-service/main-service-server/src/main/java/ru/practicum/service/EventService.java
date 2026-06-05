package ru.practicum.service;

import ru.practicum.dto.*;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventFullDto addEvent(Long userId, NewEventDto dto);

    EventFullDto getEvent(Long userId, Long eventId);

    List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size);

    EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest dto);

    EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest dto);

    List<EventFullDto> getEventsAdmin(List<Long> users, List<String> states, List<Long> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);

    List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Integer from, Integer size);

    EventFullDto getPublicEvent(Long id, String clientIp);

    EventRequestStatusUpdateResult updateEventRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest request);

    List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId);
}
