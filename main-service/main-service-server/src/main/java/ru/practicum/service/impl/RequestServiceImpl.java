package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EventRequestStatusUpdateRequest;
import ru.practicum.dto.EventRequestStatusUpdateResult;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.ParticipationRequestMapper;
import ru.practicum.model.Event;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.model.User;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.ParticipationRequestRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.RequestService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final ParticipationRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ParticipationRequestMapper requestMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("User cannot add request to their own event");
        }

        if (event.getState() != ru.practicum.dto.EventState.PUBLISHED) {
            throw new ConflictException("Event must be published");
        }

        if (event.getParticipantLimit() > 0 && event.getParticipantLimit() <=
                requestRepository.countByEventIdAndStatus(eventId, ru.practicum.dto.RequestStatus.CONFIRMED)) {
            throw new ConflictException("Participant limit reached");
        }

        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Request already exists");
        }

        ParticipationRequest request = requestMapper.toEntity(userId, eventId);
        request.setCreated(LocalDateTime.now());
        request.setStatus(ru.practicum.dto.RequestStatus.PENDING);

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            request.setStatus(ru.practicum.dto.RequestStatus.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() != null ? event.getConfirmedRequests() + 1 : 1);
            eventRepository.save(event);
        }

        return requestMapper.toDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        List<ParticipationRequest> requests = requestRepository.findByRequesterId(userId);
        return requests.stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));

        if (!request.getRequester().getId().equals(userId)) {
            throw new NotFoundException("Request with id=" + requestId + " was not found");
        }

        if (request.getStatus() == ru.practicum.dto.RequestStatus.CONFIRMED) {
            Event event = request.getEvent();
            event.setConfirmedRequests(event.getConfirmedRequests() != null ? event.getConfirmedRequests() - 1 : 0);
            eventRepository.save(event);
        }

        request.setStatus(ru.practicum.dto.RequestStatus.CANCELED);
        return requestMapper.toDto(requestRepository.save(request));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public ParticipationRequestDto updateRequestStatus(Long userId, Long eventId, Long requestId, boolean confirm) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }

        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));

        if (request.getStatus() != ru.practicum.dto.RequestStatus.PENDING) {
            throw new ConflictException("Request must have status PENDING");
        }

        if (confirm) {
            if (event.getParticipantLimit() > 0) {
                long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, ru.practicum.dto.RequestStatus.CONFIRMED);
                if (confirmedRequests >= event.getParticipantLimit()) {
                    throw new ValidationException("Participant limit reached");
                }
            }

            request.setStatus(ru.practicum.dto.RequestStatus.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() != null ? event.getConfirmedRequests() + 1 : 1);
            eventRepository.save(event);
        } else {
            request.setStatus(ru.practicum.dto.RequestStatus.REJECTED);
        }

        return requestMapper.toDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        Event event = getEventEntity(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }

        List<ParticipationRequest> requests = requestRepository.findByEventId(eventId);
        return requests.stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }

        List<ParticipationRequest> requests = requestRepository.findAllById(request.getRequestIds());

        if (requests.size() != request.getRequestIds().size()) {
            throw new NotFoundException("One or more requests not found");
        }

        for (ParticipationRequest req : requests) {
            if (req.getStatus() != ru.practicum.dto.RequestStatus.PENDING) {
                throw new ConflictException("Request with id=" + req.getId() + " must have status PENDING");
            }
        }

        List<ParticipationRequest> confirmedRequests = new ArrayList<>();
        List<ParticipationRequest> rejectedRequests = new ArrayList<>();

        if (request.getStatus().equals(ru.practicum.dto.RequestStatus.CONFIRMED.toString())) {
            int currentConfirmed = event.getConfirmedRequests() != null ? event.getConfirmedRequests() : 0;

            // Если лимит уже достигнут, нельзя подтвердить ни одну заявку
            if (event.getParticipantLimit() > 0 && currentConfirmed >= event.getParticipantLimit()) {
                throw new ConflictException("Participant limit reached. Cannot confirm any more requests.");
            }

            for (ParticipationRequest req : requests) {
                if (event.getParticipantLimit() == 0 || currentConfirmed < event.getParticipantLimit()) {
                    req.setStatus(ru.practicum.dto.RequestStatus.CONFIRMED);
                    confirmedRequests.add(req);
                    currentConfirmed++;
                } else {
                    req.setStatus(ru.practicum.dto.RequestStatus.REJECTED);
                    rejectedRequests.add(req);
                }
            }
            event.setConfirmedRequests(event.getConfirmedRequests() != null ? event.getConfirmedRequests() + confirmedRequests.size() : confirmedRequests.size());
            eventRepository.save(event);
        } else if (request.getStatus().equals(ru.practicum.dto.RequestStatus.REJECTED.toString())) {
            for (ParticipationRequest req : requests) {
                req.setStatus(ru.practicum.dto.RequestStatus.REJECTED);
                rejectedRequests.add(req);
            }
        }

        List<ParticipationRequest> updatedRequests = requestRepository.saveAll(requests);

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(confirmedRequests.stream().map(requestMapper::toDto).collect(Collectors.toList()));
        result.setRejectedRequests(rejectedRequests.stream().map(requestMapper::toDto).collect(Collectors.toList()));

        return result;
    }

    private User getUserEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
    }

    private Event getEventEntity(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }
}
