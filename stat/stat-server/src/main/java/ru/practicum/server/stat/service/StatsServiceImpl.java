package ru.practicum.server.stat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.server.stat.mapper.StatsMapper;
import ru.practicum.server.stat.model.App;
import ru.practicum.server.stat.model.EndpointHit;
import ru.practicum.server.stat.model.Uri;
import ru.practicum.server.stat.repository.AppRepository;
import ru.practicum.server.stat.repository.UriRepository;
import ru.practicum.stat.dto.EndpointHitDto;
import ru.practicum.stat.dto.ViewStatsDto;
import ru.practicum.server.stat.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;
    private final AppRepository appRepository;
    private final UriRepository uriRepository;

    @Transactional
    @Override
    public void saveHit(EndpointHitDto endpointHitDto) {
        // Получаем приложение или создаем новое
        App app = appRepository.findByName(endpointHitDto.getApp())
                .orElseGet(() -> appRepository.save(new App(null, endpointHitDto.getApp())));

        // Получаем URI или создаем новый
        Uri uri = uriRepository.findByUri(endpointHitDto.getUri())
                .orElseGet(() -> uriRepository.save(new Uri(null, endpointHitDto.getUri())));

        // Преобразуем DTO в Entity и сохраняем
        EndpointHit hit = StatsMapper.toEntity(endpointHitDto, app, uri);
        statsRepository.save(hit);
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris) {
        return statsRepository.getStats(start, end, uris);
    }
}