package ru.practicum.comment.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentResponseDto;
import ru.practicum.comment.service.CommentService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
@Validated
public class PublicCommentController {

    private final CommentService commentService;
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "created");

    @GetMapping("events/{eventId}/comments")
    public ResponseEntity<List<CommentResponseDto>> findByEvent(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Запрос на получение всех комментариев у события с id = {}", eventId);
        return ResponseEntity.ok(commentService.findByEvent(eventId, createPageRequest(from, size)));
    }

    @GetMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponseDto> findById(@PathVariable @Positive Long commentId) {
        log.info("Запрос на получение комментария с id = {}", commentId);
        return ResponseEntity.ok(commentService.findById(commentId));
    }

    private PageRequest createPageRequest(int from, int size) {
        int page = from / size;
        return PageRequest.of(page, size, DEFAULT_SORT);
    }
}
