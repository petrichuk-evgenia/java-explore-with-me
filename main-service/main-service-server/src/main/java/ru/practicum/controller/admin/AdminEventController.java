package ru.practicum.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.UpdateEventAdminRequest;
import ru.practicum.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Slf4j
public class AdminEventController {
    private final ru.practicum.service.EventService eventService;

    @GetMapping
    public List<EventFullDto> getEventsAdmin(@RequestParam(required = false) List<Long> users,
                                             @RequestParam(required = false) List<String> states,
                                             @RequestParam(required = false) List<Long> categories,
                                             @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                             @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                             @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                             @RequestParam(defaultValue = "10") Integer size) {
        // Валидация параметров
        if (users != null && !users.isEmpty()) {
            for (Long user : users) {
                if (user == null || user <= 0) {
                    log.error("Invalid user ID: {}. User ID must be greater than 0", user);
                    throw new ValidationException("User ID must be greater than 0");
                }
            }
        }

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

        return eventService.getEventsAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventAdmin(@PathVariable Long eventId,
                                         @Valid @RequestBody UpdateEventAdminRequest dto) {
        return eventService.updateEventAdmin(eventId, dto);
    }
}
