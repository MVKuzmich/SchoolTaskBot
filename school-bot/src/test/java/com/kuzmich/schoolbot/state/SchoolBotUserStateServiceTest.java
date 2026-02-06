package com.kuzmich.schoolbot.state;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты {@link SchoolBotUserStateService}: дефолт INITIAL, setState, clearState, isWaitingForInput.
 * Репозиторий мокается.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class SchoolBotUserStateServiceTest {

    private static final Long USER_ID = 100L;

    @Mock
    private UserStateRepository repository;

    private SchoolBotUserStateService service;

    @BeforeEach
    void setUp() {
        service = new SchoolBotUserStateService(repository);
    }

    @Test
    @DisplayName("getState: при отсутствии записи возвращает INITIAL")
    void getState_whenNoEntity_returnsInitial() {
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        Object state = service.getState(USER_ID);

        assertThat(state).isEqualTo(UserState.INITIAL);
    }

    @Test
    @DisplayName("getState: при null userId возвращает INITIAL")
    void getState_whenUserIdNull_returnsInitial() {
        Object state = service.getState(null);
        assertThat(state).isEqualTo(UserState.INITIAL);
    }

    @Test
    @DisplayName("getState: при наличии записи возвращает состояние из репозитория")
    void getState_whenEntityExists_returnsEntityState() {
        UserStateEntity entity = new UserStateEntity(USER_ID, UserState.AWAITING_MODE);
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.of(entity));

        Object state = service.getState(USER_ID);

        assertThat(state).isEqualTo(UserState.AWAITING_MODE);
    }

    @Test
    @DisplayName("setState: сохраняет новое состояние")
    void setState_savesNewState() {
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        service.setState(USER_ID, UserState.AWAITING_SCHOOL_LEVEL);

        verify(repository).findByUserId(USER_ID);
        verify(repository).save(org.mockito.ArgumentMatchers.argThat(e ->
                e.getUserId().equals(USER_ID) && e.getState() == UserState.AWAITING_SCHOOL_LEVEL
        ));
    }

    @Test
    @DisplayName("setState: при null userId не вызывает репозиторий")
    void setState_whenUserIdNull_doesNothing() {
        service.setState(null, UserState.AWAITING_MODE);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("clearState: устанавливает INITIAL и сохраняет")
    void clearState_setsInitialAndSaves() {
        UserStateEntity entity = new UserStateEntity(USER_ID, UserState.AWAITING_MODE);
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.of(entity));

        service.clearState(USER_ID);

        verify(repository).save(org.mockito.ArgumentMatchers.argThat(e ->
                e.getState() == UserState.INITIAL
        ));
    }

    @Test
    @DisplayName("isWaitingForInput: true для AWAITING_MODE")
    void isWaitingForInput_returnsTrueForAwaitingMode() {
        UserStateEntity entity = new UserStateEntity(USER_ID, UserState.AWAITING_MODE);
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.of(entity));

        boolean result = service.isWaitingForInput(USER_ID);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isWaitingForInput: false для INITIAL")
    void isWaitingForInput_returnsFalseForInitial() {
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        boolean result = service.isWaitingForInput(USER_ID);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isWaitingForInput: false при null userId")
    void isWaitingForInput_whenUserIdNull_returnsFalse() {
        assertThat(service.isWaitingForInput(null)).isFalse();
    }
}
