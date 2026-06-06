package ru.practicum.dto;

import lombok.*;

@Data
@Builder
public class UpdateReactionRequest {
    private Long eventId;
    private String reactionType;
}
