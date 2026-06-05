package ru.practicum.controller.public_api;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventRatingDto;
import ru.practicum.dto.UserRatingDto;
import ru.practicum.service.ReactionService;

import java.util.List;

@RestController
@RequestMapping("/ratings")
@RequiredArgsConstructor
@Validated
public class PublicRatingController {

    private final ReactionService reactionService;

    @GetMapping("/events/{eventId}")
    public EventRatingDto getEventRating(@PathVariable @Positive Long eventId) {
        return reactionService.getEventRating(eventId);
    }

    @GetMapping("/events")
    public List<EventRatingDto> getEventsRating(@RequestParam List<Long> eventIds) {
        return reactionService.getEventsRating(eventIds);
    }

    @GetMapping("/users/{userId}")
    public UserRatingDto getUserRating(@PathVariable @Positive Long userId) {
        return reactionService.getUserRating(userId);
    }

    @GetMapping("/events/top")
    public List<EventRatingDto> getTopRatedEvents(@RequestParam(defaultValue = "10") @Positive Integer limit) {
        return reactionService.getTopRatedEvents(limit);
    }
}