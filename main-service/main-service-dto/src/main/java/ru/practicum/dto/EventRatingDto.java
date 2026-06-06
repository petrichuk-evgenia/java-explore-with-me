package ru.practicum.dto;

import lombok.*;

@Data
@Builder
public class EventRatingDto {
    private Long eventId;
    private Long likesCount;
    private Long dislikesCount;
}
