package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.NewEventDto;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.User;

@Component
public class EventMapper {

    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;
    private final LocationMapper locationMapper;

    public EventMapper(CategoryMapper categoryMapper, UserMapper userMapper, LocationMapper locationMapper) {
        this.categoryMapper = categoryMapper;
        this.userMapper = userMapper;
        this.locationMapper = locationMapper;
    }

    public EventFullDto toFullDto(Event event) {
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(categoryMapper.toDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests() != null ? event.getConfirmedRequests().longValue() : 0L)
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(userMapper.toShortDto(event.getInitiator()))
                .location(locationMapper.toDto(event.getLocation()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .views(event.getViews() != null ? event.getViews().longValue() : 0L)
                .build();
    }

    public EventShortDto toShortDto(Event event) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(categoryMapper.toDto(event.getCategory()))
                .confirmedRequests((long) (event.getConfirmedRequests() != null ? event.getConfirmedRequests() : 0))
                .eventDate(event.getEventDate())
                .initiator(userMapper.toShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .views((long) (event.getViews() != null ? event.getViews() : 0))
                .build();
    }

    public Event toEntity(NewEventDto dto, User user, Category category) {
        return Event.builder()
                .annotation(dto.getAnnotation())
                .category(category)
                .description(dto.getDescription())
                .eventDate(dto.getEventDate())
                .initiator(user)
                .location(locationMapper.toEntity(dto.getLocation()))
                .paid(dto.getPaid() != null ? dto.getPaid() : false)
                .participantLimit(dto.getParticipantLimit() != null ? dto.getParticipantLimit() : 0)
                .requestModeration(dto.getRequestModeration() != null ? dto.getRequestModeration() : true)
                .title(dto.getTitle())
                .views(0)
                .confirmedRequests(0)
                .build();
    }
}
