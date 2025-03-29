package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.Location;
import ru.practicum.event.model.StateAction;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.LocationRepository;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.exceptions.ValidationException;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.dto.RequestUpdateDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.request.repository.RequestStatusRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.validation.EventValidator;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final RequestRepository requestRepository;
    private final EventValidator eventValidator;
    private final RequestStatusRepository requestStatusRepository;

    private final RequestMapper requestMapper;

    @Override
    @Transactional(readOnly = true)
    public List<EventResponseShortDto> getUserEvents(Long userId, Pageable pageable) {
        eventValidator.validateUserExists(userId);

        return eventRepository.findByInitiatorId(userId, pageable)
                .stream()
                .map(EventMapper::toShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventResponseLongDto createEvent(Long userId, EventRequestDto request) {
        User user = getUserById(userId);
        Category category = getCategoryById(request.getCategory());
        Location location = resolveLocation(request.getLocation());

        eventValidator.validateEventCreation(user.getId(), category.getId(), location);

        Event event = EventMapper.toEvent(request, user, category);
        event.setLocation(location);
        event.setState(EventState.PENDING);

        Event savedEvent = eventRepository.save(event);
        log.info("Событие успешно добавлено под id {} со статусом {} и ожидается подтверждение",
                user.getId(), event.getState());
        return EventMapper.toLongDto(savedEvent);
    }

    @Transactional(readOnly = true)
    @Override
    public EventResponseLongDto getUserEventById(Long userId,
                                                 Long eventId) {
        Event event = getEventById(eventId);
        eventValidator.validateEventOwnership(event, userId);
        return EventMapper.toLongDto(event);
    }

    @Override
    public EventResponseLongDto updateUserEvent(Long userId,
                                                Long eventId,
                                                EventUpdateUserDto updateDto) {
        User user = getUserById(userId);
        Event event = getEventById(eventId);

        eventValidator.validateUserUpdate(event, user, updateDto);
        applyUserUpdates(event, updateDto);

        Event updatedEvent = eventRepository.save(event);
        log.info("Событие успешно обновлено под id {} и дожидается подтверждения", eventId);
        return EventMapper.toLongDto(updatedEvent);
    }

    @Transactional(readOnly = true)
    @Override
    public List<RequestDto> getEventRequests(Long userId, Long eventId) {
        Event event = getEventById(eventId);
        eventValidator.validateEventOwnership(event, userId);

        return requestRepository.findByEventId(eventId)
                .stream()
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    public Map<String, List<RequestDto>> approveRequests(Long userId, Long eventId, RequestUpdateDto requestUpdateDto) {
        User user = getUserById(userId);
        Event event = getEventById(eventId);
        validateInitiator(event, user);

        List<Request> requests = getAndValidateRequests(eventId, requestUpdateDto.getRequestIds());
        RequestStatus status = requestUpdateDto.getStatus();

        if (status == RequestStatus.CONFIRMED) {
            eventValidator.validateParticipantLimit(event);
        }

        return processStatusSpecificLogic(event, requests, status);
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventResponseLongDto> searchEventsByAdmin(List<Long> usersId,
                                                          List<EventState> states,
                                                          List<Long> categoriesId,
                                                          LocalDateTime start,
                                                          LocalDateTime end,
                                                          Pageable pageable) {
        if (start.isAfter(end)) {
            throw new ValidationException("Временной промежуток задан неверно");
        }
        List<User> users;
        if (Objects.isNull(usersId) || usersId.isEmpty()) {
            users = userRepository.findAll();
            if (users.isEmpty()) {
                log.info("Еще нет ни одного пользователя, а значит и событий нет");
                return new ArrayList<>();
            }
        } else {
            users = userRepository.findByIdIn(usersId, pageable);
            if (users.size() != usersId.size()) {
                throw new ValidationException("Список пользователей передан неверно");
            }
        }
        List<Category> categories;
        if (categoriesId == null) {
            categories = categoryRepository.findAll();
            if (categories.isEmpty()) {
                log.info("Еще нет ни одной категории, а значит и событий нет");
                return new ArrayList<>();
            }
        } else {
            categories = categoryRepository.findByIdIn(categoriesId, pageable);
            if (categories.size() != categoriesId.size()) {
                throw new ValidationException("Список категорий передан неверно неверно");
            }
        }
        List<Event> events = eventRepository
                .findByInitiatorInAndStateInAndCategoryInAndEventDateAfterAndEventDateBefore(
                        users, states, categories, start, end, pageable);
        if (events.isEmpty()) {
            log.info("По данным параметрам не нашлось ни одного события");
            return new ArrayList<>();
        }
        log.info("Получен список событий по заданным параметрам");
        return events.stream().map(EventMapper::toLongDto).toList();
    }


    @Override
    @Transactional
    public EventResponseLongDto approveEventByAdmin(Long eventId,
                                                    EventUpdateDto eventUpdateDto) {
        log.info("Заявка к администратору на публикацию события с id = {}", eventId);
        Event oldEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("События с id = {} нет." + eventId));

        if ((Objects.nonNull(eventUpdateDto.getEventDate()) &&
                eventUpdateDto.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) ||
                oldEvent.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new ValidationException("Событие не может начинаться ранее" +
                    " чем через 1 час после редактирования администратором");
        }
        if (oldEvent.getPublishedOn() != null && LocalDateTime.now().plusHours(1).isBefore(oldEvent.getPublishedOn())) {
            throw new ValidationException("Дата начала изменяемого события должна быть не ранее " +
                    "чем за час от даты публикации.");
        }
        if (oldEvent.getState().equals(EventState.PUBLISHED) ||
                oldEvent.getState().equals(EventState.CANCELED)) {
            throw new ValidationException("Администратор не может менять статус опубликованного или отмененного события");
        }
        if (Objects.nonNull(eventUpdateDto.getAnnotation())) {
            oldEvent.setAnnotation(eventUpdateDto.getAnnotation());
        }
        if (Objects.nonNull(eventUpdateDto.getCategory())) {
            Category category = categoryRepository.findById(eventUpdateDto.getCategory())
                    .orElseThrow(() -> new ValidationException("Категория указана неверно"));
            oldEvent.setCategory(category);
        }
        if (Objects.nonNull(eventUpdateDto.getDescription())) {
            oldEvent.setDescription(eventUpdateDto.getDescription());
        }
        if (Objects.nonNull(eventUpdateDto.getEventDate())) {
            oldEvent.setEventDate(eventUpdateDto.getEventDate());
        }
        if (Objects.nonNull(eventUpdateDto.getLocation())) {
            oldEvent.setLocation(eventUpdateDto.getLocation());
        }
        if (Objects.nonNull(eventUpdateDto.getPaid())) {
            oldEvent.setPaid(eventUpdateDto.getPaid());
        }
        if (Objects.nonNull(eventUpdateDto.getParticipantLimit())) {
            oldEvent.setParticipantLimit(eventUpdateDto.getParticipantLimit());
        }
        if (Objects.nonNull(eventUpdateDto.getRequestModeration())) {
            oldEvent.setRequestModeration(eventUpdateDto.getRequestModeration());
        }
        if (Objects.nonNull(eventUpdateDto.getStateAction()) &&
                oldEvent.getState().equals(EventState.PENDING) &&
                eventUpdateDto.getStateAction().equals(StateAction.PUBLISH_EVENT)) {
            oldEvent.setState(EventState.PUBLISHED);
            oldEvent.setPublishedOn(LocalDateTime.now());
        }
        if (Objects.nonNull(eventUpdateDto.getStateAction()) &&
                oldEvent.getState().equals(EventState.PENDING) &&
                eventUpdateDto.getStateAction().equals(StateAction.REJECT_EVENT)) {
            oldEvent.setState(EventState.CANCELED);
            oldEvent.setPublishedOn(null);
        }
        if (Objects.nonNull(eventUpdateDto.getTitle())) {
            oldEvent.setTitle(eventUpdateDto.getTitle());
        }
        Event event = eventRepository.save(oldEvent);
        log.info("Событие успешно обновлено администратором");
        return EventMapper.toLongDto(event);
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventResponseShortDto> searchPublicEvents(String text,
                                                          List<Long> categories,
                                                          Boolean paid,
                                                          LocalDateTime start,
                                                          LocalDateTime end,
                                                          boolean onlyAvailable,
                                                          PageRequest pageRequest,
                                                          HttpServletRequest request) {

        return new ArrayList<>();
    }

    @Transactional(readOnly = true)
    @Override
    public EventResponseLongDto getPublicEvent(Long eventId,
                                               HttpServletRequest request) {

        return null;
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Не найден пользователя с ID: " + userId));
    }

    private Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Не найдено событие с ID: " + eventId));
    }

    private Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ValidationException("Указана не правильная ID категории: " + categoryId));
    }

    private Location resolveLocation(Location requestLocation) {
        Location mayBeExistingLocation = null;
        if (requestLocation.getId() == null) {
            mayBeExistingLocation = locationRepository
                    .findByLatAndLon(requestLocation.getLat(), requestLocation.getLon())
                    .orElseGet(() -> locationRepository.save(requestLocation));
        }
        return mayBeExistingLocation;
    }

    private void applyUserUpdates(Event event, EventUpdateUserDto update) {
        Optional.ofNullable(update.getAnnotation()).ifPresent(event::setAnnotation);
        Optional.ofNullable(update.getDescription()).ifPresent(event::setDescription);
        Optional.ofNullable(update.getEventDate()).ifPresent(event::setEventDate);
        Optional.ofNullable(update.getPaid()).ifPresent(event::setPaid);
        Optional.ofNullable(update.getParticipantLimit()).ifPresent(event::setParticipantLimit);
        Optional.ofNullable(update.getRequestModeration()).ifPresent(event::setRequestModeration);
        Optional.ofNullable(update.getTitle()).ifPresent(event::setTitle);

        Optional.ofNullable(update.getCategory())
                .map(this::getCategoryById)
                .ifPresent(event::setCategory);

        Optional.ofNullable(update.getLocation())
                .map(this::resolveLocation)
                .ifPresent(event::setLocation);

        updateState(update.getStateAction(), event);
    }

    private void updateState(StateAction stateAction, Event event) {
        if (stateAction == null) return;
        switch (stateAction) {
            case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
            case CANCEL_REVIEW -> event.setState(EventState.CANCELED);
        }
    }

    private Map<String, List<RequestDto>> processStatusSpecificLogic(Event event, List<Request> requests, RequestStatus status) {
        if (status == RequestStatus.REJECTED) {
            return processRejection(requests);
        } else {
            return processConfirmation(event, requests);
        }
    }

    public void validateInitiator(Event event, User user) {
        if (!event.getInitiator().equals(user)) {
            throw new ValidationException("У этого события другой инициатор");
        }
    }

    private List<Request> getAndValidateRequests(Long eventId, List<Long> requestIds) {
        List<Request> requests = requestRepository.findRequestByIdIn(requestIds);
        eventValidator.validateRequestsBelongToEvent(requests, eventId);
        return requests;
    }

    private Map<String, List<RequestDto>> processRejection(List<Request> requests) {
        eventValidator.validateNoConfirmedRequests(requests);

        requests.forEach(request -> request.setStatus(
                requestStatusRepository.findByName(RequestStatus.REJECTED)
                        .orElseThrow(() -> new IllegalArgumentException("Не верный статус"))
        ));

        List<RequestDto> rejectedRequests = requestRepository.saveAll(requests)
                .stream()
                .map(RequestMapper::toRequestDto)
                .toList();

        return Map.of("rejectedRequests", rejectedRequests);
    }

    private void updateRequestStatuses(List<Request> requests, RequestStatus status) {
        requests.forEach(request -> request.setStatus(
                requestStatusRepository.findByName(status)
                        .orElseThrow(() -> new IllegalArgumentException("Не верный статус"))
        ));
    }

    private Map<String, List<RequestDto>> processConfirmation(Event event, List<Request> requests) {
        eventValidator.validateAllRequestsPending(requests);

        int availableSlots = event.getParticipantLimit() - event.getConfirmedRequests();
        List<Request> confirmed = requests.stream().limit(availableSlots).toList();
        List<Request> rejected = requests.stream().skip(availableSlots).toList();

        updateRequestStatuses(confirmed, RequestStatus.CONFIRMED);
        updateRequestStatuses(rejected, RequestStatus.REJECTED);

        requestRepository.saveAll(requests);
        updateEventConfirmedRequests(event, confirmed.size());

        return Map.of(
                "confirmedRequests", mapToDtoList(confirmed),
                "rejectedRequests", mapToDtoList(rejected)
        );
    }

    private void updateEventConfirmedRequests(Event event, int newConfirmations) {
        event.setConfirmedRequests(event.getConfirmedRequests() + newConfirmations);
        eventRepository.save(event);
    }

    private List<RequestDto> mapToDtoList(List<Request> requests) {
        return requests.stream()
                .map(RequestMapper::toRequestDto)
                .toList();
    }

}
