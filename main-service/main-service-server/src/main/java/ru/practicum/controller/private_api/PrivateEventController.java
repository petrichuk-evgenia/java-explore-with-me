package ru.practicum.controller.private_api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.*;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Validated
public class PrivateEventController {
    private final ru.practicum.service.EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(@PathVariable @NotNull Long userId,
                                 @Valid @RequestBody NewEventDto dto) {
        return eventService.addEvent(userId, dto);
    }

    @GetMapping
    public List<EventShortDto> getUserEvents(@PathVariable @NotNull Long userId,
                                             @RequestParam(defaultValue = "0") Integer from,
                                             @RequestParam(defaultValue = "10") Integer size) {
        return eventService.getUserEvents(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEvent(@PathVariable @NotNull Long userId,
                                 @PathVariable @NotNull Long eventId) {
        return eventService.getEvent(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateUserEvent(@PathVariable @NotNull Long userId,
                                        @PathVariable @NotNull Long eventId,
                                        @Valid @RequestBody UpdateEventUserRequest dto) {
        return eventService.updateUserEvent(userId, eventId, dto);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateEventRequestStatus(@PathVariable @NotNull Long userId,
                                                                   @PathVariable @NotNull Long eventId,
                                                                   @Valid @RequestBody EventRequestStatusUpdateRequest request) {
        return eventService.updateEventRequestStatus(userId, eventId, request);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getEventRequests(@PathVariable @NotNull Long userId,
                                                          @PathVariable @NotNull Long eventId) {
        return eventService.getEventRequests(userId, eventId);
    }
}
