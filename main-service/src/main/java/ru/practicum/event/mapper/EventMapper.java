package ru.practicum.event.mapper;

import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

public class EventMapper {

    public static Event toEvent(NewEventDto newEventDto,
                                User initiator,
                                Category category) {

        return Event.builder()
                .initiator(initiator)
                .annotation(newEventDto.getAnnotation())
                .category(category)
                .description(newEventDto.getDescription())
                .eventDate(newEventDto.getEventDate())
                .location(newEventDto.getLocation())
                .paid(newEventDto.getPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .requestModeration(newEventDto.getRequestModeration())
                .title(newEventDto.getTitle())
                .createdOn(LocalDateTime.now())
                .state(EventState.PENDING)
                .confirmedRequests(0)
                .build();
    }

    public static EventShortDto toShortDto(Event event) {
        return EventShortDto.builder()
                .annotation(event.getAnnotation())
                .category(new CategoryDto(
                        event.getCategory().getId(),
                        event.getCategory().getName()))
                .confirmedRequests(event.getConfirmedRequests())
                .eventDate(event.getEventDate())
                .id(event.getId())
                .initiator(new UserDto(
                        event.getInitiator().getId(),
                        event.getInitiator().getName(),
                        "email"))
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(0L)
                .build();
    }

    public static EventFullDto toFullDto(Event event) {
        return EventFullDto.builder()
                .annotation(event.getAnnotation())
                .category(new CategoryDto(
                        event.getCategory().getId(),
                        event.getCategory().getName()))
                .confirmedRequests(event.getConfirmedRequests())
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .id(event.getId())
                .initiator(new UserDto(
                        event.getInitiator().getId(),
                        event.getInitiator().getName(),
                        "email"))
                .location(event.getLocation())
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .views(0L)
                .build();
    }
}
