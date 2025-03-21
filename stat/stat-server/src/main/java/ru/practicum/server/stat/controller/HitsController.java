package ru.practicum.server.stat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.server.stat.service.StatsService;
import ru.practicum.stat.dto.EndpointHitDto;

@RestController
@RequestMapping("/hit")
@RequiredArgsConstructor
public class HitsController {

    private final StatsService statsService;

    @PostMapping
    public ResponseEntity<EndpointHitDto> saveHit(@RequestBody @Valid EndpointHitDto hitDto) {
        EndpointHitDto saved = statsService.saveHit(hitDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

}
