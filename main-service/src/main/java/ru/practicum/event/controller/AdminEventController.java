package ru.practicum.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventResponseLongDto;
import ru.practicum.event.dto.EventUpdateDto;
import ru.practicum.event.model.EventState;
import ru.practicum.event.service.EventService;
import ru.practicum.exceptions.ValidationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/events")
public class AdminEventController {
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "id");
    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventResponseLongDto>> searchEventsByAdmin(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> stateStrings,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime rangeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size) {
        validateTimeRange(rangeStart, rangeEnd);
        LocalDateTime start = (rangeStart == null) ? LocalDateTime.now() : rangeStart;
        LocalDateTime end = (rangeStart == null) ? LocalDateTime.now().plusYears(100) : rangeEnd;
        List<EventState> states = parseEventStates(stateStrings);
        log.info("Административное событие поиск события по параметрам: " +
                        "users={}, states={}, categories={}, start={}, end={}",
                users, states, categories, start, end);
        PageRequest pageRequest = createPageRequest(from, size);
        return ResponseEntity.ok(
                eventService.searchEventsByAdmin(
                        users,
                        states,
                        categories,
                        rangeStart,
                        rangeEnd,
                        pageRequest
                )
        );
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventResponseLongDto> approveEventByAdmin(
            @PathVariable @Positive Long eventId,
            @RequestBody @Valid EventUpdateDto updateRequest) {
        log.info("Административное событие утверждение события с ID {} и данными: {}", eventId, updateRequest);
        return ResponseEntity.ok(
                eventService.approveEventByAdmin(eventId, updateRequest)
        );
    }

    private void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new ValidationException("Время начала должно быть до окончания");
        }
    }

    private List<EventState> parseEventStates(List<String> stateStrings) {
        if (ObjectUtils.isEmpty(stateStrings)) {
            return List.of(EventState.PUBLISHED, EventState.CANCELED, EventState.PENDING);
        }
        return stateStrings.stream()
                .map(this::parseEventState)
                .collect(Collectors.toList());
    }

    private EventState parseEventState(String state) {
        try {
            return EventState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Недопустимое значение статуса: " + state);
        }
    }

    private PageRequest createPageRequest(int from, int size) {
        int page = from / size;
        return PageRequest.of(page, size, DEFAULT_SORT);
    }
}
