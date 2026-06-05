package ru.practicum.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRatingDto {
    private Long eventId;
    private String eventTitle;
    private Long likesCount;
    private Long dislikesCount;
    private Integer rating;
    private Long totalReactions;
}