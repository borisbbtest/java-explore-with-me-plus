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
import ru.practicum.StatClient;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.SearchPublicEventsParamDto;
import ru.practicum.event.model.EventSort;
import ru.practicum.event.service.EventService;
import ru.practicum.exceptions.ValidationException;
import ru.practicum.stat.dto.EndpointHitDto;
import ru.practicum.stat.dto.ViewStatsDto;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private static final int START_SEARCH_DATE_PERIOD = 100;
    private static final int END_SEARCH_DATE_PERIOD = 300;
    private final EventService eventService;
    private final StatClient statClient;
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

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
        log.info("Запрос на получение опубликованных событий: text='{}', " +
                        "categoriesIds={}, paid={}, start={}, end={}, onlyAvailable={}, eventSort={}",
                text, categoriesIds, paid, rangeStart, rangeEnd, onlyAvailable, eventSort);
        validateTimeRange(rangeStart, rangeEnd);
        if (rangeStart == null) rangeStart = LocalDateTime.now();
        if (rangeEnd == null) rangeEnd = LocalDateTime.now().plusYears(100);

        PageRequest pageRequest = createPageRequest(from, size, eventSort);
        SearchPublicEventsParamDto searchPublicEventsParamDto =
                SearchPublicEventsParamDto.builder().text(text)
                        .categoriesIds(categoriesIds)
                        .paid(paid)
                        .rangeStart(rangeStart)
                        .rangeEnd(rangeEnd)
                        .onlyAvailable(onlyAvailable)
                        .pageRequest(pageRequest)
                        .build();

        List<EventShortDto> eventShortDtos = eventService.searchPublicEvents(searchPublicEventsParamDto);
        List<Long> eventShortDtoIds = eventShortDtos.stream().map(EventShortDto::getId).toList();

        log.info("Запрос статистики для событий с id {}", eventShortDtoIds);
        String start = rangeStart.format(dateTimeFormatter);
        String end = rangeEnd.format(dateTimeFormatter);
        List<String> uris = buildUrisFromPathAndIds(request.getRequestURI(), eventShortDtoIds);
        List<ViewStatsDto> viewStatsDtos = getStatisticsEventViews(start,
                end, uris, true);
        Map<Long, Long> viewsMap = viewStatsDtos.stream()
                .collect(Collectors.toMap(
                        stats -> {
                            String[] parts = stats.getUri().split("/");
                            return Long.parseLong(parts[parts.length - 1]);
                        },
                        ViewStatsDto::getHits,
                        (existing, replacement) -> existing
                ));

        eventShortDtos.forEach(dto ->
                dto.setViews(viewsMap.getOrDefault(dto.getId(), 0L))
        );
        log.info("Обновляем статистику");
        saveStat(request);

        return ResponseEntity.ok(eventShortDtos);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventFullDto> getEvent(
            @PathVariable @Positive Long eventId,
            HttpServletRequest request) {
        log.info("Запрос на получение опубликованого события с id {}", eventId);
        EventFullDto eventFullDto = eventService.getPublicEvent(eventId, request);

        log.info("Запрос статистики для события с id {}", eventId);
        String start = LocalDateTime.now().minusYears(START_SEARCH_DATE_PERIOD).format(dateTimeFormatter);
        String end = LocalDateTime.now().plusYears(END_SEARCH_DATE_PERIOD).format(dateTimeFormatter);
        List<ViewStatsDto> viewStatsDtos = getStatisticsEventViews(start,
                end, List.of(request.getRequestURI()), true);
        if (viewStatsDtos.size() != 0) {
            eventFullDto.setViews(viewStatsDtos.get(0).getHits());
        } else {
            eventFullDto.setViews(0L);
        }

        log.info("Обновляем статистику");
        if (eventFullDto.getId() != null) saveStat(request);

        return ResponseEntity.ok(eventFullDto);
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

    public void saveStat(HttpServletRequest request) {
        EndpointHitDto hitDto = EndpointHitDto.builder()
                .app("ewm-service-1")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build();
        statClient.saveStatEvent(hitDto);
    }

    public List<ViewStatsDto> getStatisticsEventViews(String start,
                                                      String end,
                                                      List<String> uris,
                                                      boolean unique) {
        ResponseEntity<List<ViewStatsDto>> response = statClient.getStats(
                start,
                end,
                uris,
                unique
        );

        return response.getBody();
    }

    public List<String> buildUrisFromPathAndIds(String uriPath, List<Long> ids) {
        return ids.stream()
                .map(id -> Path.of(uriPath, String.valueOf(id)).toString())
                .collect(Collectors.toList());
    }
}