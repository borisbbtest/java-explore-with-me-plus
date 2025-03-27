package ru.practicum.category.service;

import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.CategoryUpdateDto;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(CategoryDto categoryDto);

    List<CategoryDto> getAll();

    CategoryDto getById(Long id);

    void deleteById(Long id);

    List<CategoryDto> getAllPaged(int from, int size);

    CategoryDto update(Long catId, CategoryUpdateDto dto);
}
