package ru.practicum.event.dto;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.PageRequest;

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
