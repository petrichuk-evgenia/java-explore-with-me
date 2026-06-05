package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EventRatingDto;
import ru.practicum.dto.ReactionDto;
import ru.practicum.dto.ReactionRequestDto;
import ru.practicum.dto.UserRatingDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.ReactionMapper;
import ru.practicum.model.Event;
import ru.practicum.model.Reaction;
import ru.practicum.model.ReactionId;
import ru.practicum.model.User;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.ReactionRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.ReactionService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.practicum.dto.ReactionType.DISLIKE;
import static ru.practicum.dto.ReactionType.LIKE;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReactionServiceImpl implements ReactionService {

    private final ReactionRepository reactionRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ReactionMapper reactionMapper;

    @Override
    @Transactional
    public ReactionDto addReaction(Long userId, ReactionRequestDto request) {
        log.info("Adding reaction: userId={}, eventId={}, type={}",
                userId, request.getEventId(), request.getReactionType());

        // Проверяем существование пользователя
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        // Проверяем существование события
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new NotFoundException("Event with id=" + request.getEventId() + " was not found"));

        // Проверяем, что событие опубликовано
        if (!"PUBLISHED".equals(event.getState().toString())) {
            throw new ConflictException("Cannot react to unpublished event");
        }

        // Проверяем, не оставил ли уже пользователь реакцию
        if (reactionRepository.findById_EventIdAndId_UserId(request.getEventId(), userId).isPresent()) {
            throw new ConflictException("User already reacted to this event. Use PUT to update reaction.");
        }

        // Создаем новую реакцию
        Reaction reaction = Reaction.builder()
                .id(new ReactionId(request.getEventId(), userId))
                .event(event)
                .user(user)
                .reactionType(request.getReactionType())
                .created(LocalDateTime.now())
                .build();

        Reaction savedReaction = reactionRepository.save(reaction);
        log.info("Reaction added successfully: userId={}, eventId={}", userId, request.getEventId());

        return reactionMapper.toDto(savedReaction);
    }

    @Override
    @Transactional
    public ReactionDto updateReaction(Long userId, ReactionRequestDto request) {
        log.info("Updating reaction: userId={}, eventId={}, newType={}",
                userId, request.getEventId(), request.getReactionType());

        // Находим существующую реакцию
        Reaction reaction = reactionRepository
                .findById_EventIdAndId_UserId(request.getEventId(), userId)
                .orElseThrow(() -> new NotFoundException("Reaction not found for userId=" + userId +
                        " and eventId=" + request.getEventId()));

        // Обновляем тип реакции
        reaction.setReactionType(request.getReactionType());

        Reaction updatedReaction = reactionRepository.save(reaction);
        log.info("Reaction updated successfully: userId={}, eventId={}", userId, request.getEventId());

        return reactionMapper.toDto(updatedReaction);
    }

    @Override
    @Transactional
    public void deleteReaction(Long userId, Long eventId) {
        log.info("Deleting reaction: userId={}, eventId={}", userId, eventId);

        Reaction reaction = reactionRepository
                .findById_EventIdAndId_UserId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Reaction not found for userId=" + userId +
                        " and eventId=" + eventId));

        reactionRepository.delete(reaction);
        log.info("Reaction deleted successfully: userId={}, eventId={}", userId, eventId);
    }

    @Override
    public ReactionDto getUserReaction(Long userId, Long eventId) {
        log.debug("Getting user reaction: userId={}, eventId={}", userId, eventId);

        return reactionRepository.findById_EventIdAndId_UserId(eventId, userId)
                .map(reactionMapper::toDto)
                .orElse(null);
    }

    @Override
    public EventRatingDto getEventRating(Long eventId) {
        log.debug("Getting rating for event: {}", eventId);

        // Проверяем существование события
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        List<Object[]> ratingData = reactionRepository.getEventRating(eventId);

        if (ratingData.isEmpty()) {
            return EventRatingDto.builder()
                    .eventId(eventId)
                    .eventTitle(event.getTitle())
                    .likesCount(0L)
                    .dislikesCount(0L)
                    .totalReactions(0L)
                    .rating(0)
                    .build();
        }

        Object[] data = ratingData.get(0);
        Long totalReactions = (Long) data[1];
        Long likesCount = (Long) data[2];
        Long dislikesCount = (Long) data[3];

        Integer rating = totalReactions > 0 ? (int) ((likesCount * 100) / totalReactions) : 0;

        return EventRatingDto.builder()
                .eventId(eventId)
                .eventTitle(event.getTitle())
                .likesCount(likesCount)
                .dislikesCount(dislikesCount)
                .totalReactions(totalReactions)
                .rating(rating)
                .build();
    }

    @Override
    public List<EventRatingDto> getEventsRating(List<Long> eventIds) {
        log.debug("Getting rating for events: {}", eventIds);

        if (eventIds == null || eventIds.isEmpty()) {
            return List.of();
        }

        // Получаем все события
        List<Event> events = eventRepository.findAllById(eventIds);
        Map<Long, String> eventTitles = events.stream()
                .collect(Collectors.toMap(Event::getId, Event::getTitle));

        // Получаем рейтинги
        List<Object[]> ratingsData = reactionRepository.getEventsRating(eventIds);
        Map<Long, EventRatingDto> ratingMap = ratingsData.stream()
                .map(data -> {
                    Long eventId = (Long) data[0];
                    Long totalReactions = (Long) data[1];
                    Long likesCount = (Long) data[2];
                    Long dislikesCount = (Long) data[3];
                    Integer rating = totalReactions > 0 ? (int) ((likesCount * 100) / totalReactions) : 0;

                    return EventRatingDto.builder()
                            .eventId(eventId)
                            .eventTitle(eventTitles.get(eventId))
                            .likesCount(likesCount)
                            .dislikesCount(dislikesCount)
                            .totalReactions(totalReactions)
                            .rating(rating)
                            .build();
                })
                .collect(Collectors.toMap(EventRatingDto::getEventId, dto -> dto));

        // Добавляем события без реакций
        for (Long eventId : eventIds) {
            if (!ratingMap.containsKey(eventId)) {
                ratingMap.put(eventId, EventRatingDto.builder()
                        .eventId(eventId)
                        .eventTitle(eventTitles.get(eventId))
                        .likesCount(0L)
                        .dislikesCount(0L)
                        .totalReactions(0L)
                        .rating(0)
                        .build());
            }
        }

        return ratingMap.values().stream().toList();
    }

    @Override
    public UserRatingDto getUserRating(Long userId) {
        log.debug("Getting rating for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        // Используем corrected methods
        Long likesGiven = reactionRepository.countByUserIdAndReactionType(userId, LIKE);
        Long dislikesGiven = reactionRepository.countByUserIdAndReactionType(userId, DISLIKE);

        // Если null, устанавливаем 0
        likesGiven = likesGiven != null ? likesGiven : 0L;
        dislikesGiven = dislikesGiven != null ? dislikesGiven : 0L;

        // Получаем топ-5 событий пользователя (где пользователь - инициатор)
        List<EventRatingDto> topRatedEvents = getTopRatedEventsForUser(userId, 5);

        return UserRatingDto.builder()
                .userId(userId)
                .userName(user.getName())
                .totalLikesGiven(likesGiven)
                .totalDislikesGiven(dislikesGiven)
                .totalReactionsGiven(likesGiven + dislikesGiven)
                .topRatedEvents(topRatedEvents)
                .build();
    }

    @Override
    public List<EventRatingDto> getTopRatedEvents(Integer limit) {
        log.debug("Getting top {} rated events", limit);

        // Получаем все события, которые имеют хотя бы одну реакцию
        List<Object[]> allRatings = reactionRepository.getEventsRating(null);

        List<EventRatingDto> ratings = allRatings.stream()
                .map(data -> {
                    Long eventId = (Long) data[0];
                    Long totalReactions = (Long) data[1];
                    Long likesCount = (Long) data[2];
                    Long dislikesCount = (Long) data[3];
                    Integer rating = totalReactions > 0 ? (int) ((likesCount * 100) / totalReactions) : 0;

                    return EventRatingDto.builder()
                            .eventId(eventId)
                            .likesCount(likesCount)
                            .dislikesCount(dislikesCount)
                            .totalReactions(totalReactions)
                            .rating(rating)
                            .build();
                })
                .sorted((r1, r2) -> r2.getRating().compareTo(r1.getRating()))
                .limit(limit)
                .toList();

        // Загружаем названия событий
        List<Long> eventIds = ratings.stream().map(EventRatingDto::getEventId).toList();
        List<Event> events = eventRepository.findAllById(eventIds);
        Map<Long, String> eventTitles = events.stream()
                .collect(Collectors.toMap(Event::getId, Event::getTitle));

        ratings.forEach(rating -> rating.setEventTitle(eventTitles.get(rating.getEventId())));

        return ratings;
    }

    private List<EventRatingDto> getTopRatedEventsForUser(Long userId, int limit) {
        // Получаем события, созданные пользователем
        List<Event> userEvents = eventRepository.findByInitiatorId(userId, PageRequest.of(0, limit))
                .getContent();

        List<Long> eventIds = userEvents.stream().map(Event::getId).toList();

        if (eventIds.isEmpty()) {
            return List.of();
        }

        return getEventsRating(eventIds).stream()
                .sorted((r1, r2) -> r2.getRating().compareTo(r1.getRating()))
                .limit(limit)
                .toList();
    }
}