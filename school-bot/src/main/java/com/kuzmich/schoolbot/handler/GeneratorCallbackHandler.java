package com.kuzmich.schoolbot.handler;

import com.kuzmich.schoolbot.context.UserContext;
import com.kuzmich.schoolbot.core.handler.callback.CallbackQueryHandler;
import com.kuzmich.schoolbot.core.i18n.StartMessageKeys;
import com.kuzmich.schoolbot.core.service.MessageService;
import com.kuzmich.schoolbot.core.service.UserContextService;
import com.kuzmich.schoolbot.core.service.UserStateService;
import com.kuzmich.schoolbot.core.validation.Validation;
import com.kuzmich.schoolbot.domain.Mode;
import com.kuzmich.schoolbot.i18n.GeneratorMessageKeys;
import com.kuzmich.schoolbot.domain.SchoolLevel;
import com.kuzmich.schoolbot.domain.Subject;
import com.kuzmich.schoolbot.state.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Обработчик callback от inline-кнопок сценария генератора: выбор режима, класса, предмета,
 * кнопки «Назад» и «Главное меню». Соответствует MathBot-Scenarios (режим → класс → предмет).
 */
@Component
@RequiredArgsConstructor
public class GeneratorCallbackHandler implements CallbackQueryHandler {

    private final MessageService messageService;
    private final UserStateService userStateService;
    private final UserContextService<UserContext> userContextService;

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
        String data = Validation.requireOneOf(update.getCallbackQuery().getData(), "callbackData",
                CallbackData.MODE_GENERATOR, CallbackData.MODE_TRAINER, CallbackData.GEN_ELEMENTARY,
                CallbackData.GEN_SECONDARY, CallbackData.SUBJECT_MATH, CallbackData.BACK_TO_MODE,
                CallbackData.BACK_TO_CLASS, CallbackData.MENU, "help");
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
            default -> handleHelp(client, chatId);
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

    private void handleModeGenerator(TelegramClient client, Long chatId, Long userId) {
        UserContext ctx = userContextService.getOrCreate(userId);
        ctx.setMode(Mode.GENERATOR);
        userContextService.save(ctx);
        userStateService.setState(userId, UserState.AWAITING_SCHOOL_LEVEL);
        String back = messageService.getText(GeneratorMessageKeys.BUTTON_BACK);
        String help = messageService.getText(GeneratorMessageKeys.BUTTON_HELP);
        messageService.sendFromKey(client, chatId, GeneratorMessageKeys.GENERATOR_CLASS_TITLE,
                GeneratorKeyboardFactory.classSelectionKeyboard(back, help));
    }

    private void handleModeTrainer(TelegramClient client, Long chatId) {
        messageService.sendFromKey(client, chatId, GeneratorMessageKeys.TRAINER_COMING_SOON,
                GeneratorKeyboardFactory.mainMenuOnlyKeyboard(messageService.getText(GeneratorMessageKeys.BUTTON_MENU)));
    }

    private void handleGenElementary(TelegramClient client, Long chatId, Long userId) {
        UserContext ctx = userContextService.getOrCreate(userId);
        ctx.setSchoolLevel(SchoolLevel.ELEMENTARY);
        userContextService.save(ctx);
        userStateService.setState(userId, UserState.AWAITING_SUBJECT);
        showSubjectSelection(client, chatId, messageService.getText(GeneratorMessageKeys.CLASS_ELEMENTARY));
    }

    private void handleGenSecondary(TelegramClient client, Long chatId, Long userId) {
        UserContext ctx = userContextService.getOrCreate(userId);
        ctx.setSchoolLevel(SchoolLevel.SECONDARY);
        userContextService.save(ctx);
        userStateService.setState(userId, UserState.AWAITING_SUBJECT);
        showSubjectSelection(client, chatId, messageService.getText(GeneratorMessageKeys.CLASS_SECONDARY));
    }

    private void showSubjectSelection(TelegramClient client, Long chatId, String schoolLevelLabel) {
        String mathLabel = messageService.getText(GeneratorMessageKeys.SUBJECT_MATH);
        String back = messageService.getText(GeneratorMessageKeys.BUTTON_BACK);
        String menu = messageService.getText(GeneratorMessageKeys.BUTTON_MENU);
        messageService.sendFromKey(client, chatId, GeneratorMessageKeys.GENERATOR_SUBJECT_TITLE,
                GeneratorKeyboardFactory.subjectSelectionKeyboard(mathLabel, back, menu),
                schoolLevelLabel);
    }

    private void handleSubjectMath(TelegramClient client, Long chatId, Long userId) {
        UserContext ctx = userContextService.getOrCreate(userId);
        ctx.setSubject(Subject.MATH);
        userContextService.save(ctx);
        userStateService.setState(userId, UserState.AWAITING_TOPIC);
        messageService.sendFromKey(client, chatId, GeneratorMessageKeys.TOPIC_COMING,
                GeneratorKeyboardFactory.mainMenuOnlyKeyboard(messageService.getText(GeneratorMessageKeys.BUTTON_MENU)));
    }

    private void handleBackToMode(TelegramClient client, Long chatId, Long userId) {
        showModeSelection(client, chatId, userId);
    }

    private void handleBackToClass(TelegramClient client, Long chatId, Long userId) {
        userStateService.setState(userId, UserState.AWAITING_SCHOOL_LEVEL);
        String back = messageService.getText(GeneratorMessageKeys.BUTTON_BACK);
        String help = messageService.getText(GeneratorMessageKeys.BUTTON_HELP);
        messageService.sendFromKey(client, chatId, GeneratorMessageKeys.GENERATOR_CLASS_TITLE,
                GeneratorKeyboardFactory.classSelectionKeyboard(back, help));
    }

    private void handleMenu(TelegramClient client, Long chatId, Long userId) {
        showModeSelection(client, chatId, userId);
    }

    private void showModeSelection(TelegramClient client, Long chatId, Long userId) {
        userStateService.setState(userId, UserState.AWAITING_MODE);
        messageService.sendFromKey(client, chatId, StartMessageKeys.START_MESSAGE,
                GeneratorKeyboardFactory.modeSelectionKeyboard());
    }

    private void handleHelp(TelegramClient client, Long chatId) {
        messageService.sendFromKey(client, chatId, GeneratorMessageKeys.HELP_MESSAGE,
                GeneratorKeyboardFactory.mainMenuOnlyKeyboard(messageService.getText(GeneratorMessageKeys.BUTTON_MENU)));
    }
}
