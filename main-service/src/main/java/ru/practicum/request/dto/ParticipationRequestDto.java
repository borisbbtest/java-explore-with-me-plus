package ru.practicum.request.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import ru.practicum.request.model.RequestStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class ParticipationRequestDto {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime created;
    Long event;
    Long id;
    Long requester;
    RequestStatus status;
}
