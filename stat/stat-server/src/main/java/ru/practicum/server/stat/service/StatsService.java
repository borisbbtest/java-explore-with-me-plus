package ru.practicum.server.stat.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stat.dto.EndpointHitDto;

@Service
@Transactional
public class StatsService {
    public void saveHit(EndpointHitDto endpointHitDto) {
    }
}
