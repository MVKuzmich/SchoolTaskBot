package com.kuzmich.schoolbot.handler;

import com.kuzmich.schoolbot.core.i18n.StartMessageKeys;
import com.kuzmich.schoolbot.core.service.MessageService;
import com.kuzmich.schoolbot.core.service.UserContextService;
import com.kuzmich.schoolbot.core.service.UserStateService;
import com.kuzmich.schoolbot.context.UserContext;
import com.kuzmich.schoolbot.state.UserState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты {@link StartCommandHandler}: команда /start, состояние AWAITING_MODE, клавиатура выбора режима.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class StartCommandHandlerTest {

    private static final Long CHAT_ID = 100L;
    private static final Long USER_ID = 200L;

    @Mock
    private MessageService messageService;
    @Mock
    private UserStateService userStateService;
    @Mock
    private UserContextService<UserContext> userContextService;
    @Mock
    private TelegramClient client;

    private StartCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new StartCommandHandler(messageService, userStateService, userContextService);
    }

    @Test
    @DisplayName("canHandle: true для сообщения с текстом /start")
    void canHandle_returnsTrueForStartCommand() {
        var update = org.mockito.Mockito.mock(org.telegram.telegrambots.meta.api.objects.Update.class);
        var message = org.mockito.Mockito.mock(org.telegram.telegrambots.meta.api.objects.message.Message.class);
        org.mockito.Mockito.when(update.getMessage()).thenReturn(message);
        org.mockito.Mockito.when(message.getText()).thenReturn("/start");

        boolean result = handler.canHandle(update);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("canHandle: true для /start с пробелами")
    void canHandle_returnsTrueForStartWithSpaces() {
        var update = org.mockito.Mockito.mock(org.telegram.telegrambots.meta.api.objects.Update.class);
        var message = org.mockito.Mockito.mock(org.telegram.telegrambots.meta.api.objects.message.Message.class);
        org.mockito.Mockito.when(update.getMessage()).thenReturn(message);
        org.mockito.Mockito.when(message.getText()).thenReturn("  /start  ");

        assertThat(handler.canHandle(update)).isTrue();
    }

    @Test
    @DisplayName("canHandle: false если сообщения нет")
    void canHandle_returnsFalseWhenMessageIsNull() {
        var update = new org.telegram.telegrambots.meta.api.objects.Update();
        update.setMessage(null);

        assertThat(handler.canHandle(update)).isFalse();
    }

    @Test
    @DisplayName("handle: устанавливает AWAITING_MODE, getOrCreate контекст, отправляет приветствие с клавиатурой")
    void handle_setsStateAndSendsWelcomeWithKeyboard() {
        var update = org.mockito.Mockito.mock(org.telegram.telegrambots.meta.api.objects.Update.class);
        var message = org.mockito.Mockito.mock(org.telegram.telegrambots.meta.api.objects.message.Message.class);
        var from = org.mockito.Mockito.mock(org.telegram.telegrambots.meta.api.objects.User.class);
        org.mockito.Mockito.when(update.getMessage()).thenReturn(message);
        org.mockito.Mockito.when(message.getChatId()).thenReturn(CHAT_ID);
        org.mockito.Mockito.when(message.getFrom()).thenReturn(from);
        org.mockito.Mockito.when(from.getId()).thenReturn(USER_ID);
        UserContext ctx = new UserContext(USER_ID);
        when(userContextService.getOrCreate(USER_ID)).thenReturn(ctx);

        handler.handle(client, update);

        verify(userStateService).setState(USER_ID, UserState.AWAITING_MODE);
        verify(userContextService).getOrCreate(USER_ID);
        verify(messageService).sendFromKey(eq(client), eq(CHAT_ID), eq(StartMessageKeys.START_MESSAGE), any(InlineKeyboardMarkup.class));
    }

    @Test
    @DisplayName("handle: при null userId отправляет только приветствие без установки состояния")
    void handle_whenUserIdNull_sendsWelcomeOnly() {
        var update = org.mockito.Mockito.mock(org.telegram.telegrambots.meta.api.objects.Update.class);
        var message = org.mockito.Mockito.mock(org.telegram.telegrambots.meta.api.objects.message.Message.class);
        org.mockito.Mockito.when(update.getMessage()).thenReturn(message);
        org.mockito.Mockito.when(message.getChatId()).thenReturn(CHAT_ID);
        org.mockito.Mockito.when(message.getFrom()).thenReturn(null);

        handler.handle(client, update);

        verify(messageService).sendFromKey(client, CHAT_ID, StartMessageKeys.START_MESSAGE);
        // setState и getOrCreate не вызываются при null userId
    }
}
