package ru.practicum.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.service.EventService;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;
import java.util.Map;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
public class PrivateEventController {
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "id");
    private static final String DEFAULT_PAGE_SIZE = "10";
    private static final String DEFAULT_PAGE_START = "0";
    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getUserEvents(
            @PathVariable @Positive Long userId,
            @RequestParam(defaultValue = DEFAULT_PAGE_START) @PositiveOrZero int from,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) @Positive int size) {
        log.info("Запрос на получение событий пользователя с id = {} (page: {}, size: {})", userId, from, size);
        PageRequest pageRequest = createPageRequest(from, size);

        return ResponseEntity.ok(eventService.getUserEvents(userId, pageRequest));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<EventFullDto> createEvent(
            @PathVariable @Positive Long userId,
            @RequestBody @Valid @DateTimeFormat(pattern = DATE_TIME_PATTERN) NewEventDto newEventDto) {
        log.info("Запрос на добавление нового события для пользователя user ID {}: {}", userId, newEventDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.createEvent(userId, newEventDto));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventFullDto> getEventById(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId) {
        log.info("Запрос на получение события с id = {} для пользователя с ID {}", eventId, userId);
        return ResponseEntity.ok(eventService.getUserEventById(userId, eventId));
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateEvent(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId,
            @RequestBody @Valid UpdateEventUserRequest updateEventUserRequest) {
        log.info("Запрос на обновление события с id = {} для пользователя с ID {}: {}", eventId, userId, updateEventUserRequest);
        return ResponseEntity.ok(eventService.updateUserEvent(userId, eventId, updateEventUserRequest));
    }

    @GetMapping("/{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getEventRequests(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId) {
        log.info("Запрос на получение всех заявок на событие с id = {} для пользователя с ID {}", eventId, userId);
        return ResponseEntity.ok(eventService.getEventRequests(userId, eventId));
    }

    @PatchMapping("/{eventId}/requests")
    public ResponseEntity<Map<String, List<ParticipationRequestDto>>> approveRequests(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId,
            @RequestBody @Valid EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        log.info("Запрос на изменение статуса переданных заявок на событие с id = {} для пользователя с ID {}: {}",
                eventId, userId, eventRequestStatusUpdateRequest);

        return ResponseEntity.ok(eventService.approveRequests(userId, eventId, eventRequestStatusUpdateRequest));
    }

    private PageRequest createPageRequest(int from, int size) {
        int page = from / size;
        return PageRequest.of(page, size, DEFAULT_SORT);
    }
}