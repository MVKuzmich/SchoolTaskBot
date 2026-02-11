package com.kuzmich.schoolbot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Базовая конфигурация Jackson для приложения.
 * <p>
 * В некоторых профилях / стартах (например, без web-starter) автонастройка Spring Boot
 * может не создать {@link ObjectMapper}. Явно объявляем бин, чтобы он был доступен
 * для сервисов, которым нужна сериализация в JSON (например, FeatureConfigAuditService).
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        // При необходимости сюда можно добавить дополнительные модули / настройки
        return new ObjectMapper();
    }
}

