package ru.practicum.server.stat.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "uris", uniqueConstraints = @UniqueConstraint(columnNames = "uri"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Uri {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 500)
    private String uri;
}
