package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.SearchPublicEventsParamDto;
import ru.practicum.event.model.EventSort;
import ru.practicum.event.service.EventService;
import ru.practicum.exceptions.ValidationException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class PublicEventController {
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DEFAULT_TEXT = "";
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int DEFAULT_PAGE_START = 0;
    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventShortDto>> searchPublicEvents(
            @RequestParam(defaultValue = DEFAULT_TEXT) String text,
            @RequestParam(required = false) List<Long> categoriesIds,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime rangeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") boolean onlyAvailable,
            @RequestParam(required = false) EventSort eventSort,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_START) @PositiveOrZero int from,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) @Positive int size,
            HttpServletRequest request) {

        if (rangeStart == null) rangeStart = LocalDateTime.now();
        if (rangeEnd == null) rangeEnd = LocalDateTime.now().plusYears(100);
        validateTimeRange(rangeStart, rangeEnd);
        PageRequest pageRequest = createPageRequest(from, size, eventSort);
        SearchPublicEventsParamDto searchPublicEventsParamDto =
                SearchPublicEventsParamDto.builder().text(text).
                        categoriesIds(categoriesIds).
                        paid(paid).
                        rangeStart(rangeStart).
                        rangeEnd(rangeEnd).
                        onlyAvailable(onlyAvailable).
                        pageRequest(pageRequest).
                        build();
        log.info("Запрос на получение опубликованных событий: text='{}', " +
                        "categoriesIds={}, paid={}, start={}, end={}, onlyAvailable={}, eventSort={}",
                text, categoriesIds, paid, rangeStart, rangeEnd, onlyAvailable, eventSort);

        return ResponseEntity.ok(eventService.searchPublicEvents(searchPublicEventsParamDto));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventFullDto> getEvent(
            @PathVariable @Positive Long eventId,
            HttpServletRequest request) {
        log.info("Запрос на получение опубликованого события с id {}", eventId);
        return ResponseEntity.ok(eventService.getPublicEvent(eventId, request));
    }

    private PageRequest createPageRequest(int from, int size, EventSort sort) {
        int page = from / size;
        Sort sorting = (sort == EventSort.VIEWS)
                ? Sort.by(Sort.Direction.DESC, "views")
                : Sort.by(Sort.Direction.ASC, "eventDate");

        return PageRequest.of(page, size, sorting);
    }

    private void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new ValidationException("Время начала должно быть до окончания");
        }
    }
}