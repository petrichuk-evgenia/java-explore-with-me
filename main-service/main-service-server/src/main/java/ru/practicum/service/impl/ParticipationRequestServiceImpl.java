package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EventState;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.ParticipationRequestMapper;
import ru.practicum.model.Event;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.model.User;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.ParticipationRequestRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.ParticipationRequestService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestServiceImpl implements ParticipationRequestService {
    private final ParticipationRequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ParticipationRequestMapper requestMapper;

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        User user = getUserEntity(userId);
        Event event = getEventEntity(eventId);

        // Проверка на повторный запрос
        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Request already exists");
        }

        // Проверка, что пользователь не инициатор события
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot request participation in own event");
        }

        // Проверка статуса события
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Event must be published");
        }

        // Проверка лимита заявок
        if (event.getParticipantLimit() > 0 &&
                requestRepository.countByEventIdAndStatus(eventId, "CONFIRMED") >= event.getParticipantLimit()) {
            throw new ConflictException("Participant limit has been reached");
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(user)
                .status(event.getRequestModeration() != null && !event.getRequestModeration() ? "CONFIRMED" : "PENDING")
                .build();

        try {
            return requestMapper.toDto(requestRepository.save(request));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Request already exists");
        }
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = getRequestEntity(requestId);

        if (!request.getRequester().getId().equals(userId)) {
            throw new NotFoundException("Request not found");
        }

        request.setStatus("CANCELED");
        return requestMapper.toDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        return requestRepository.findByRequesterId(userId)
                .stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto getRequestById(Long userId, Long requestId) {
        ParticipationRequest request = getRequestEntity(requestId);

        if (!request.getRequester().getId().equals(userId)) {
            throw new NotFoundException("Request not found");
        }

        return requestMapper.toDto(request);
    }

    private User getUserEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
    }

    private Event getEventEntity(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    private ParticipationRequest getRequestEntity(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));
    }
}
