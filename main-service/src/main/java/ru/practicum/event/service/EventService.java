package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.EventState;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.dto.RequestUpdateDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface EventService {

    List<EventResponseShortDto> getUserEvents(Long userId,
                                        Pageable pageable);

    EventResponseLongDto createEvent(Long userId,
                              EventRequestDto eventRequestDto);

    EventResponseLongDto getUserEventById(Long userId,
                                  Long eventId);

    EventResponseLongDto updateUserEvent(Long userId,
                                Long eventId,
                                EventUpdateUserDto eventUpdateUserDto);

    List<RequestDto> getEventRequests(Long userId,
                                           Long eventId);

    Map<String, List<RequestDto>> approveRequests(Long userId,
                                                  Long eventId,
                                                  RequestUpdateDto requestUpdateDto);

    List<EventResponseLongDto> searchEventsByAdmin(List<Long> usersId,
                                                   List<EventState> states,
                                                   List<Long> categoriesId,
                                                   LocalDateTime start,
                                                   LocalDateTime end,
                                                   Pageable pageable);

    EventResponseLongDto approveEventByAdmin(Long eventId,
                                             EventUpdateDto eventUpdateDto);

    List<EventResponseShortDto> searchPublicEvents(String text,
                                              List<Long> categories,
                                              Boolean paid,
                                              LocalDateTime start,
                                              LocalDateTime end,
                                              boolean onlyAvailable,
                                              PageRequest pageRequest,
                                              HttpServletRequest request
                                              );

    EventResponseLongDto getPublicEvent(Long eventId,
                                        HttpServletRequest request);
}
