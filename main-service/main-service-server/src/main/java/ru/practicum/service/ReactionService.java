package ru.practicum.service;

import ru.practicum.dto.AddReactionRequest;
import ru.practicum.dto.EventRatingDto;
import ru.practicum.dto.ReactionDto;
import ru.practicum.dto.UpdateReactionRequest;
import ru.practicum.dto.UserRatingDto;

public interface ReactionService {
    ReactionDto addReaction(Long userId, AddReactionRequest request);

    ReactionDto updateReaction(Long userId, UpdateReactionRequest request);

    ReactionDto getReaction(Long userId, Long eventId);

    void deleteReaction(Long userId, Long eventId);

    EventRatingDto getEventRating(Long eventId);

    UserRatingDto getUserRating(Long userId);
}
