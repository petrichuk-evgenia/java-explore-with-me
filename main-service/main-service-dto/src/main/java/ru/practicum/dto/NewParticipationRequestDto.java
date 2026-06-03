package ru.practicum.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewParticipationRequestDto {
    private Long event;
}
