package ru.practicum.controller.public_api;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventState;
import ru.practicum.exception.ValidationException;
import ru.practicum.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Validated
public class PublicEventController {

    private final EventService eventService;

    @GetMapping("/events")
    public List<EventFullDto> getAllEvents(@RequestParam(required = false) String text,
                                           @RequestParam(required = false) List<Long> categories,
                                           @RequestParam(required = false) Boolean paid,
                                           @RequestParam(required = false) String rangeStart,
                                           @RequestParam(required = false) String rangeEnd,
                                           @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                           @RequestParam(required = false) String sort,
                                           @RequestParam(defaultValue = "0") Integer from,
                                           @RequestParam(defaultValue = "10") Integer size) {
        if (from == null) {
            from = 0;
        }
        if (from < 0) {
            throw new ValidationException("Parameter from must be greater than or equal to 0");
        }
        if (size == null) {
            size = 10;
        }
        if (size <= 0) {
            throw new ValidationException("Parameter size must be greater than 0");
        }
        if (rangeStart != null && rangeEnd != null) {
            try {
                LocalDateTime start = LocalDateTime.parse(rangeStart);
                LocalDateTime end = LocalDateTime.parse(rangeEnd);
                if (start.isAfter(end)) {
                    throw new ValidationException("Parameter rangeStart must be before rangeEnd");
                }
            } catch (Exception e) {
                throw new ValidationException("Invalid date format for rangeStart or rangeEnd");
            }
        }
        return eventService.getAllEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
    }

    @GetMapping("/events/{eventId}")
    public EventFullDto getEventById(@PathVariable @Positive Long eventId) {
        EventFullDto event = eventService.getEventById(eventId);
        if (event.getState() != EventState.PUBLISHED) {
            throw new ValidationException("Event is not published");
        }
        return event;
    }
}
