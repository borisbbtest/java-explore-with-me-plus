package ru.practicum.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.exceptions.ConflictException;
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

    public void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            log.error("Пользователя с id {} не найден", userId);
            throw new NotFoundException("Пользователя с id не найден: " + userId);
        }
    }

    public void validateInitiator(Event event, User user) {
        if (!event.getInitiator().equals(user)) {
            throw new ValidationException("У этого события другой инициатор");
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
            throw new ConflictException("Лимит заявок на участие в событии исчерпан");
        }
    }

    public void validateNoConfirmedRequests(List<Request> requests) {
        if (requests.stream().anyMatch(r -> r.getStatus().getName() == RequestStatus.CONFIRMED)) {
            throw new ConflictException("Нельзя отменить уже подтвержденные заявки");
        }
    }

    public void validateAllRequestsPending(List<Request> requests) {
        if (requests.stream().anyMatch(r -> r.getStatus().getName() != RequestStatus.PENDING)) {
            throw new ConflictException("Все заявки должны быть в статусе ожидания");
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
            throw new ConflictException("Нельзя изменить опубликованное событие");
        }
        if (Objects.nonNull(updateDto.getEventDate()) &&
                updateDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Событие не может начинаться ранее чем через 2 часа");
        }
    }

    public void validateAdminEventDate(Event oldEvent) {
        if (oldEvent.getPublishedOn() == null) return;
        LocalDateTime minEventStartTime = oldEvent.getPublishedOn().plusHours(1);
        if (oldEvent.getEventDate().isBefore(minEventStartTime)) {
            throw new ConflictException(
                    "Событие не может начинаться раньше чем через 1 час после публикации. " +
                            "Минимальное время: " + minEventStartTime
            );
        }
    }

    public void validateAdminPublishedEventDate(LocalDateTime newEventDate, Event oldEvent) {
        LocalDateTime minEventStartTime = oldEvent.getPublishedOn() != null
                ? oldEvent.getPublishedOn().plusHours(1)
                : LocalDateTime.now().plusHours(1);

        if (newEventDate != null && newEventDate.isBefore(minEventStartTime)) {
            throw new ConflictException("Новая дата начала должна быть не ранее " + minEventStartTime);
        }
    }

    public void validateAdminEventUpdateState(EventState currentState) {
        if (currentState == EventState.PUBLISHED || currentState == EventState.CANCELED) {
            throw new ConflictException("Запрещено редактирование в статусах: " +
                    String.join(", ", EventState.PUBLISHED.name(), EventState.CANCELED.name()));
        }
    }

}