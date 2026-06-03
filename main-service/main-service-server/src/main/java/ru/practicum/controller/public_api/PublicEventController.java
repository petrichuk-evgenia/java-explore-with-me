package ru.practicum.controller.public_api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class PublicEventController {
    private final ru.practicum.service.EventService eventService;

    @GetMapping
    public List<EventShortDto> getEvents(@RequestParam(required = false) String text,
                                         @RequestParam(required = false) List<Long> categories,
                                         @RequestParam(required = false) Boolean paid,
                                         @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                         @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                         @RequestParam(defaultValue = "true") Boolean onlyAvailable,
                                         @RequestParam(required = false) String sort,
                                         @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                         @RequestParam(defaultValue = "10") Integer size) {
        // Валидация параметров
        if (categories != null && !categories.isEmpty()) {
            for (Long category : categories) {
                if (category == null || category <= 0) {
                    log.error("Invalid category ID: {}. Category ID must be greater than 0", category);
                    throw new ValidationException("Category ID must be greater than 0");
                }
            }
        }

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            log.error("Invalid date range: rangeStart ({}) is after rangeEnd ({})", rangeStart, rangeEnd);
            throw new ValidationException("rangeStart cannot be after rangeEnd");
        }

        return eventService.getPublicEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
    }

    @GetMapping("/{id}")
    public EventFullDto getEvent(@PathVariable Long id, HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        return eventService.getPublicEvent(id, clientIp);
    }
}
