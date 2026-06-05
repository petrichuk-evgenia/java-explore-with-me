package ru.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.dto.ReactionType;

import java.time.LocalDateTime;

@Entity
@Table(name = "reactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reaction {

    @EmbeddedId
    private ReactionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("eventId")
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type", nullable = false)
    private ReactionType reactionType;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime created;
}