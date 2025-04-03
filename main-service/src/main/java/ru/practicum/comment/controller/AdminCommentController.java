package ru.practicum.comment.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.service.CommentService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Validated
public class AdminCommentController {

    private final CommentService commentService;

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<String> deleteByIds(@RequestParam List<Long> ids) {
        log.info("Запрос на удаление комментариев администратором");
        commentService.deleteByIds(ids);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Комметарии удалены: " + ids);
    }

    @DeleteMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<String> deleteByEventId(@PathVariable @Positive Long eventId) {
        log.info("Запрос на удаление всех комментариев у события с id = {}", eventId);
        commentService.deleteByEventId(eventId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Удалены все комментарии у события с id: " + eventId);
    }
}
