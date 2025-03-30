package ru.practicum.event.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import ru.practicum.event.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SearchAdminEventsParamDto {
    List<Long> users;
    List<EventState> eventStates;
    List<Long> categoriesIds;
    LocalDateTime rangeStart;
    LocalDateTime rangeEnd;
    PageRequest pageRequest;
}
