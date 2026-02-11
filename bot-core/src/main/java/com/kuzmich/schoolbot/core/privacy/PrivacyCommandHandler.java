package com.kuzmich.schoolbot.core.privacy;

import com.kuzmich.schoolbot.core.handler.command.CommandHandler;
import com.kuzmich.schoolbot.core.service.MessageService;
import com.kuzmich.schoolbot.core.service.PrivacyConsentService;
import com.kuzmich.schoolbot.core.validation.Validation;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Обработчик команды /privacy: отправляет пользователю ссылку на политику конфиденциальности.
 * Текст сообщения задаётся ключом privacyMessageKey (аргумент {0} — URL).
 * Бин создаётся в модуле бота.
 */
public class PrivacyCommandHandler implements CommandHandler {

    private static final String COMMAND_PRIVACY = "/privacy";

    private final PrivacyConsentService privacyConsentService;
    private final MessageService messageService;
    private final String privacyMessageKey;

    public PrivacyCommandHandler(PrivacyConsentService privacyConsentService,
                                 MessageService messageService,
                                 String privacyMessageKey) {
        this.privacyConsentService = privacyConsentService;
        this.messageService = messageService;
        this.privacyMessageKey = Validation.requireNonNull(privacyMessageKey, "privacyMessageKey");
    }

    @Override
    public boolean canHandle(Update update) {
        if (update.getMessage() == null || update.getMessage().getText() == null) {
            return false;
        }
        return update.getMessage().getText().trim().equals(COMMAND_PRIVACY);
    }

    @Override
    public void handle(TelegramClient client, Update update) {
        Validation.requireOneOf(update.getMessage().getText(), "command", COMMAND_PRIVACY);
        Long chatId = Validation.requireNonNull(update.getMessage().getChatId(), "chatId");
        String url = privacyConsentService.getPrivacyPolicyUrl();
        messageService.sendFromKey(client, chatId, privacyMessageKey, url);
    }
}
