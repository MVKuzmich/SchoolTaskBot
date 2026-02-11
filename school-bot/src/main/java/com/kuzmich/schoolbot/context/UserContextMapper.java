package com.kuzmich.schoolbot.context;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Маппер между {@link UserContextEntity} и {@link UserContext}.
 * Генерируется MapStruct на этапе компиляции.
 */
@Mapper(componentModel = "spring")
public interface UserContextMapper {

    /**
     * Преобразовать сущность в доменный контекст.
     */
    UserContext toContext(UserContextEntity entity);

    /**
     * Скопировать поля контекста в сущность (без изменения userId и полей согласия с политикой).
     */
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "privacyConsentAt", ignore = true)
    @Mapping(target = "privacyPolicyVersion", ignore = true)
    void updateEntity(UserContext context, @MappingTarget UserContextEntity entity);
}
