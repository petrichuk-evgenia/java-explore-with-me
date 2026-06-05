package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.ReactionDto;
import ru.practicum.model.Reaction;

@Component
public class ReactionMapper {

    public ReactionDto toDto(Reaction reaction) {
        if (reaction == null) {
            return null;
        }

        return ReactionDto.builder()
                .id(reaction.getId().getEventId() * 1000000 + reaction.getId().getUserId()) // Генерируем уникальный id
                .eventId(reaction.getId().getEventId())
                .userId(reaction.getId().getUserId())
                .reactionType(reaction.getReactionType())
                .created(reaction.getCreated())
                .build();
    }
}