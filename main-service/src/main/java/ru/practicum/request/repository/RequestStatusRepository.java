package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.model.RequestStatusEntity;

import java.util.Optional;

@Repository
public interface RequestStatusRepository extends JpaRepository<RequestStatusEntity, Long> {
    Optional<RequestStatusEntity> findByName(RequestStatus name);
}
