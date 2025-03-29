package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.event.dto.*;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;
import java.util.Map;

public interface EventService {

    List<EventShortDto> getUserEvents(Long userId,
                                      Pageable pageable);

    EventFullDto createEvent(Long userId,
                             NewEventDto newEventDto);

    EventFullDto getUserEventById(Long userId,
                                  Long eventId);

    EventFullDto updateUserEvent(Long userId,
                                 Long eventId,
                                 UpdateEventUserRequest updateEventUserRequest);

    List<ParticipationRequestDto> getEventRequests(Long userId,
                                                   Long eventId);

    Map<String, List<ParticipationRequestDto>> approveRequests(Long userId,
                                                               Long eventId,
                                                               EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest);

    List<EventFullDto> searchEventsByAdmin(SearchAdminEventsParamDto searchAdminEventsParamDto);

    EventFullDto updateEventByAdmin(Long eventId,
                                     UpdateEventAdminRequest updateEventAdminRequest);

    List<EventShortDto> searchPublicEvents(SearchPublicEventsParamDto searchPublicEventsParamDto);

    EventFullDto getPublicEvent(Long eventId,
                                HttpServletRequest request);
}
