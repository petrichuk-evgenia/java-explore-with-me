package ru.practicum.controller.public_api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventRatingDto;
import ru.practicum.dto.UserRatingDto;
import ru.practicum.service.ReactionService;

@RestController
@RequestMapping("/ratings")
@RequiredArgsConstructor
public class RatingController {
    private final ReactionService reactionService;

    @GetMapping("/events/{eventId}")
    public EventRatingDto getEventRating(@PathVariable Long eventId) {
        return reactionService.getEventRating(eventId);
    }

    @GetMapping("/users/{userId}")
    public UserRatingDto getUserRating(@PathVariable Long userId) {
        return reactionService.getUserRating(userId);
    }
}
