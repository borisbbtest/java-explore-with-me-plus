package ru.practicum.comment.mapper;

import ru.practicum.comment.dto.CommentRequestDto;
import ru.practicum.comment.dto.CommentResponseDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

public class CommentMapper {

    public static Comment toComment(CommentRequestDto commentRequestDto,
                                    User user,
                                    Event event) {
        return Comment.builder()
                .text(commentRequestDto.getText())
                .created(LocalDateTime.now())
                .author(user)
                .event(event).build();
    }

    public static CommentResponseDto toCommentResponseDto(Comment comment) {

        return CommentResponseDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .event(EventMapper.toShortDto(comment.getEvent()))
                .created(comment.getCreated())
                .build();
    }
}
