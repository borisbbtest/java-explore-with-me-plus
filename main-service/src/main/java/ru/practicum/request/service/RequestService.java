package ru.practicum.request.service;

import ru.practicum.request.dto.RequestDto;

import java.util.List;

public interface RequestService {

    List<RequestDto> getUserRequests(Long userId);

    RequestDto createParticipationRequest(Long userId,
                    Long eventId);

    RequestDto cancelParticipationRequest(Long userId,
                             Long requestId);
}
