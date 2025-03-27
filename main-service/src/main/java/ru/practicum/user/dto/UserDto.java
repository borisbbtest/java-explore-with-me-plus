package ru.practicum.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private Long id;

    @NotBlank(message = "Name must not be blank")
    @Size(min = 2, max = 250, message = "Name must be between 2 and 250 characters long")
    private String name;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email must not be blank")
    @Size(min = 6, max = 254, message = "Name must be between 2 and 250 characters long")
    private String email;
}
