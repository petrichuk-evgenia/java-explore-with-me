package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.model.Event;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.model.User;

@Component
public class ParticipationRequestMapper {

    public ParticipationRequestDto toDto(ParticipationRequest request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .status(request.getStatus().name())
                .build();
    }

    public ParticipationRequest toEntity(Long userId, Long eventId) {
        return ParticipationRequest.builder()
                .requester(User.builder().id(userId).build())
                .event(Event.builder().id(eventId).build())
                .build();
    }
}
