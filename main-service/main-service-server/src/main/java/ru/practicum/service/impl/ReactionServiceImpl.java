package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.AddReactionRequest;
import ru.practicum.dto.EventRatingDto;
import ru.practicum.dto.ReactionDto;
import ru.practicum.dto.UpdateReactionRequest;
import ru.practicum.dto.UserRatingDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.ReactionMapper;
import ru.practicum.model.Event;
import ru.practicum.model.Reaction;
import ru.practicum.model.User;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.ReactionRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.ReactionService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReactionServiceImpl implements ReactionService {
    private final ReactionRepository reactionRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ReactionMapper reactionMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public ReactionDto addReaction(Long userId, AddReactionRequest request) {
        if (!eventRepository.existsById(request.getEventId())) {
            throw new NotFoundException("Event with id=" + request.getEventId() + " was not found");
        }

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        Optional<Reaction> existingReaction = reactionRepository.findByUserIdAndEventIdCustom(userId, request.getEventId());
        if (existingReaction.isPresent()) {
            // Реакция уже существует, возвращаем её без изменений (идемпотентность)
            return reactionMapper.toDto(existingReaction.get());
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Event event = eventRepository.findById(request.getEventId()).orElseThrow(() -> new NotFoundException("Event with id=" + request.getEventId() + " was not found"));

        Reaction reaction = Reaction.builder()
                .user(user)
                .event(event)
                .reactionType(request.getReactionType())
                .build();

        try {
            Reaction savedReaction = reactionRepository.save(reaction);
            return reactionMapper.toDto(savedReaction);
        } catch (DataIntegrityViolationException e) {
            // Если вдруг между проверкой и сохранением другая транзакция добавила реа��цию
            Optional<Reaction> doubleCheck = reactionRepository.findByUserIdAndEventIdCustom(userId, request.getEventId());
            if (doubleCheck.isPresent()) {
                return reactionMapper.toDto(doubleCheck.get());
            }
            // Если всё ещё не нашли, значит конфликт, но это редкий случай
            // Возвращаем существующую реакцию, если она появилась
            throw new ConflictException("Reaction from user " + userId + " to event " + request.getEventId() + " already exists");
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public ReactionDto updateReaction(Long userId, UpdateReactionRequest request) {
        if (!eventRepository.existsById(request.getEventId())) {
            throw new NotFoundException("Event with id=" + request.getEventId() + " was not found");
        }

        Reaction existingReaction = reactionRepository.findByUserIdAndEventIdCustom(userId, request.getEventId())
                .orElseThrow(() -> new NotFoundException("Reaction from user " + userId + " to event " + request.getEventId() + " was not found"));

        existingReaction.setReactionType(request.getReactionType());

        Reaction savedReaction = reactionRepository.save(existingReaction);
        return reactionMapper.toDto(savedReaction);
    }

    @Override
    public ReactionDto getReaction(Long userId, Long eventId) {
        Reaction reaction = reactionRepository.findByUserIdAndEventIdCustom(userId, eventId)
                .orElseThrow(() -> new NotFoundException("Reaction from user " + userId + " to event " + eventId + " was not found"));
        return reactionMapper.toDto(reaction);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public void deleteReaction(Long userId, Long eventId) {
        reactionRepository.findByUserIdAndEventIdCustom(userId, eventId)
                .ifPresent(reactionRepository::delete);
    }

    @Override
    public EventRatingDto getEventRating(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }

        Long likesCount = reactionRepository.countByEventIdAndReactionTypeLike(eventId);
        Long dislikesCount = reactionRepository.countByEventIdAndReactionTypeDislike(eventId);

        return EventRatingDto.builder()
                .eventId(eventId)
                .likesCount(likesCount)
                .dislikesCount(dislikesCount)
                .build();
    }

    @Override
    public UserRatingDto getUserRating(Long userId) {
        Long likesReceived = reactionRepository.countByEventInitiatorIdAndReactionTypeLike(userId);
        Long dislikesReceived = reactionRepository.countByEventInitiatorIdAndReactionTypeDislike(userId);
        Long likesGiven = reactionRepository.countByUserIdAndReactionTypeLike(userId);
        Long dislikesGiven = reactionRepository.countByUserIdAndReactionTypeDislike(userId);

        return UserRatingDto.builder()
                .userId(userId)
                .totalLikesReceived(likesReceived)
                .totalDislikesReceived(dislikesReceived)
                .totalLikesGiven(likesGiven)
                .totalDislikesGiven(dislikesGiven)
                .build();
    }
}
