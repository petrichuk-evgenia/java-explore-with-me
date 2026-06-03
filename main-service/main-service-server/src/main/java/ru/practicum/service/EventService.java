package ru.practicum.service;

import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.NewEventDto;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.dto.UpdateEventUserRequest;

import java.util.List;

public interface EventService {
    EventFullDto createEvent(Long userId, NewEventDto eventDto);

    List<EventFullDto> getUserEvents(Long userId, Integer from, Integer size);

    EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest eventDto);

    EventFullDto publishEvent(Long userId, Long eventId);

    EventFullDto cancelEvent(Long userId, Long eventId);

    List<EventFullDto> getAllEvents(String text, List<Long> categories, Boolean paid,
                                    String rangeStart, String rangeEnd, Boolean onlyAvailable,
                                    String sort, Integer from, Integer size);

    EventFullDto getEventById(Long eventId);

    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto createRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);
}
