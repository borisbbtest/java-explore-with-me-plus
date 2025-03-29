package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.*;
import lombok.Data;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.Location;
import ru.practicum.validation.NotBeforeHours;

import java.time.LocalDateTime;

@Data
public class EventRequestDto {

    @NotBlank
    @Size(min = 20, max = 2000)
    String annotation;

    @NotNull
    Long category;

    @NotBlank
    @Size(min = 20, max = 7000)
    String description;

    @NotNull
    @Future
    @NotBeforeHours
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;

    @NotNull
    Location location;

    @JsonSetter(nulls = Nulls.SKIP)
    Boolean paid = false;

    @PositiveOrZero
    @JsonSetter(nulls = Nulls.SKIP)
    Integer participantLimit = 0;

    @JsonSetter(nulls = Nulls.SKIP)
    Boolean requestModeration = true;

    EventState state;

    @NotBlank
    @Size(min = 3, max = 120)
    String title;
}