package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CompilationMapper {

    private final EventMapper eventMapper;

    public CompilationMapper(EventMapper eventMapper) {
        this.eventMapper = eventMapper;
    }

    public CompilationDto toDto(Compilation compilation) {
        Set<EventShortDto> eventsDto = new HashSet<>();
        if (compilation.getEvents() != null && !compilation.getEvents().isEmpty()) {
            eventsDto = compilation.getEvents().stream()
                    .map(eventMapper::toShortDto)
                    .collect(Collectors.toSet());
        }

        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .events(eventsDto)
                .build();
    }

    public Compilation toEntity(NewCompilationDto dto) {
        Compilation compilation = Compilation.builder()
                .title(dto.getTitle())
                .pinned(dto.getPinned() != null ? dto.getPinned() : false)
                .build();

        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            Set<Event> events = new HashSet<>();
            for (Long eventId : dto.getEvents()) {
                Event event = new Event();
                event.setId(eventId);
                events.add(event);
            }
            compilation.setEvents(events);
        }

        return compilation;
    }
}
