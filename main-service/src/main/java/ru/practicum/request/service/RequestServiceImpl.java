package ru.practicum.request.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.exceptions.ValidationException;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.model.RequestStatusEntity;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.request.repository.RequestStatusRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.validation.RequestValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final RequestStatusRepository requestStatusRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestValidator requestValidator;

    @Transactional(readOnly = true)
    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        log.debug("Запрос на получение всех заявок участия пользователя с ID: {}", userId);
        return requestRepository.findByRequesterId(userId).stream()
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto createParticipationRequest(Long userId, Long eventId) {
        final User user = getUserById(userId);
        final Event event = getEventById(eventId);

        requestValidator.validateRequestCreation(user, event);

        final Request request = buildNewRequest(user, event);
        determineInitialStatus(event, request);

        final Request savedRequest = requestRepository.save(request);
        updateEventStatistics(event, request.getStatus().getName());

        log.info("Заявка на участие сохранена со статусом с ID: {} и статусом: {}",
                savedRequest.getId(), savedRequest.getStatus());
        return RequestMapper.toRequestDto(savedRequest);
    }

    @Override
    public ParticipationRequestDto cancelParticipationRequest(Long userId, Long requestId) {
        final User user = getUserById(userId);
        final Request request = getRequestById(requestId);

        requestValidator.validateRequestOwnership(user, request);
        updateRequestStatus(request, RequestStatus.CANCELED);

        if (request.getStatus().getName() == RequestStatus.CONFIRMED) {
            adjustEventConfirmedRequests(request.getEvent(), -1);
        }

        log.info("Заявка на участие с id = {} отменена пользователем ID: {}", requestId, userId);
        return RequestMapper.toRequestDto(request);
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Не найден пользователя с ID: " + userId));
    }

    private Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Не найдено событие с ID: " + eventId));
    }

    private Request getRequestById(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Не найдена заявка с ID: " + requestId));
    }

    private RequestStatusEntity getRequestStatusEntityByRequestStatus(RequestStatus newStatus) {
        return requestStatusRepository.findByName(newStatus)
                .orElseThrow(() -> new NotFoundException("Не найден статус: " + newStatus.name()));
    }

    private Request buildNewRequest(User user, Event event) {
        RequestStatusEntity requestStatusEntity = getRequestStatusEntityByRequestStatus(RequestStatus.PENDING);
        return Request.builder()
                .requester(user)
                .event(event)
                .created(LocalDateTime.now())
                .status(requestStatusEntity)
                .build();
    }

    private void determineInitialStatus(Event event, Request request) {
        if (shouldAutoConfirm(event)) {
            request.setStatus(getRequestStatusEntityByRequestStatus(RequestStatus.CONFIRMED));
        } else if (isEventFull(event)) {
            request.setStatus(getRequestStatusEntityByRequestStatus(RequestStatus.REJECTED));
        }
    }

    private boolean shouldAutoConfirm(Event event) {
        return event.getParticipantLimit() == 0 ||
                (!event.getRequestModeration() && hasAvailableSlots(event));
    }

    private boolean isEventFull(Event event) {
        return event.getParticipantLimit() > 0 &&
                event.getConfirmedRequests() >= event.getParticipantLimit();
    }

    private boolean hasAvailableSlots(Event event) {
        return event.getConfirmedRequests() < event.getParticipantLimit();
    }

    private void updateEventStatistics(Event event, RequestStatus status) {
        if (status == RequestStatus.CONFIRMED) {
            adjustEventConfirmedRequests(event, 1);
        }
    }

    private void adjustEventConfirmedRequests(Event event, int delta) {
        event.setConfirmedRequests(event.getConfirmedRequests() + delta);
        eventRepository.save(event);
    }

    private void updateRequestStatus(Request request, RequestStatus newStatus) {
        String currentStatusName = request.getStatus().getName().name();
        if (currentStatusName.equals(newStatus.name())) {
            throw new ValidationException("Статус уже установлен: " + newStatus);
        }
        request.setStatus(getRequestStatusEntityByRequestStatus(newStatus));
    }
}

