package ru.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.NewEventDto;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.dto.UpdateEventUserRequest;
import ru.practicum.exception.ValidationException;
import ru.practicum.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
@Validated
public class UserEventsController {

    private final EventService eventService;

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable @Positive Long userId,
                                    @Valid @RequestBody NewEventDto eventDto) {
        if (eventDto.getAnnotation() == null || eventDto.getAnnotation().isBlank()) {
            throw new ValidationException("Annotation is required");
        }
        if (eventDto.getDescription() == null || eventDto.getDescription().isBlank()) {
            throw new ValidationException("Description is required");
        }
        return eventService.createEvent(userId, eventDto);
    }

    @GetMapping("/events")
    public List<EventFullDto> getUserEvents(@PathVariable @Positive Long userId,
                                            @RequestParam(defaultValue = "0") Integer from,
                                            @RequestParam(defaultValue = "10") Integer size) {
        if (from < 0) {
            throw new ValidationException("Parameter from must be greater than or equal to 0");
        }
        if (size <= 0) {
            throw new ValidationException("Parameter size must be greater than 0");
        }
        return eventService.getUserEvents(userId, from, size);
    }

    @PatchMapping("/events/{eventId}")
    public EventFullDto updateUserEvent(@PathVariable @Positive Long userId,
                                        @PathVariable @Positive Long eventId,
                                        @Valid @RequestBody UpdateEventUserRequest eventDto) {
        return eventService.updateUserEvent(userId, eventId, eventDto);
    }

    @PostMapping("/events/{eventId}/publish")
    public EventFullDto publishEvent(@PathVariable @Positive Long userId,
                                     @PathVariable @Positive Long eventId) {
        return eventService.publishEvent(userId, eventId);
    }

    @PostMapping("/events/{eventId}/cancel")
    public EventFullDto cancelEvent(@PathVariable @Positive Long userId,
                                    @PathVariable @Positive Long eventId) {
        return eventService.cancelEvent(userId, eventId);
    }

    @GetMapping("/requests")
    public List<ParticipationRequestDto> getUserRequests(@PathVariable @Positive Long userId) {
        return eventService.getUserRequests(userId);
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable @Positive Long userId,
                                                 @RequestParam(required = false) Long eventId) {
        if (eventId == null) {
            throw new ValidationException("Parameter eventId is required");
        }
        return eventService.createRequest(userId, eventId);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable @Positive Long userId,
                                                 @PathVariable @Positive Long requestId) {
        return eventService.cancelRequest(userId, requestId);
    }
}