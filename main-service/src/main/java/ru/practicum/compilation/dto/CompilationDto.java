package ru.practicum.compilation.dto;

import lombok.Data;

import java.util.List;

@Data
public class CompilationDto {
    private Long id;
    private String title;
    private boolean pinned;
    /*private List<EventShortDto> events;*/
}
