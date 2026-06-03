package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.model.ParticipationRequest;

@Component
public class ParticipationRequestMapper {

    public ParticipationRequestDto toDto(ParticipationRequest request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .status(request.getStatus())
                .build();
    }

    public ParticipationRequest toEntity(ParticipationRequestDto dto) {
        return ParticipationRequest.builder()
                .id(dto.getId())
                .created(dto.getCreated())
                .build();
    }
}
