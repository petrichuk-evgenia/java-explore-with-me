package ru.practicum.dto;

import lombok.*;

@Data
@Builder
public class AddReactionRequest {
    private Long eventId;
    private String reactionType;
}
