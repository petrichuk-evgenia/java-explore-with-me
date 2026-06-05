package ru.practicum.service;

import ru.practicum.dto.EventRatingDto;
import ru.practicum.dto.ReactionDto;
import ru.practicum.dto.ReactionRequestDto;
import ru.practicum.dto.UserRatingDto;

import java.util.List;

public interface ReactionService {

    ReactionDto addReaction(Long userId, ReactionRequestDto request);

    ReactionDto updateReaction(Long userId, ReactionRequestDto request);

    void deleteReaction(Long userId, Long eventId);

    ReactionDto getUserReaction(Long userId, Long eventId);

    EventRatingDto getEventRating(Long eventId);

    List<EventRatingDto> getEventsRating(List<Long> eventIds);

    UserRatingDto getUserRating(Long userId);

    List<EventRatingDto> getTopRatedEvents(Integer limit);
}