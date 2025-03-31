package ru.practicum.request.mapper;

import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.Request;

public class RequestMapper {

    public static ParticipationRequestDto toRequestDto(Request request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .requester(request.getRequester().getId())
                .event(request.getEvent().getId())
                .status(request.getStatus().getName())
                .created(request.getCreated())
                .build();
    }
}
