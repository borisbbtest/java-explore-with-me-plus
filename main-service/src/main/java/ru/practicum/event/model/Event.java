package ru.practicum.event.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.category.model.Category;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode()
@Builder
public class Event {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "title")
    String title;

    @Column(name = "annotation", columnDefinition = "TEXT")
    String annotation;

    @ManyToOne
    @JoinColumn(name = "category_id")
    Category category;

    @Column(name = "paid")
    Boolean paid;

    @Column(name = "event_date")
    LocalDateTime eventDate;

    @ManyToOne
    @JoinColumn(name = "initiator_id")
    User initiator;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Column(name = "participant_limit")
    Integer participantLimit;

    @Enumerated(EnumType.STRING)
    EventState state;

    @Column(name = "created_on")
    LocalDateTime createdOn;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "location_id")
    Location location;

    @Column(name = "request_moderation")
    Boolean requestModeration;

    @Column(name = "published_on")
    LocalDateTime publishedOn;

    @Column(name = "confirmed_requests")
    Integer confirmedRequests;
}
