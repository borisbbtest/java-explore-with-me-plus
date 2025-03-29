package ru.practicum.request.dto;


import lombok.Builder;
import lombok.Data;
import ru.practicum.request.model.RequestStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class RequestDto {

    Long id;

    Long requester;

    Long event;

    RequestStatus status;

    LocalDateTime created;
}
