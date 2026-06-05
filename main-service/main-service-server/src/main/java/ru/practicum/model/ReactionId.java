package ru.practicum.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactionId implements Serializable {
    private Long eventId;
    private Long userId;
}