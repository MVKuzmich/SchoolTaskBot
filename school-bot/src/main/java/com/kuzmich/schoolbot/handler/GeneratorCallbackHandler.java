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
import com.kuzmich.schoolbot.generator.OperationType;
import com.kuzmich.schoolbot.generator.service.PdfGenerationAccessException;
import com.kuzmich.schoolbot.generator.service.PdfGenerationService;
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
    private final PdfGenerationService pdfGenerationService;

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
                || data.startsWith("subject_") || data.startsWith("topic_")
                || data.startsWith("op_") || data.startsWith("qty_")
                || CallbackData.BACK_TO_MODE.equals(data)
                || CallbackData.BACK_TO_CLASS.equals(data) || CallbackData.MENU.equals(data)
                || "help".equals(data);
    }

    @Override
    public void handle(TelegramClient client, Update update) {
        var callbackQuery = Validation.requireNonNull(update.getCallbackQuery(), "callbackQuery");
        String data = Validation.requireOneOf(callbackQuery.getData(), "callbackData",
                CallbackData.MODE_GENERATOR, CallbackData.MODE_TRAINER, CallbackData.GEN_ELEMENTARY,
                CallbackData.GEN_SECONDARY, CallbackData.SUBJECT_MATH, CallbackData.TOPIC_ARITHMETIC,
                CallbackData.OP_ADDITION_10, CallbackData.OP_SUBTRACTION_10,
                CallbackData.OP_ADDITION_20_NO_CARRY, CallbackData.OP_SUBTRACTION_20_NO_CARRY,
                CallbackData.QTY_10, CallbackData.QTY_20, CallbackData.QTY_30, CallbackData.QTY_50,
                CallbackData.GEN_DEMO_PDF, CallbackData.GEN_CONFIRM_PDF,
                CallbackData.BACK_TO_MODE, CallbackData.BACK_TO_CLASS, CallbackData.MENU, "help");
        var message = Validation.requireNonNull(callbackQuery.getMessage(), "message");
        long chatId = Validation.requirePositiveLong(message.getChatId(), "chatId");
        var from = Validation.requireNonNull(callbackQuery.getFrom(), "from");
        long userId = Validation.requirePositiveLong(from.getId(), "userId");
        String callbackQueryId = callbackQuery.getId();

        answerCallback(client, callbackQueryId);

        switch (data) {
            case CallbackData.MODE_GENERATOR -> handleModeGenerator(client, chatId, userId);
            case CallbackData.MODE_TRAINER -> handleModeTrainer(client, chatId);
            case CallbackData.GEN_ELEMENTARY -> handleGenElementary(client, chatId, userId);
            case CallbackData.GEN_SECONDARY -> handleGenSecondary(client, chatId, userId);
            case CallbackData.SUBJECT_MATH -> handleSubjectMath(client, chatId, userId);
            case CallbackData.TOPIC_ARITHMETIC -> handleTopicArithmetic(client, chatId, userId);
            case CallbackData.OP_ADDITION_10,
                    CallbackData.OP_SUBTRACTION_10,
                    CallbackData.OP_ADDITION_20_NO_CARRY,
                    CallbackData.OP_SUBTRACTION_20_NO_CARRY ->
                    handleOperationSelected(client, chatId, userId, data);
            case CallbackData.QTY_10, CallbackData.QTY_20, CallbackData.QTY_30, CallbackData.QTY_50 ->
                    handleQuantitySelected(client, chatId, userId, data);
            case CallbackData.GEN_CONFIRM_PDF -> handleConfirmPdf(client, chatId, userId);
            case CallbackData.GEN_DEMO_PDF -> handleDemoPdf(client, chatId, userId);
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
        String back = messageService.getText(GeneratorMessageKeys.BUTTON_BACK);
        String menu = messageService.getText(GeneratorMessageKeys.BUTTON_MENU);
        messageService.sendFromKey(client, chatId, GeneratorMessageKeys.CLASS_SECONDARY_COMING_SOON,
                GeneratorKeyboardFactory.backAndMenuKeyboard(back, menu));
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
        String arithmetic = messageService.getText(GeneratorMessageKeys.TOPIC_ARITHMETIC);
        String back = messageService.getText(GeneratorMessageKeys.BUTTON_BACK);
        String menu = messageService.getText(GeneratorMessageKeys.BUTTON_MENU);
        messageService.sendFromKey(client, chatId, GeneratorMessageKeys.TOPIC_TITLE,
                GeneratorKeyboardFactory.topicSelectionKeyboard(arithmetic, back, menu));
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

    private void handleTopicArithmetic(TelegramClient client, Long chatId, Long userId) {
        UserContext ctx = userContextService.getOrCreate(userId);
        ctx.setTopic("ARITHMETIC");
        userContextService.save(ctx);
        userStateService.setState(userId, UserState.AWAITING_OPERATION_TYPE);

        String add10 = messageService.getText(GeneratorMessageKeys.OPERATION_ADDITION_10);
        String sub10 = messageService.getText(GeneratorMessageKeys.OPERATION_SUBTRACTION_10);
        String add20 = messageService.getText(GeneratorMessageKeys.OPERATION_ADDITION_20_NO_CARRY);
        String sub20 = messageService.getText(GeneratorMessageKeys.OPERATION_SUBTRACTION_20_NO_CARRY);
        String back = messageService.getText(GeneratorMessageKeys.BUTTON_BACK);
        String menu = messageService.getText(GeneratorMessageKeys.BUTTON_MENU);

        messageService.sendFromKey(client, chatId, GeneratorMessageKeys.OPERATION_TITLE,
                GeneratorKeyboardFactory.operationSelectionKeyboard(add10, sub10, add20, sub20, back, menu));
    }

    private void handleOperationSelected(TelegramClient client, Long chatId, Long userId, String data) {
        UserContext ctx = userContextService.getOrCreate(userId);
        OperationType operationType = mapCallbackToOperationType(data);
        ctx.setOperationType(operationType.name());
        userContextService.save(ctx);
        userStateService.setState(userId, UserState.AWAITING_QUANTITY);

        String qty10 = messageService.getText(GeneratorMessageKeys.QUANTITY_10);
        String qty20 = messageService.getText(GeneratorMessageKeys.QUANTITY_20);
        String qty30 = messageService.getText(GeneratorMessageKeys.QUANTITY_30);
        String qty50 = messageService.getText(GeneratorMessageKeys.QUANTITY_50);
        String back = messageService.getText(GeneratorMessageKeys.BUTTON_BACK);
        String menu = messageService.getText(GeneratorMessageKeys.BUTTON_MENU);

        messageService.sendFromKey(client, chatId, GeneratorMessageKeys.QUANTITY_TITLE,
                GeneratorKeyboardFactory.quantitySelectionKeyboard(qty10, qty20, qty30, qty50, back, menu));
    }

    private void handleQuantitySelected(TelegramClient client, Long chatId, Long userId, String data) {
        int quantity = quantityFromCallback(data);
        UserContext ctx = userContextService.getOrCreate(userId);
        ctx.setQuantity(quantity);
        userContextService.save(ctx);
        userStateService.setState(userId, UserState.AWAITING_CONFIRMATION);

        String confirmTitle = messageService.getText(GeneratorMessageKeys.CONFIRM_TITLE);
        String generate = messageService.getText(GeneratorMessageKeys.BUTTON_GENERATE_PDF);
        String back = messageService.getText(GeneratorMessageKeys.BUTTON_BACK);
        String menu = messageService.getText(GeneratorMessageKeys.BUTTON_MENU);

        messageService.sendFromKey(client, chatId, GeneratorMessageKeys.CONFIRM_TITLE,
                GeneratorKeyboardFactory.confirmationKeyboard(generate, back, menu),
                confirmTitle);
    }

    private void handleConfirmPdf(TelegramClient client, Long chatId, Long userId) {
        UserContext ctx = userContextService.getOrCreate(userId);
        OperationType operationType;
        try {
            operationType = OperationType.valueOf(ctx.getOperationType());
        } catch (Exception e) {
            messageService.sendFromKey(client, chatId, GeneratorMessageKeys.PDF_GENERATION_ERROR);
            userStateService.setState(userId, UserState.ERROR);
            return;
        }
        int quantity = ctx.getQuantity() != null ? ctx.getQuantity() : 20;

        userStateService.setState(userId, UserState.GENERATING);

        String operationLabel = resolveOperationLabel(operationType);
        String title = "Арифметика: " + operationLabel;

        try {
            byte[] pdf = pdfGenerationService.generateArithmeticPdf(userId, operationType, quantity, title);

            org.telegram.telegrambots.meta.api.objects.InputFile inputFile =
                    new org.telegram.telegrambots.meta.api.objects.InputFile(
                            new java.io.ByteArrayInputStream(pdf),
                            "math_tasks.pdf");

            org.telegram.telegrambots.meta.api.methods.send.SendDocument sendDocument =
                    org.telegram.telegrambots.meta.api.methods.send.SendDocument.builder()
                            .chatId(chatId.toString())
                            .document(inputFile)
                            .build();

            client.execute(sendDocument);
            userStateService.setState(userId, UserState.COMPLETED);
        } catch (PdfGenerationAccessException e) {
            messageService.sendText(client, chatId, e.getMessage());
            userStateService.setState(userId, UserState.COMPLETED);
        } catch (Exception e) {
            messageService.sendFromKey(client, chatId, GeneratorMessageKeys.PDF_GENERATION_ERROR);
            userStateService.setState(userId, UserState.ERROR);
        }
    }

    private static int quantityFromCallback(String data) {
        return switch (data) {
            case CallbackData.QTY_10 -> 10;
            case CallbackData.QTY_20 -> 20;
            case CallbackData.QTY_30 -> 30;
            case CallbackData.QTY_50 -> 50;
            default -> 20;
        };
    }

    private OperationType mapCallbackToOperationType(String data) {
        return switch (data) {
            case CallbackData.OP_ADDITION_10 -> OperationType.ADDITION_10;
            case CallbackData.OP_SUBTRACTION_10 -> OperationType.SUBTRACTION_10;
            case CallbackData.OP_ADDITION_20_NO_CARRY -> OperationType.ADDITION_20_NO_CARRY;
            case CallbackData.OP_SUBTRACTION_20_NO_CARRY -> OperationType.SUBTRACTION_20_NO_CARRY;
            default -> throw new IllegalArgumentException("Unknown operation callback: " + data);
        };
    }

    private String resolveOperationLabel(OperationType operationType) {
        return switch (operationType) {
            case ADDITION_10 -> messageService.getText(GeneratorMessageKeys.OPERATION_ADDITION_10);
            case SUBTRACTION_10 -> messageService.getText(GeneratorMessageKeys.OPERATION_SUBTRACTION_10);
            case ADDITION_20_NO_CARRY -> messageService.getText(GeneratorMessageKeys.OPERATION_ADDITION_20_NO_CARRY);
            case SUBTRACTION_20_NO_CARRY -> messageService.getText(GeneratorMessageKeys.OPERATION_SUBTRACTION_20_NO_CARRY);
        };
    }

    private void handleDemoPdf(TelegramClient client, Long chatId, Long userId) {
        userStateService.setState(userId, UserState.GENERATING);
        String title = messageService.getText(GeneratorMessageKeys.PDF_DEMO_TITLE);
        try {
            byte[] pdf = pdfGenerationService.generateDemoForUser(userId, title);

            org.telegram.telegrambots.meta.api.objects.InputFile inputFile =
                    new org.telegram.telegrambots.meta.api.objects.InputFile(
                            new java.io.ByteArrayInputStream(pdf),
                            "math_tasks_demo.pdf");

            org.telegram.telegrambots.meta.api.methods.send.SendDocument sendDocument =
                    org.telegram.telegrambots.meta.api.methods.send.SendDocument.builder()
                            .chatId(chatId.toString())
                            .document(inputFile)
                            .build();

            client.execute(sendDocument);
            userStateService.setState(userId, UserState.COMPLETED);
        } catch (PdfGenerationAccessException e) {
            messageService.sendText(client, chatId, e.getMessage());
            userStateService.setState(userId, UserState.COMPLETED);
        } catch (Exception e) {
            messageService.sendFromKey(client, chatId, GeneratorMessageKeys.PDF_GENERATION_ERROR);
            userStateService.setState(userId, UserState.ERROR);
        }
    }
}
