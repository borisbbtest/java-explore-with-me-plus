package ru.practicum.compilation.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateCompilationRequest {
    private List<Long> events;
    private Boolean pinned;
    @Size(min = 1, max = 50, message = "Заголовок подборки должен быть от 1 до 50 символов")
    private String title;
}
