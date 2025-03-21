package ru.practicum.server.stat.mapper;

import ru.practicum.server.stat.model.App;
import ru.practicum.server.stat.model.EndpointHit;
import ru.practicum.server.stat.model.Uri;
import ru.practicum.stat.dto.EndpointHitDto;

public class StatsMapper {

    public static EndpointHit toEntity(EndpointHitDto dto, App app, Uri uri) {
        return EndpointHit.builder()
                .app(app)
                .uri(uri)
                .ip(dto.getIp())
                .timestamp(dto.getTimestamp())
                .build();
    }

    public static EndpointHitDto toDto(EndpointHit hit) {
        return EndpointHitDto.builder()
                .id(hit.getId())
                .app(hit.getApp().getName())
                .uri(hit.getUri().getUri())
                .ip(hit.getIp())
                .timestamp(hit.getTimestamp())
                .build();
    }
}
