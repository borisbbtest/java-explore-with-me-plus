package ru.practicum.comment.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.event.model.Event;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Comment {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "text", nullable = false)
    private String text;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    @Column(name = "created")
    private LocalDateTime created;
}
