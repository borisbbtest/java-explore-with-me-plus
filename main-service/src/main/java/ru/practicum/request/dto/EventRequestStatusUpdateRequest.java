package ru.practicum.request.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.request.model.RequestStatus;

import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {
    @NotNull
    List<Long> requestIds;
    @NotNull
    RequestStatus status;
}
