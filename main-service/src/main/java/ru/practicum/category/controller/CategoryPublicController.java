package ru.practicum.category.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryPublicController {

    private final CategoryService categoryService;

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllPaged(
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<CategoryDto> categories = categoryService.getAllPaged(from, size);
        return ResponseEntity.ok(categories);
    }

}

