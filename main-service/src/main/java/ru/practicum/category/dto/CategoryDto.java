package ru.practicum.category.dto;

import jakarta.validation.constraints.Size;
import lombok.*;
import ru.practicum.validation.NotOnlySpaces;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDto {
    private Long id;
    @Size(min = 1, max = 50, message = "Category name must be between 1 and 50 characters")
    @NotOnlySpaces
    private String name;
}
