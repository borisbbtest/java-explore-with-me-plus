package ru.practicum.request.service;

import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {

    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto createParticipationRequest(Long userId,
                                                       Long eventId);

    ParticipationRequestDto cancelParticipationRequest(Long userId,
                                                       Long requestId);
}
