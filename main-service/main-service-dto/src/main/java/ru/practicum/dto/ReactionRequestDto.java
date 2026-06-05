package ru.practicum.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReactionRequestDto {
    @NotNull
    private Long eventId;

    @NotNull
    private ReactionType reactionType;
}