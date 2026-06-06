package ru.practicum.dto;

import lombok.*;

@Data
@Builder
public class UserRatingDto {
    private Long userId;
    private Long totalLikesReceived;
    private Long totalDislikesReceived;
    private Long totalLikesGiven;
    private Long totalDislikesGiven;
}
