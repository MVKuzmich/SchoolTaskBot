package com.kuzmich.schoolbot.handler;

import com.kuzmich.schoolbot.core.i18n.StartMessageKeys;
import com.kuzmich.schoolbot.core.service.MessageService;
import com.kuzmich.schoolbot.core.service.UserContextService;
import com.kuzmich.schoolbot.core.service.UserStateService;
import com.kuzmich.schoolbot.context.UserContext;
import com.kuzmich.schoolbot.i18n.GeneratorMessageKeys;
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
 * Unit-тесты {@link GeneratorCallbackHandler}: canHandle, маршрутизация callback, вызовы state/context и messageService.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class GeneratorCallbackHandlerTest {

    private static final Long CHAT_ID = 100L;
    private static final Long USER_ID = 200L;
    private static final String QUERY_ID = "query-1";

    @Mock
    private MessageService messageService;
    @Mock
    private UserStateService userStateService;
    @Mock
    private UserContextService<UserContext> userContextService;
    @Mock
    private TelegramClient client;

    private GeneratorCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GeneratorCallbackHandler(messageService, userStateService, userContextService);
    }

    @Test
    @DisplayName("canHandle: true для callback mode_generator")
    void canHandle_returnsTrueForModeGenerator() {
        var update = org.mockito.Mockito.mock(org.telegram.telegrambots.meta.api.objects.Update.class);
        var callback = org.mockito.Mockito.mock(org.telegram.telegrambots.meta.api.objects.CallbackQuery.class);
        org.mockito.Mockito.when(update.getCallbackQuery()).thenReturn(callback);
        org.mockito.Mockito.when(callback.getData()).thenReturn(CallbackData.MODE_GENERATOR);
        assertThat(handler.canHandle(update)).isTrue();
    }

    @Test
    @DisplayName("canHandle: false для пустого data")
    void canHandle_returnsFalseForBlankData() {
        var update = org.mockito.Mockito.mock(org.telegram.telegrambots.meta.api.objects.Update.class);
        var callback = org.mockito.Mockito.mock(org.telegram.telegrambots.meta.api.objects.CallbackQuery.class);
        org.mockito.Mockito.when(update.getCallbackQuery()).thenReturn(callback);
        org.mockito.Mockito.when(callback.getData()).thenReturn("   ");
        assertThat(handler.canHandle(update)).isFalse();
    }

    @Test
    @DisplayName("handle mode_generator: устанавливает режим, AWAITING_SCHOOL_LEVEL, отправляет клавиатуру класса")
    void handle_modeGenerator_setsContextAndShowsClassKeyboard() {
        var update = com.kuzmich.schoolbot.testutil.UpdateFactory.callbackUpdate(CHAT_ID, USER_ID, CallbackData.MODE_GENERATOR, QUERY_ID);
        when(messageService.getText(GeneratorMessageKeys.BUTTON_BACK)).thenReturn("◀️ Назад");
        when(messageService.getText(GeneratorMessageKeys.BUTTON_HELP)).thenReturn("ℹ️ Справка");
        UserContext ctx = new UserContext(USER_ID);
        when(userContextService.getOrCreate(USER_ID)).thenReturn(ctx);

        handler.handle(client, update);

        verify(userContextService).getOrCreate(USER_ID);
        verify(userContextService).save(ctx);
        verify(userStateService).setState(USER_ID, UserState.AWAITING_SCHOOL_LEVEL);
        verify(messageService).sendFromKey(eq(client), eq(CHAT_ID), eq(GeneratorMessageKeys.GENERATOR_CLASS_TITLE), any(InlineKeyboardMarkup.class));
    }

    @Test
    @DisplayName("handle menu: переводит в AWAITING_MODE и показывает выбор режима")
    void handle_menu_setsAwaitingModeAndShowsModeKeyboard() {
        var update = com.kuzmich.schoolbot.testutil.UpdateFactory.callbackUpdate(CHAT_ID, USER_ID, CallbackData.MENU, QUERY_ID);

        handler.handle(client, update);

        verify(userStateService).setState(USER_ID, UserState.AWAITING_MODE);
        verify(messageService).sendFromKey(eq(client), eq(CHAT_ID), eq(StartMessageKeys.START_MESSAGE), any(InlineKeyboardMarkup.class));
    }
}
