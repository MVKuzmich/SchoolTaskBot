package com.kuzmich.schoolbot.context;

import com.kuzmich.schoolbot.core.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты {@link SchoolBotUserContextService}: getOrCreate, get, save, clear.
 * Репозиторий и маппер мокаются. Null-safety: тесты на null userId и null context.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class SchoolBotUserContextServiceTest {

    private static final Long USER_ID = 100L;

    @Mock
    private UserContextRepository repository;

    @Mock
    private UserContextMapper mapper;

    private SchoolBotUserContextService service;

    @BeforeEach
    void setUp() {
        service = new SchoolBotUserContextService(repository, mapper);
    }

    @Test
    @DisplayName("getOrCreate: при отсутствии записи создаёт и сохраняет новый контекст")
    void getOrCreate_whenNoEntity_createsAndSavesContext() {
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        UserContext result = service.getOrCreate(USER_ID);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(USER_ID);
        verify(repository).save(any(UserContextEntity.class));
    }

    @Test
    @DisplayName("getOrCreate: при null userId выбрасывает ValidationException")
    void getOrCreate_whenUserIdNull_throws() {
        assertThatThrownBy(() -> service.getOrCreate(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("userId");
    }

    @Test
    @DisplayName("get: при отсутствии записи возвращает Optional.empty()")
    void get_whenNoEntity_returnsEmpty() {
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        Optional<UserContext> result = service.get(USER_ID);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("get: при null userId возвращает Optional.empty()")
    void get_whenUserIdNull_returnsEmpty() {
        Optional<UserContext> result = service.get(null);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("save: при null context выбрасывает ValidationException")
    void save_whenContextNull_throws() {
        assertThatThrownBy(() -> service.save(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("context");
    }

    @Test
    @DisplayName("clear: вызывает deleteByUserId")
    void clear_callsDeleteByUserId() {
        service.clear(USER_ID);
        verify(repository).deleteByUserId(USER_ID);
    }

    @Test
    @DisplayName("clear: при null userId не вызывает репозиторий")
    void clear_whenUserIdNull_doesNothing() {
        service.clear(null);
        verify(repository, never()).deleteByUserId(any());
    }
}
