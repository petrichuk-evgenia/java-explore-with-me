package ru.practicum.dto;

import lombok.*;

@Data
@Builder
public class ReactionDto {
    private Long id;
    private Long userId;
    private Long eventId;
    private String reactionType;
}
