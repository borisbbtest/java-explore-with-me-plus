package ru.practicum.request.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.event.model.Event;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "participation_requests")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode()
@Builder
public class Request {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "requester_id")
    private User requester;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne
    @JoinColumn(name = "status_id", nullable = false)
    private RequestStatusEntity status;

    @Column(name = "created")
    private LocalDateTime created;
}

