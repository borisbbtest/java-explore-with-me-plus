package ru.practicum.compilation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.service.CompilationService;

import java.util.List;

@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
public class PublicCompilationController {

    private final CompilationService compilationService;

    @GetMapping
    public List<CompilationDto> getCompilations(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        return compilationService.getCompilations(pinned, from, size);
    }

    @GetMapping("/{compId}")
    public CompilationDto getCompilationById(@PathVariable Long compId) {
        return compilationService.getCompilationById(compId);
    }
}
