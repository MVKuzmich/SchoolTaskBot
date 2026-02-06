package com.kuzmich.schoolbot.handler;

import com.kuzmich.schoolbot.context.UserContext;
import com.kuzmich.schoolbot.core.handler.callback.CallbackQueryHandler;
import com.kuzmich.schoolbot.core.service.MessageService;
import com.kuzmich.schoolbot.core.service.UserContextService;
import com.kuzmich.schoolbot.core.service.UserStateService;
import com.kuzmich.schoolbot.domain.Mode;
import com.kuzmich.schoolbot.domain.SchoolLevel;
import com.kuzmich.schoolbot.domain.Subject;
import com.kuzmich.schoolbot.state.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Locale;
import java.util.Objects;

/**
 * Обработчик callback от inline-кнопок сценария генератора: выбор режима, класса, предмета,
 * кнопки «Назад» и «Главное меню». Соответствует MathBot-Scenarios (режим → класс → предмет).
 */
@Component
@RequiredArgsConstructor
public class GeneratorCallbackHandler implements CallbackQueryHandler {

    private static final String MESSAGE_KEY_START = "start.message";
    private static final String MESSAGE_KEY_GENERATOR_CLASS = "generator.class.title";
    private static final String MESSAGE_KEY_GENERATOR_SUBJECT = "generator.subject.title";
    private static final String MESSAGE_KEY_TRAINER_COMING_SOON = "trainer.coming.soon";
    private static final String MESSAGE_KEY_BUTTON_BACK = "button.back";
    private static final String MESSAGE_KEY_BUTTON_MENU = "button.menu";
    private static final String MESSAGE_KEY_BUTTON_HELP = "button.help";
    private static final String MESSAGE_KEY_CLASS_ELEMENTARY = "generator.class.elementary";
    private static final String MESSAGE_KEY_CLASS_SECONDARY = "generator.class.secondary";
    private static final String MESSAGE_KEY_SUBJECT_MATH = "generator.subject.math";
    private static final String MESSAGE_KEY_TOPIC_COMING = "generator.topic.coming";
    private static final String MESSAGE_KEY_HELP = "help.message";

    private final MessageService messageService;
    private final UserStateService userStateService;
    private final UserContextService<UserContext> userContextService;
    private final MessageSource messageSource;

    @Override
    public boolean canHandle(Update update) {
        if (update.getCallbackQuery() == null) {
            return false;
        }
        String data = update.getCallbackQuery().getData();
        if (data == null || data.isBlank()) {
            return false;
        }
        return data.startsWith("mode_") || data.startsWith("gen_")
                || data.startsWith("subject_") || CallbackData.BACK_TO_MODE.equals(data)
                || CallbackData.BACK_TO_CLASS.equals(data) || CallbackData.MENU.equals(data)
                || "help".equals(data);
    }

    @Override
    public void handle(TelegramClient client, Update update) {
        String data = Objects.requireNonNull(update.getCallbackQuery().getData(), "callback data");
        Long chatId = update.getCallbackQuery().getMessage() != null
                ? update.getCallbackQuery().getMessage().getChatId()
                : null;
        Long userId = update.getCallbackQuery().getFrom() != null
                ? update.getCallbackQuery().getFrom().getId()
                : null;
        String callbackQueryId = update.getCallbackQuery().getId();

        if (chatId == null || userId == null) {
            answerCallback(client, callbackQueryId);
            return;
        }

        answerCallback(client, callbackQueryId);

        switch (data) {
            case CallbackData.MODE_GENERATOR -> handleModeGenerator(client, chatId, userId);
            case CallbackData.MODE_TRAINER -> handleModeTrainer(client, chatId);
            case CallbackData.GEN_ELEMENTARY -> handleGenElementary(client, chatId, userId);
            case CallbackData.GEN_SECONDARY -> handleGenSecondary(client, chatId, userId);
            case CallbackData.SUBJECT_MATH -> handleSubjectMath(client, chatId, userId);
            case CallbackData.BACK_TO_MODE -> handleBackToMode(client, chatId, userId);
            case CallbackData.BACK_TO_CLASS -> handleBackToClass(client, chatId, userId);
            case CallbackData.MENU -> handleMenu(client, chatId, userId);
            default -> handleHelp(client, chatId, userId);
        }
    }

    private void answerCallback(TelegramClient client, String callbackQueryId) {
        if (callbackQueryId == null) {
            return;
        }
        try {
            client.execute(AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQueryId)
                    .build());
        } catch (TelegramApiException e) {
            // Логируем, но не прерываем обработку
        }
    }

    private String msg(String key, Object... args) {
        return messageSource.getMessage(key, args, Locale.getDefault());
    }

    private void handleModeGenerator(TelegramClient client, Long chatId, Long userId) {
        UserContext ctx = userContextService.getOrCreate(userId);
        ctx.setMode(Mode.GENERATOR);
        userContextService.save(ctx);
        userStateService.setState(userId, UserState.AWAITING_SCHOOL_LEVEL);
        String back = msg(MESSAGE_KEY_BUTTON_BACK);
        String help = msg(MESSAGE_KEY_BUTTON_HELP);
        messageService.sendFromKey(client, chatId, MESSAGE_KEY_GENERATOR_CLASS,
                GeneratorKeyboardFactory.classSelectionKeyboard(back, help));
    }

    private void handleModeTrainer(TelegramClient client, Long chatId) {
        messageService.sendFromKey(client, chatId, MESSAGE_KEY_TRAINER_COMING_SOON,
                GeneratorKeyboardFactory.mainMenuOnlyKeyboard(msg(MESSAGE_KEY_BUTTON_MENU)));
    }

    private void handleGenElementary(TelegramClient client, Long chatId, Long userId) {
        UserContext ctx = userContextService.getOrCreate(userId);
        ctx.setSchoolLevel(SchoolLevel.ELEMENTARY);
        userContextService.save(ctx);
        userStateService.setState(userId, UserState.AWAITING_SUBJECT);
        showSubjectSelection(client, chatId, msg(MESSAGE_KEY_CLASS_ELEMENTARY));
    }

    private void handleGenSecondary(TelegramClient client, Long chatId, Long userId) {
        UserContext ctx = userContextService.getOrCreate(userId);
        ctx.setSchoolLevel(SchoolLevel.SECONDARY);
        userContextService.save(ctx);
        userStateService.setState(userId, UserState.AWAITING_SUBJECT);
        showSubjectSelection(client, chatId, msg(MESSAGE_KEY_CLASS_SECONDARY));
    }

    private void showSubjectSelection(TelegramClient client, Long chatId, String schoolLevelLabel) {
        String mathLabel = msg(MESSAGE_KEY_SUBJECT_MATH);
        String back = msg(MESSAGE_KEY_BUTTON_BACK);
        String menu = msg(MESSAGE_KEY_BUTTON_MENU);
        messageService.sendFromKey(client, chatId, MESSAGE_KEY_GENERATOR_SUBJECT,
                GeneratorKeyboardFactory.subjectSelectionKeyboard(mathLabel, back, menu),
                schoolLevelLabel);
    }

    private void handleSubjectMath(TelegramClient client, Long chatId, Long userId) {
        UserContext ctx = userContextService.getOrCreate(userId);
        ctx.setSubject(Subject.MATH);
        userContextService.save(ctx);
        userStateService.setState(userId, UserState.AWAITING_TOPIC);
        messageService.sendFromKey(client, chatId, MESSAGE_KEY_TOPIC_COMING,
                GeneratorKeyboardFactory.mainMenuOnlyKeyboard(msg(MESSAGE_KEY_BUTTON_MENU)));
    }

    private void handleBackToMode(TelegramClient client, Long chatId, Long userId) {
        showModeSelection(client, chatId, userId);
    }

    private void handleBackToClass(TelegramClient client, Long chatId, Long userId) {
        userStateService.setState(userId, UserState.AWAITING_SCHOOL_LEVEL);
        String back = msg(MESSAGE_KEY_BUTTON_BACK);
        String help = msg(MESSAGE_KEY_BUTTON_HELP);
        messageService.sendFromKey(client, chatId, MESSAGE_KEY_GENERATOR_CLASS,
                GeneratorKeyboardFactory.classSelectionKeyboard(back, help));
    }

    private void handleMenu(TelegramClient client, Long chatId, Long userId) {
        showModeSelection(client, chatId, userId);
    }

    private void showModeSelection(TelegramClient client, Long chatId, Long userId) {
        userStateService.setState(userId, UserState.AWAITING_MODE);
        messageService.sendFromKey(client, chatId, MESSAGE_KEY_START,
                GeneratorKeyboardFactory.modeSelectionKeyboard());
    }

    private void handleHelp(TelegramClient client, Long chatId, Long userId) {
        messageService.sendFromKey(client, chatId, MESSAGE_KEY_HELP,
                GeneratorKeyboardFactory.mainMenuOnlyKeyboard(msg(MESSAGE_KEY_BUTTON_MENU)));
    }
}
