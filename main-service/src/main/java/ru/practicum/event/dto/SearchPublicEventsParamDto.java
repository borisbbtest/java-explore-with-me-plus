package ru.practicum.event.dto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.event.model.EventSort;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SearchPublicEventsParamDto {
    String text;
    List<Long> categoriesIds;
    Boolean paid;
    LocalDateTime rangeStart;
    LocalDateTime rangeEnd;
    boolean onlyAvailable;
    PageRequest pageRequest;
    HttpServletRequest request;
}
