package ru.practicum.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.practicum.validation.NotOnlySpaces;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryUpdateDto {

    @NotBlank(message = "Название категории не может быть пустым")
    @Size(min = 1, max = 50, message = "Категория должна быть от 1 до 50 символов")
    @NotOnlySpaces
    private String name;
}
