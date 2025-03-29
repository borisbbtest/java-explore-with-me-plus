package ru.practicum.request.controller;


import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
@Validated
public class RequestController {

    private final RequestService requestService;

    @GetMapping
    public ResponseEntity<List<ParticipationRequestDto>> getUserRequests(@PathVariable @Positive Long userId) {
        log.info("Запрос на получение всех заявок участия пользователя с id {}", userId);
        return ResponseEntity.ok(requestService.getUserRequests(userId));
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public ResponseEntity<ParticipationRequestDto> createParticipationRequest(@PathVariable Long userId,
                                                                              @RequestParam Long eventId) {
        log.info("Запрос на создание заявки на участие пользователя с id {} в событии с id {}", userId, eventId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(requestService.createParticipationRequest(userId, eventId));
    }

    @PatchMapping("/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> cancelParticipationRequest(@PathVariable @Positive Long userId,
                                                                              @PathVariable @Positive Long requestId) {
        log.info("Запрос на отмену заявки на участие с id = {}", requestId);
        return ResponseEntity.ok(requestService.cancelParticipationRequest(userId, requestId));
    }
}
