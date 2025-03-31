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
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.SearchAdminEventsParamDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
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
    public ResponseEntity<List<EventFullDto>> searchEventsByAdmin(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> stateStrings,
            @RequestParam(required = false) List<Long> categoriesIds,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime rangeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size) {

        if (rangeStart == null) rangeStart = LocalDateTime.now();
        if (rangeEnd == null) rangeEnd = LocalDateTime.now().plusYears(100);
        validateTimeRange(rangeStart, rangeEnd);
        PageRequest pageRequest = createPageRequest(from, size);

        List<EventState> states = parseEventStates(stateStrings);
        log.info("Админ поиск события по параметрам: " +
                        "users={}, states={}, categoriesIds={}, rangeStart={}, rangeEnd={}",
                users, states, categoriesIds, rangeStart, rangeEnd);
        SearchAdminEventsParamDto searchAdminEventsParamDto =
                SearchAdminEventsParamDto.builder()
                        .users(users)
                        .eventStates(states)
                        .categoriesIds(categoriesIds)
                        .rangeStart(rangeStart)
                        .rangeEnd(rangeEnd)
                        .pageRequest(pageRequest)
                        .build();
        return ResponseEntity.ok(eventService.searchEventsByAdmin(searchAdminEventsParamDto)
        );
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateEventByAdmin(
            @PathVariable @Positive Long eventId,
            @RequestBody @Valid UpdateEventAdminRequest updateEventAdminRequest) {
        log.info("Редактирование данных события и его статуса (отклонение/публикация) id = {} и изменения: {}",
                eventId, updateEventAdminRequest);

        return ResponseEntity.ok(
                eventService.updateEventByAdmin(eventId, updateEventAdminRequest)
        );
    }

    private void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            log.error("Временной промежуток задан неверно: {} - {}", start, end);
            throw new ValidationException("Временной промежуток задан неверно");
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
