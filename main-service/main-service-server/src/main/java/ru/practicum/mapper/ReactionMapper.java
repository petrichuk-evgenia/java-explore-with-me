package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.*;
import ru.practicum.model.Reaction;
import ru.practicum.model.User;

@Component
public class ReactionMapper {

    public ReactionDto toDto(Reaction reaction) {
        return ReactionDto.builder()
                .id(reaction.getId())
                .userId(reaction.getUser().getId())
                .eventId(reaction.getEvent().getId())
                .reactionType(reaction.getReactionType())
                .build();
    }

    public Reaction toEntity(ReactionDto dto) {
        return Reaction.builder()
                .id(dto.getId())
                .user(User.builder().id(dto.getUserId()).build())
                .reactionType(dto.getReactionType())
                .build();
    }

    public Reaction toEntity(AddReactionRequest request) {
        return Reaction.builder()
                .reactionType(request.getReactionType())
                .build();
    }
}
