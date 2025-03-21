package ru.practicum.server.stat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stat.dto.EndpointHitDto;
import ru.practicum.server.stat.service.StatsService;
import ru.practicum.stat.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/hit")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @PostMapping
    public ResponseEntity<EndpointHitDto> saveHit(@RequestBody @Valid EndpointHitDto hitDto) {
        EndpointHitDto saved = statsService.saveHit(hitDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<ViewStatsDto>> getStats(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end,
            @RequestParam(required = false) List<String> uris
    ) {
        return ResponseEntity.ok(statsService.getStats(start, end, uris));
    }
}
