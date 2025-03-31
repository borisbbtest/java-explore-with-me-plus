package ru.practicum.compilation.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class CompilationMapper {

    public CompilationDto toDto(Compilation compilation) {
        List<EventShortDto> eventDtos = compilation.getEvents().stream()
                .map(EventMapper::toShortDto)
                .collect(Collectors.toList());

        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.isPinned())
                .events(eventDtos)
                .build();
    }

    public Compilation toEntity(NewCompilationDto dto, Set<Event> events) {
        return Compilation.builder()
                .title(dto.getTitle())
                .pinned(dto.isPinned())
                .events(events)
                .build();
    }
}