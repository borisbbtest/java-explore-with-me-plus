package ru.practicum.event.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.event.dto.EventRequestDto;
import ru.practicum.event.dto.EventResponseLongDto;
import ru.practicum.event.dto.EventResponseShortDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

@Component
public class EventMapper {

    public static Event toEvent(EventRequestDto eventRequestDto,
                                User initiator,
                                Category category) {

        return Event.builder()
                .initiator(initiator)
                .annotation(eventRequestDto.getAnnotation())
                .category(category)
                .description(eventRequestDto.getDescription())
                .eventDate(eventRequestDto.getEventDate())
                .location(eventRequestDto.getLocation())
                .paid(eventRequestDto.getPaid())
                .participantLimit(eventRequestDto.getParticipantLimit())
                .requestModeration(eventRequestDto.getRequestModeration())
                .title(eventRequestDto.getTitle())
                .createdOn(LocalDateTime.now())
                .state(EventState.PENDING)
                .confirmedRequests(0)
                .build();
    }

    public static EventResponseShortDto toShortDto(Event event) {
        return EventResponseShortDto.builder()
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
                .views(0)
                .build();
    }

    public static EventResponseLongDto toLongDto(Event event) {
        return EventResponseLongDto.builder()
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
                .views(0)
                .build();
    }
}
