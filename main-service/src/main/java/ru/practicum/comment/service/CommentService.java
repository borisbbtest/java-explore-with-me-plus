package ru.practicum.comment.service;

import org.springframework.data.domain.PageRequest;
import ru.practicum.comment.dto.CommentRequestDto;
import ru.practicum.comment.dto.CommentResponseDto;

import java.util.List;


public interface CommentService {


    List<CommentResponseDto> findAll(Long userId,
                                     Long eventId,
                                     PageRequest pageRequest);

    CommentResponseDto save(CommentRequestDto commentRequestDto,
                            Long userId,
                            Long eventId);

    CommentResponseDto update(CommentRequestDto commentRequestDto,
                              Long userId,
                              Long commentId);

    void delete(Long userId,
                Long commentId);

    void deleteByIds(List<Long> ids);

    void deleteByEventId(Long eventId);


    List<CommentResponseDto> findByEvent(Long eventId,
                                         PageRequest pageRequest);

    CommentResponseDto findById(Long commentId);
}
