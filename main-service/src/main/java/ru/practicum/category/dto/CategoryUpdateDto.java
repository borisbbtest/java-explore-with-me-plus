package ru.practicum.category.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryUpdateDto {
    @NotBlank(message = "Название категории не может быть пустым")
    private String name;
}
