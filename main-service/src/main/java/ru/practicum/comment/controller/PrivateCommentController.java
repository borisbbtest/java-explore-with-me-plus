package ru.practicum.comment.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentRequestDto;
import ru.practicum.comment.dto.CommentResponseDto;
import ru.practicum.comment.service.CommentService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
@Validated
public class PrivateCommentController {

    private final CommentService commentService;
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "created");


    @GetMapping("/events/{eventId}/comments")
    public ResponseEntity<List<CommentResponseDto>> findAll(@PathVariable @Positive Long userId,
                                                            @PathVariable @Positive Long eventId,
                                                            @RequestParam(defaultValue = "0") int from,
                                                            @RequestParam(defaultValue = "10") int size) {
        log.info("Запрос на получение всех комментариев пользователя с id = {}", userId);
        return ResponseEntity.ok(commentService.findAll(userId, eventId, createPageRequest(from, size)));
    }

    @PostMapping("/events/{eventId}/comments")
    @ResponseStatus(code = HttpStatus.CREATED)
    public ResponseEntity<CommentResponseDto> save(@RequestBody CommentRequestDto commentRequestDto,
                                                   @PathVariable @Positive Long userId,
                                                   @PathVariable @Positive Long eventId) {
        log.info("Запрос на сохранение комментария к событию с id = {}", eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.save(commentRequestDto, userId, eventId));
    }

    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponseDto> update(@RequestBody CommentRequestDto commentRequestDto,
                                                     @PathVariable @Positive Long userId,
                                                     @PathVariable @Positive Long commentId) {
        log.info("Запрос на обновление комментария с id = {}", commentId);
        return ResponseEntity.ok(commentService.update(commentRequestDto, userId, commentId));
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<String> delete(@PathVariable @Positive Long userId,
                                         @PathVariable @Positive Long commentId) {
        log.info("Запрос на удаление комментария с id = {}", commentId);
        commentService.delete(userId, commentId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Комметарий удален: " + commentId);
    }

    private PageRequest createPageRequest(int from, int size) {
        int page = from / size;
        return PageRequest.of(page, size, DEFAULT_SORT);
    }
}
