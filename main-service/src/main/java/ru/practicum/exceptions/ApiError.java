package ru.practicum.exceptions;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    private LocalDateTime timestamp;
    private int status;
    private String message;
    private List<String> errors;
}