package ru.practicum.controller.private_api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.AddReactionRequest;
import ru.practicum.dto.ReactionDto;
import ru.practicum.dto.UpdateReactionRequest;
import ru.practicum.service.ReactionService;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
@Validated
public class ReactionController {
    private final ReactionService reactionService;

    @PostMapping("/reactions")
    @ResponseStatus(HttpStatus.CREATED)
    public ReactionDto addReaction(@PathVariable @NotNull Long userId,
                                   @Valid @RequestBody AddReactionRequest request) {
        return reactionService.addReaction(userId, request);
    }

    @PutMapping("/reactions")
    public ReactionDto updateReaction(@PathVariable @NotNull Long userId,
                                      @Valid @RequestBody UpdateReactionRequest request) {
        return reactionService.updateReaction(userId, request);
    }

    @GetMapping("/reactions/{eventId}")
    public ReactionDto getReaction(@PathVariable @NotNull Long userId,
                                   @PathVariable @NotNull Long eventId) {
        return reactionService.getReaction(userId, eventId);
    }

    @DeleteMapping("/reactions/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReaction(@PathVariable @NotNull Long userId,
                               @PathVariable @NotNull Long eventId) {
        reactionService.deleteReaction(userId, eventId);
    }
}
