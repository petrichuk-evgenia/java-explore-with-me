package ru.practicum.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRatingDto {
    private Long userId;
    private String userName;
    private Long totalLikesGiven;
    private Long totalDislikesGiven;
    private Long totalReactionsGiven;
    private List<EventRatingDto> topRatedEvents;
}