package com.kuzmich.schoolbot.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.MessageSource;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты {@link MessageService}: построение и отправка сообщений по ключу и тексту.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class MessageServiceTest {

    private static final Long CHAT_ID = 12345L;
    private static final String MESSAGE_KEY = "start.message";
    private static final String RESOLVED_TEXT = "Привет! Я бот.";

    @Mock
    private MessageSource messageSource;

    @Mock
    private TelegramClient client;

    private MessageService messageService;

    @BeforeEach
    void setUp() {
        messageService = new MessageService(messageSource);
    }

    @Test
    @DisplayName("buildText: возвращает SendMessage с chatId и текстом")
    void buildText_returnsSendMessageWithChatIdAndText() {
        String text = "Hello";

        SendMessage result = messageService.buildText(CHAT_ID, text);

        assertThat(result).isNotNull();
        assertThat(result.getChatId()).isEqualTo(CHAT_ID.toString());
        assertThat(result.getText()).isEqualTo(text);
    }

    @Test
    @DisplayName("buildFromKey: резолвит ключ через MessageSource и собирает SendMessage")
    void buildFromKey_resolvesKeyAndBuildsSendMessage() {
        when(messageSource.getMessage(eq(MESSAGE_KEY), any(), eq(Locale.getDefault())))
                .thenReturn(RESOLVED_TEXT);

        SendMessage result = messageService.buildFromKey(CHAT_ID, MESSAGE_KEY);

        assertThat(result).isNotNull();
        assertThat(result.getChatId()).isEqualTo(CHAT_ID.toString());
        assertThat(result.getText()).isEqualTo(RESOLVED_TEXT);
    }

    @Test
    @DisplayName("buildFromKey с клавиатурой: текст по ключу и replyMarkup")
    void buildFromKey_withMarkup_includesReplyMarkup() {
        when(messageSource.getMessage(eq(MESSAGE_KEY), any(), eq(Locale.getDefault())))
                .thenReturn(RESOLVED_TEXT);
        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder().build();

        SendMessage result = messageService.buildFromKey(CHAT_ID, MESSAGE_KEY, markup);

        assertThat(result).isNotNull();
        assertThat(result.getText()).isEqualTo(RESOLVED_TEXT);
        assertThat(result.getReplyMarkup()).isSameAs(markup);
    }

    @Test
    @DisplayName("sendFromKey: вызывает client.execute с SendMessage по ключу")
    void sendFromKey_executesClientWithResolvedMessage() throws TelegramApiException {
        when(messageSource.getMessage(eq(MESSAGE_KEY), any(), eq(Locale.getDefault())))
                .thenReturn(RESOLVED_TEXT);

        messageService.sendFromKey(client, CHAT_ID, MESSAGE_KEY);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(client).execute(captor.capture());
        SendMessage sent = captor.getValue();
        assertThat(sent.getChatId()).isEqualTo(CHAT_ID.toString());
        assertThat(sent.getText()).isEqualTo(RESOLVED_TEXT);
    }
}
