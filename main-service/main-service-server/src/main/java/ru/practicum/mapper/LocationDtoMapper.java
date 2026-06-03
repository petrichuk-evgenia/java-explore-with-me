package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.Location;

@Component
public class LocationDtoMapper {

    public Location toDto(ru.practicum.model.Location location) {
        return Location.builder()
                .lat(location.getLat())
                .lon(location.getLon())
                .build();
    }

    public ru.practicum.model.Location toEntity(Location location) {
        return ru.practicum.model.Location.builder()
                .lat(location.getLat())
                .lon(location.getLon())
                .build();
    }
}
