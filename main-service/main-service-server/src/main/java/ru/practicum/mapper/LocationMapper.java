package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.Location;

@Component
public class LocationMapper {

    public Location toDto(ru.practicum.model.Location location) {
        return Location.builder()
                .lat(location.getLat())
                .lon(location.getLon())
                .build();
    }

    public ru.practicum.model.Location toEntity(Location dto) {
        ru.practicum.model.Location location = new ru.practicum.model.Location();
        location.setLat(dto.getLat());
        location.setLon(dto.getLon());
        return location;
    }
}
