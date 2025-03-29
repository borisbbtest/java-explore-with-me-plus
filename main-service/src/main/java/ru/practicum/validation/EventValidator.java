package ru.practicum.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.Location;
import ru.practicum.exceptions.DuplicateException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.exceptions.ValidationException;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventValidator {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public void validateEventCreation(Long userId, Long categoryId, Location location) {
        validateUserExists(userId);
        validateCategoryExists(categoryId);
    }

    public void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            log.error("Пользователя с id {} не найден", userId);
            throw new NotFoundException("Пользователя с id не найден: " + userId);
        }
    }

    public void validateRequestsBelongToEvent(List<Request> requests, Long eventId) {
        boolean allMatch = requests.stream()
                .allMatch(request -> request.getEvent().getId().equals(eventId));
        if (!allMatch) {
            throw new ValidationException("Неверно передан список запросов");
        }
    }

    public void validateParticipantLimit(Event event) {
        if (event.getParticipantLimit() != 0 &&
                event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ValidationException("Лимит заявок на участие в событии исчерпан");
        }
    }

    public void validateNoConfirmedRequests(List<Request> requests) {
        if (requests.stream().anyMatch(r -> r.getStatus().getName() == RequestStatus.CONFIRMED)) {
            throw new DuplicateException("Нельзя отменить уже подтвержденные заявки");
        }
    }

    public void validateAllRequestsPending(List<Request> requests) {
        if (requests.stream().anyMatch(r -> r.getStatus().getName() != RequestStatus.PENDING)) {
            throw new DuplicateException("Все заявки должны быть в статусе ожидания");
        }
    }

    private void validateCategoryExists(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            log.error("Категория с ID {} не найдена", categoryId);
            throw new NotFoundException("Не найдена категория с ID: " + categoryId);
        }
    }

    public void validateEventOwnership(Event event, Long userId) {
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("Только пользователь создавший событие может получить его полное описание");
        }
    }

    public void validateUserUpdate(Event oldEvent, User user, UpdateEventUserRequest updateDto) {
        if (!oldEvent.getInitiator().getId().equals(user.getId())) {
            throw new ValidationException("Только пользователь создавший событие может его редактировать");
        }
        if (oldEvent.getState().equals(EventState.PUBLISHED)) {
            throw new ValidationException("Нельзя изменить опубликованное событие, или переданный статут несуществует");
        }
        if (Objects.nonNull(updateDto.getEventDate()) &&
                updateDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Событие не может начинаться ранее чем через 2 часа после обновления");
        }
    }
}