package ru.practicum.stat.dto.mapper;

import ru.practicum.stat.dto.EndpointHitDto;
import ru.practicum.stat.model.EndpointHit;

public class EndpointHitMapper {

    /**
     * Преобразует DTO в Entity
     */
    public static EndpointHit toEntity(EndpointHitDto dto) {
        return EndpointHit.builder()
                .id(dto.getId())
                .app(dto.getApp())
                .uri(dto.getUri())
                .ip(dto.getIp())
                .timestamp(dto.getTimestamp())
                .build();
    }

    /**
     * Преобразует Entity в DTO
     */
    public static EndpointHitDto toDto(EndpointHit entity) {
        return EndpointHitDto.builder()
                .id(entity.getId())
                .app(entity.getApp())
                .uri(entity.getUri())
                .ip(entity.getIp())
                .timestamp(entity.getTimestamp())
                .build();
    }
}
