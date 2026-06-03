package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.*;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.*;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;
    private final RequestMapper requestMapper;

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto eventDto) {
        validateNewEvent(eventDto);

        Event event = eventMapper.toEntity(eventDto, eventDto.getCategory(), userId);
        eventRepository.save(event);

        return eventMapper.toFullDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getUserEvents(Long userId, Integer from, Integer size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("User not found: " + userId));

        Pageable pageable = PageRequest.of(from / size, size);
        Page<Event> eventsPage = eventRepository.findByInitiatorId(userId, pageable);

        return eventsPage.stream()
                .map(eventMapper::toFullDto)
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest eventDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ValidationException("Event not found: " + eventId));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("Event does not belong to user");
        }

        if (event.getState() != EventState.PENDING) {
            throw new ValidationException("Can only update events in PENDING state");
        }

        if (eventDto.getAnnotation() != null) {
            event.setAnnotation(eventDto.getAnnotation());
        }
        if (eventDto.getCategory() != null) {
            event.setCategory(Category.builder().id(eventDto.getCategory()).build());
        }
        if (eventDto.getDescription() != null) {
            event.setDescription(eventDto.getDescription());
        }
        if (eventDto.getEventDate() != null) {
            event.setEventDate(eventDto.getEventDate());
        }
        if (eventDto.getLocation() != null) {
            event.setLocation(new ru.practicum.model.Location(
                    eventDto.getLocation().getLat(),
                    eventDto.getLocation().getLon()
            ));
        }
        if (eventDto.getPaid() != null) {
            event.setPaid(eventDto.getPaid());
        }
        if (eventDto.getParticipantLimit() != null) {
            if (eventDto.getParticipantLimit() < 0) {
                throw new ValidationException("Participant limit cannot be negative");
            }
            event.setParticipantLimit(eventDto.getParticipantLimit());
        }
        if (eventDto.getRequestModeration() != null) {
            event.setRequestModeration(eventDto.getRequestModeration());
        }
        if (eventDto.getTitle() != null) {
            event.setTitle(eventDto.getTitle());
        }

        return eventMapper.toFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto publishEvent(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ValidationException("Event not found: " + eventId));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("Event does not belong to user");
        }

        if (event.getState() != EventState.PENDING) {
            throw new ValidationException("Can only publish events in PENDING state");
        }

        event.setState(EventState.PUBLISHED);
        event.setPublishedOn(LocalDateTime.now());

        return eventMapper.toFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto cancelEvent(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ValidationException("Event not found: " + eventId));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("Event does not belong to user");
        }

        if (event.getState() == EventState.PUBLISHED) {
            throw new ValidationException("Can only cancel published events");
        }

        event.setState(EventState.CANCELED);

        return eventMapper.toFullDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getAllEvents(String text, List<Long> categories, Boolean paid,
                                           String rangeStart, String rangeEnd, Boolean onlyAvailable,
                                           String sort, Integer from, Integer size) {

        return eventRepository.findAll().stream()
                .map(eventMapper::toFullDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ValidationException("Event not found: " + eventId));

        return eventMapper.toFullDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("User not found: " + userId));

        List<ParticipationRequest> requests = requestRepository.findByRequesterId(userId);

        return requests.stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("User not found: " + userId));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ValidationException("Event not found: " + eventId));

        if (event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("Initiator cannot request participation in own event");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ValidationException("Can only request participation in published events");
        }

        if (event.getParticipantLimit() != 0 &&
                requestRepository.countByEventIdAndStatus(eventId, "CONFIRMED") >= event.getParticipantLimit()) {
            throw new ValidationException("Event has reached participant limit");
        }

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ValidationException("Request already exists");
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .event(event)
                .requester(User.builder().id(userId).build())
                .created(LocalDateTime.now())
                .status(RequestStatus.PENDING)
                .build();

        if (event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else if (!event.getRequestModeration()) {
            request.setStatus(RequestStatus.CONFIRMED);
        }

        requestRepository.save(request);

        return requestMapper.toDto(request);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ValidationException("Request not found: " + requestId));

        if (!request.getRequester().getId().equals(userId)) {
            throw new ValidationException("Request does not belong to user");
        }

        if (request.getStatus() == RequestStatus.CONFIRMED || request.getStatus() == RequestStatus.REJECTED) {
            throw new ValidationException("Cannot cancel confirmed or rejected request");
        }

        request.setStatus(RequestStatus.CANCELLED);

        return requestMapper.toDto(request);
    }

    private void validateNewEvent(NewEventDto eventDto) {
        if (eventDto.getAnnotation() == null || eventDto.getAnnotation().isBlank()) {
            throw new ValidationException("Annotation is required");
        }
        if (eventDto.getAnnotation().length() < 20 || eventDto.getAnnotation().length() > 2000) {
            throw new ValidationException("Annotation must be between 20 and 2000 characters");
        }
        if (eventDto.getDescription() == null || eventDto.getDescription().isBlank()) {
            throw new ValidationException("Description is required");
        }
        if (eventDto.getDescription().length() < 20 || eventDto.getDescription().length() > 7000) {
            throw new ValidationException("Description must be between 20 and 7000 characters");
        }
        if (eventDto.getEventDate() == null) {
            throw new ValidationException("Event date is required");
        }
        if (eventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Event date must be at least 2 hours from now");
        }
        if (eventDto.getTitle() == null || eventDto.getTitle().isBlank()) {
            throw new ValidationException("Title is required");
        }
        if (eventDto.getTitle().length() < 3 || eventDto.getTitle().length() > 120) {
            throw new ValidationException("Title must be between 3 and 120 characters");
        }
        if (eventDto.getParticipantLimit() != null && eventDto.getParticipantLimit() < 0) {
            throw new ValidationException("Participant limit cannot be negative");
        }
    }
}
