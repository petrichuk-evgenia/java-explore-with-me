package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.EndpointHit;
import ru.practicum.model.EndpointHitEntity;

@Component
public class StatisticsMapper {

    /**
     * Преобразование DTO в Entity
     */
    public EndpointHitEntity toEntity(EndpointHit dto) {
        if (dto == null) {
            return null;
        }

        return EndpointHitEntity.builder()
                .id(dto.getId())
                .app(dto.getApp())
                .uri(dto.getUri())
                .ip(dto.getIp())
                .timestamp(dto.getTimestamp())
                .build();
    }

    /**
     * Преобразование Entity в DTO
     */
    public EndpointHit toDto(EndpointHitEntity entity) {
        if (entity == null) {
            return null;
        }

        return EndpointHit.builder()
                .id(entity.getId())
                .app(entity.getApp())
                .uri(entity.getUri())
                .ip(entity.getIp())
                .timestamp(entity.getTimestamp())
                .build();
    }
}