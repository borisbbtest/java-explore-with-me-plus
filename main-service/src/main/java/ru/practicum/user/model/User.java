package ru.practicum.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "users",
        uniqueConstraints = {@UniqueConstraint(columnNames = "email")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name must not be blank")
    @Column(nullable = false)
    private String name;

    @Email(message = "Invalid email")
    @NotBlank(message = "Email must not be blank")
    @Column(nullable = false, unique = true)
    private String email;
}
