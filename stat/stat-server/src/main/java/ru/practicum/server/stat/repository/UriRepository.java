package ru.practicum.server.stat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.server.stat.model.Uri;

import java.util.Optional;

public interface UriRepository extends JpaRepository<Uri, Long> {
    Optional<Uri> findByUri(String uri);
}