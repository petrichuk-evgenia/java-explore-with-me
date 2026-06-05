package ru.practicum.controller.private_api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.ReactionDto;
import ru.practicum.dto.ReactionRequestDto;
import ru.practicum.service.ReactionService;

@RestController
@RequestMapping("/users/{userId}/reactions")
@RequiredArgsConstructor
@Validated
public class PrivateReactionController {

    private final ReactionService reactionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReactionDto addReaction(@PathVariable @Positive Long userId,
                                   @Valid @RequestBody ReactionRequestDto request) {
        return reactionService.addReaction(userId, request);
    }

    @PutMapping
    public ReactionDto updateReaction(@PathVariable @Positive Long userId,
                                      @Valid @RequestBody ReactionRequestDto request) {
        return reactionService.updateReaction(userId, request);
    }

    @DeleteMapping("/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReaction(@PathVariable @Positive Long userId,
                               @PathVariable @Positive Long eventId) {
        reactionService.deleteReaction(userId, eventId);
    }

    @GetMapping("/{eventId}")
    public ReactionDto getUserReaction(@PathVariable @Positive Long userId,
                                       @PathVariable @Positive Long eventId) {
        return reactionService.getUserReaction(userId, eventId);
    }
}