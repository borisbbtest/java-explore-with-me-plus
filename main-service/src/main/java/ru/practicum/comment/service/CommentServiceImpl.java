package ru.practicum.comment.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentRequestDto;
import ru.practicum.comment.dto.CommentResponseDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.exceptions.ValidationException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentServiceImpl implements CommentService {

    CommentRepository commentRepository;

    UserRepository userRepository;

    EventRepository eventRepository;

    @Override
    public List<CommentResponseDto> findAll(Long userId,
                                            Long eventId,
                                            PageRequest pageRequest) {
        User user = getUserById(userId);
        Event event = getEventById(eventId);
        List<Comment> comments = commentRepository.findByAuthorAndEvent(user, event, pageRequest);
        return comments.stream().map(CommentMapper::toCommentResponseDto).toList();
    }

    @Override
    public CommentResponseDto save(CommentRequestDto commentRequestDto,
                                   Long userId,
                                   Long eventId) {
        User user = getUserById(userId);
        Event event = getEventById(eventId);

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Нельзя написать комментарий к событию которое еще не было опубликованно");
        }
        Comment comment = commentRepository.save(CommentMapper.toComment(commentRequestDto, user, event));
        return CommentMapper.toCommentResponseDto(comment);
    }

    @Override
    public CommentResponseDto update(CommentRequestDto commentRequestDto,
                                     Long userId,
                                     Long commentId) {
        Comment oldComment = getCommentById(commentId);
        getUserById(userId);

        if (!oldComment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Редактировать комментарии разрешено только его автору");
        }
        oldComment.setText(commentRequestDto.getText());
        Comment comment = commentRepository.save(oldComment);
        return CommentMapper.toCommentResponseDto(comment);
    }

    @Override
    public void delete(Long userId,
                       Long commentId) {
        Comment comment = getCommentById(commentId);
        getUserById(userId);
        if (!comment.getAuthor().getId().equals(userId) &&
                !comment.getAuthor().getId().equals(comment.getEvent().getInitiator().getId())) {
            throw new ConflictException("Удалять комментарии разрешено только его автору или инициатору мероприятия");
        }
        commentRepository.deleteById(commentId);
    }


    @Override
    public void deleteByIds(final List<Long> ids) {
        List<Event> events = eventRepository.findAllById(ids);
        if (ids.size() != events.size()) {
            throw new ValidationException("Были переданы несуществующие id событий");
        }
        commentRepository.deleteAllById(ids);
        log.info("Комментарии успешно удалены");
    }

    @Override
    public void deleteByEventId(Long eventId) {
        Event event = getEventById(eventId);
        commentRepository.deleteByEvent(event);
        log.info("Все комментарии у события с id = {} успешно удалены", eventId);
    }

    @Override
    public List<CommentResponseDto> findByEvent(Long eventId,
                                                PageRequest pageRequest) {
        Event event = getEventById(eventId);
        List<Comment> comments = commentRepository.findByEvent(event, pageRequest);
        log.info("Получены все комментарии события с id = {}", eventId);
        return comments.stream().map(CommentMapper::toCommentResponseDto).toList();
    }

    @Override
    public CommentResponseDto findById(final Long commentId) {
        Comment comment = getCommentById(commentId);
        return CommentMapper.toCommentResponseDto(comment);
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с id = {} нет." + userId));
    }

    private Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("События с id = {} нет." + eventId));
    }

    private Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментария с id = {} нет." + commentId));
    }
}
