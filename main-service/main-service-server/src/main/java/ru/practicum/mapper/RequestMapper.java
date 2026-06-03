package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.model.Event;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.model.RequestStatus;
import ru.practicum.model.User;

@Component
public class RequestMapper {

    public ParticipationRequestDto toDto(ParticipationRequest request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .status(request.getStatus().toString())
                .build();
    }

    public ParticipationRequest toEntity(ParticipationRequestDto dto, Long eventId, Long requesterId) {
        return ParticipationRequest.builder()
                .id(dto.getId())
                .created(dto.getCreated())
                .event(Event.builder().id(eventId).build())
                .requester(User.builder().id(requesterId).build())
                .status(RequestStatus.valueOf(dto.getStatus()))
                .build();
    }
}
