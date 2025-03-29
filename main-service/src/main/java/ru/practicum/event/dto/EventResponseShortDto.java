package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.user.dto.UserDto;

import java.time.LocalDateTime;

@Data
@Builder
public class EventResponseShortDto {

    String annotation;

    CategoryDto category;

    Integer confirmedRequests;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;

    Long id;

    UserDto initiator;

    Boolean paid;

    String title;

    Integer views;
}
