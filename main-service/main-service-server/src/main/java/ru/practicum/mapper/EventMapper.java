package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.*;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class EventMapper {

    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;
    private final LocationDtoMapper locationDtoMapper;

    public EventMapper(CategoryMapper categoryMapper, UserMapper userMapper, LocationDtoMapper locationDtoMapper) {
        this.categoryMapper = categoryMapper;
        this.userMapper = userMapper;
        this.locationDtoMapper = locationDtoMapper;
    }

    public EventFullDto toFullDto(Event event) {
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(categoryMapper.toDto(event.getCategory()))
                .confirmedRequests(0L)
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(userMapper.toShortDto(event.getInitiator()))
                .location(locationDtoMapper.toDto(event.getLocation()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .views(0L)
                .build();
    }

    public EventShortDto toShortDto(Event event) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(categoryMapper.toDto(event.getCategory()))
                .confirmedRequests(0L)
                .eventDate(event.getEventDate())
                .initiator(userMapper.toShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .title(event.getTitle())
                .build();
    }

    public Event toEntity(NewEventDto dto, Long categoryId, Long userId) {
        return Event.builder()
                .annotation(dto.getAnnotation())
                .category(Category.builder().id(categoryId).build())
                .description(dto.getDescription())
                .eventDate(dto.getEventDate())
                .initiator(User.builder().id(userId).build())
                .location(new ru.practicum.model.Location(dto.getLocation().getLat(), dto.getLocation().getLon()))
                .paid(dto.getPaid())
                .participantLimit(dto.getParticipantLimit())
                .requestModeration(dto.getRequestModeration())
                .title(dto.getTitle())
                .createdOn(LocalDateTime.now())
                .state(EventState.PENDING)
                .build();
    }

    public List<EventFullDto> toFullDtoList(List<Event> events) {
        List<EventFullDto> dtoList = new ArrayList<>();
        for (Event event : events) {
            dtoList.add(toFullDto(event));
        }
        return dtoList;
    }

    public List<EventShortDto> toShortDtoList(List<Event> events) {
        List<EventShortDto> dtoList = new ArrayList<>();
        for (Event event : events) {
            dtoList.add(toShortDto(event));
        }
        return dtoList;
    }
}
