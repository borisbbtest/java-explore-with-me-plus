package ru.practicum.event.dto;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.practicum.event.model.Location;
import ru.practicum.event.model.StateAction;

import java.time.LocalDateTime;

@Data
public class EventUpdateUserDto {

    @Size(min = 3, max = 120)
    String title;

    @Size(min = 20, max = 2000)
    String annotation;

    @Size(min = 20, max = 7000)
    String description;

    @PositiveOrZero
    Long category;

    @PositiveOrZero
    Integer participantLimit;

    Boolean paid;

    Location location;

    LocalDateTime eventDate;

    Boolean requestModeration;

    StateAction stateAction;
}
