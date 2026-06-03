package ru.practicum.service;

import ru.practicum.dto.ParticipationRequestDto;

import java.util.List;

public interface ParticipationRequestService {
    ParticipationRequestDto createRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto getRequestById(Long userId, Long requestId);
}
