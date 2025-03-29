package ru.practicum;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StatsClientConfig {

    @Value("${stats-server.url}")
    private String statsServerUrl;

    @Bean
    public StatClient statClient() {
        return new StatClient(statsServerUrl);
    }
}
