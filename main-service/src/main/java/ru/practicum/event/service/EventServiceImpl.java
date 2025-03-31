package ru.practicum.event.service;

import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.exceptions.ValidationException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.model.RequestStatusEntity;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.request.repository.RequestStatusRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.validation.EventValidator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getUserEvents(Long userId, Pageable pageable) {
        eventValidator.validateUserExists(userId);

        return eventRepository.findByInitiatorId(userId, pageable)
                .stream()
                .map(EventMapper::toShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto createEvent(Long userId, NewEventDto request) {
        User user = getUserById(userId);
        Category category = getCategoryById(request.getCategory());
        Location location = resolveLocation(request.getLocation());

        Event event = EventMapper.toEvent(request, user, category);
        event.setLocation(location);
        event.setState(EventState.PENDING);

        Event savedEvent = eventRepository.save(event);
        log.info("Событие успешно добавлено под id {} со статусом {} и ожидается подтверждение",
                user.getId(), event.getState());
        return EventMapper.toFullDto(savedEvent);
    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto getUserEventById(Long userId,
                                         Long eventId) {
        Event event = getEventById(eventId);
        eventValidator.validateEventOwnership(event, userId);
        return EventMapper.toFullDto(event);
    }

    @Override
    public EventFullDto updateUserEvent(Long userId,
                                        Long eventId,
                                        UpdateEventUserRequest updateDto) {
        User user = getUserById(userId);
        Event event = getEventById(eventId);

        eventValidator.validateUserUpdate(event, user, updateDto);
        applyUserUpdates(event, updateDto);

        Event updatedEvent = eventRepository.save(event);
        log.info("Событие успешно обновлено под id {} и дожидается подтверждения", eventId);
        return EventMapper.toFullDto(updatedEvent);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        Event event = getEventById(eventId);
        eventValidator.validateEventOwnership(event, userId);

        return requestRepository.findByEventId(eventId)
                .stream()
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    public Map<String, List<ParticipationRequestDto>> approveRequests(Long userId,
                                                                      Long eventId,
                                                                      EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        User user = getUserById(userId);
        Event event = getEventById(eventId);
        eventValidator.validateInitiator(event, user);

        List<Request> requests = getAndValidateRequests(eventId, eventRequestStatusUpdateRequest.getRequestIds());
        RequestStatus status = eventRequestStatusUpdateRequest.getStatus();

        if (status == RequestStatus.CONFIRMED) {
            eventValidator.validateParticipantLimit(event);
        }

        return processStatusSpecificLogic(event, requests, status);
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventFullDto> searchEventsByAdmin(SearchAdminEventsParamDto searchParams) {

        return eventRepository.findAll((root, query, cb) -> {
                    List<Predicate> predicates = new ArrayList<>();

                    // Фильтр по пользователям
                    if (searchParams.getUsers() != null && !searchParams.getUsers().isEmpty()) {
                        predicates.add(root.get("initiator").get("id").in(searchParams.getUsers()));
                    }

                    // Фильтр по состояниям
                    if (searchParams.getEventStates() != null && !searchParams.getEventStates().isEmpty()) {
                        predicates.add(root.get("state").in(searchParams.getEventStates()));
                    }

                    // Фильтр по категориям
                    if (searchParams.getCategoriesIds() != null && !searchParams.getCategoriesIds().isEmpty()) {
                        predicates.add(root.get("category").get("id").in(searchParams.getCategoriesIds()));
                    }

                    // Фильтр по датам
                    predicates.add(cb.between(root.get("eventDate"), searchParams.getRangeStart(),
                            searchParams.getRangeEnd()));

                    return cb.and(predicates.toArray(new Predicate[0]));
                }, searchParams.getPageRequest()).stream()
                .map(EventMapper::toFullDto)
                .collect(Collectors.toList());
    }


    @Override
    public EventFullDto updateEventByAdmin(Long eventId,
                                           UpdateEventAdminRequest updateEventAdminRequest) {
        Event oldEvent = getEventById(eventId);
        eventValidator.validateAdminPublishedEventDate(updateEventAdminRequest.getEventDate(), oldEvent);
        eventValidator.validateAdminEventDate(oldEvent);
        eventValidator.validateAdminEventUpdateState(oldEvent.getState());
        applyAdminUpdates(oldEvent, updateEventAdminRequest);
        Event event = eventRepository.save(oldEvent);
        log.info("Событие успешно обновлено администратором");
        return EventMapper.toFullDto(event);
    }

    private void handleStateUpdateEventAdminRequest(StateAction action, Event event) {
        if (event.getState() != EventState.PENDING) {
            throw new ConflictException("Изменение статуса возможно только для событий в состоянии PENDING");
        }

        switch (action) {
            case PUBLISH_EVENT -> {
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }
            case REJECT_EVENT -> {
                event.setState(EventState.CANCELED);
                event.setPublishedOn(null);
            }
            default -> throw new UnsupportedOperationException("Неподдерживаемая операция: " + action);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> searchPublicEvents(SearchPublicEventsParamDto searchParams) {

        Specification<Event> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Базовые условия
            predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), searchParams.getRangeStart()));
            predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), searchParams.getRangeEnd()));
            predicates.add(cb.equal(root.get("state"), EventState.PUBLISHED));

            // Фильтр по тексту
            if (StringUtils.hasText(searchParams.getText())) {
                String searchTerm = "%" + searchParams.getText().toLowerCase() + "%";
                Predicate annotationLike = cb.like(cb.lower(root.get("annotation")), searchTerm);
                Predicate descriptionLike = cb.like(cb.lower(root.get("description")), searchTerm);
                predicates.add(cb.or(annotationLike, descriptionLike));
            }

            // Фильтр по категориям
            if (searchParams.getCategoriesIds() != null && !searchParams.getCategoriesIds().isEmpty()) {
                predicates.add(root.get("category").get("id").in(searchParams.getCategoriesIds()));
            }

            // Фильтр по paid
            if (searchParams.getPaid() != null) {
                predicates.add(cb.equal(root.get("paid"), searchParams.getPaid()));
            }

            // Фильтр по доступности
            if (searchParams.isOnlyAvailable()) {
                predicates.add(cb.gt(root.get("participantLimit"), root.get("confirmedRequests")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Event> eventsPage = eventRepository.findAll(
                specification,
                PageRequest.of(searchParams.getPageRequest().getPageNumber(),
                        searchParams.getPageRequest().getPageSize(),
                        searchParams.getPageRequest().getSort())
        );

        List<Event> events = eventsPage.getContent();
        if (events.isEmpty()) {
            return new ArrayList<>();
        }
        return paginateAndMap(events, searchParams.getPageRequest());
    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto getPublicEvent(Long eventId,
                                       HttpServletRequest request) {
        Event event = getEventById(eventId);
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("У события должен быть статус <PUBLISHED>");
        }
        return EventMapper.toFullDto(event);
    }

    private List<EventShortDto> paginateAndMap(List<Event> events, PageRequest pageRequest) {
        List<Event> paginatedEvents = events.stream()
                .skip(pageRequest.getOffset())
                .toList();

        return paginatedEvents.stream()
                .map(EventMapper::toShortDto)
                .toList();
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

    private void applyAdminUpdates(Event event, UpdateEventAdminRequest update) {
        Optional.ofNullable(update.getAnnotation()).ifPresent(event::setAnnotation);
        Optional.ofNullable(update.getDescription()).ifPresent(event::setDescription);
        Optional.ofNullable(update.getEventDate()).ifPresent(event::setEventDate);
        Optional.ofNullable(update.getLocation()).ifPresent(event::setLocation);
        Optional.ofNullable(update.getPaid()).ifPresent(event::setPaid);
        Optional.ofNullable(update.getParticipantLimit()).ifPresent(event::setParticipantLimit);
        Optional.ofNullable(update.getRequestModeration()).ifPresent(event::setRequestModeration);
        Optional.ofNullable(update.getTitle()).ifPresent(event::setTitle);

        Optional.ofNullable(update.getCategory())
                .map(categoryId -> categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new ValidationException("Не найдена категория с ID: " + categoryId)))
                .ifPresent(event::setCategory);
        Optional.ofNullable(update.getStateAction())
                .ifPresent(action -> handleStateUpdateEventAdminRequest(action, event));
    }

    private void applyUserUpdates(Event event, UpdateEventUserRequest update) {

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

    private Map<String, List<ParticipationRequestDto>> processStatusSpecificLogic(Event event,
                                                                                  List<Request> requests,
                                                                                  RequestStatus status) {
        if (status == RequestStatus.REJECTED) {
            return processRejection(requests);
        } else {
            return processConfirmation(event, requests);
        }
    }

    private List<Request> getAndValidateRequests(Long eventId, List<Long> requestIds) {
        List<Request> requests = requestRepository.findRequestByIdIn(requestIds);
        eventValidator.validateRequestsBelongToEvent(requests, eventId);
        return requests;
    }

    private Map<String, List<ParticipationRequestDto>> processRejection(List<Request> requests) {
        eventValidator.validateNoConfirmedRequests(requests);
        updateRequestStatuses(requests, RequestStatus.REJECTED);
        List<ParticipationRequestDto> rejectedRequests = requestRepository.saveAll(requests)
                .stream()
                .map(RequestMapper::toRequestDto)
                .toList();

        return Map.of("rejectedRequests", rejectedRequests);
    }

    private void updateRequestStatuses(List<Request> requests, RequestStatus status) {
        RequestStatusEntity requestStatusEntity =
                requestStatusRepository.findByName(status)
                        .orElseThrow(() -> new IllegalArgumentException("Не верный статус"));
        requests.forEach(request -> request.setStatus(requestStatusEntity));
    }

    private Map<String, List<ParticipationRequestDto>> processConfirmation(Event event, List<Request> requests) {
        eventValidator.validateAllRequestsPending(requests);

        int availableSlots = event.getParticipantLimit() - event.getConfirmedRequests();
        List<Request> confirmed = requests.stream().limit(availableSlots).toList();
        List<Request> rejected = requests.stream().skip(availableSlots).toList();

        updateRequestStatuses(confirmed, RequestStatus.CONFIRMED);
        updateRequestStatuses(rejected, RequestStatus.REJECTED);

        requestRepository.saveAll(requests);
        updateEventConfirmedRequests(event, confirmed.size());

        return Map.of(
                "confirmedRequests", mapToParticipationRequestDtoList(confirmed),
                "rejectedRequests", mapToParticipationRequestDtoList(rejected)
        );
    }

    private void updateEventConfirmedRequests(Event event, int newConfirmations) {
        event.setConfirmedRequests(event.getConfirmedRequests() + newConfirmations);
        eventRepository.save(event);
    }

    private List<ParticipationRequestDto> mapToParticipationRequestDtoList(List<Request> requests) {
        return requests.stream()
                .map(RequestMapper::toRequestDto)
                .toList();
    }

}
