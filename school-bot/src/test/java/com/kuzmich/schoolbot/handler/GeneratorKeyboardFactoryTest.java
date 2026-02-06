package com.kuzmich.schoolbot.handler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-тесты {@link GeneratorKeyboardFactory}: структура клавиатур, подписи кнопок, callbackData.
 */
class GeneratorKeyboardFactoryTest {

    @Test
    @DisplayName("modeSelectionKeyboard: 3 строки, кнопки Генератор, Тренажёр, Справка с правильным callbackData")
    void modeSelectionKeyboard_hasThreeRowsAndCorrectCallbacks() {
        InlineKeyboardMarkup markup = GeneratorKeyboardFactory.modeSelectionKeyboard();

        assertThat(markup.getKeyboard()).hasSize(3);

        List<InlineKeyboardButton> row0 = markup.getKeyboard().get(0);
        assertThat(row0).hasSize(1);
        assertThat(row0.get(0).getText()).contains("Генератор");
        assertThat(row0.get(0).getCallbackData()).isEqualTo(CallbackData.MODE_GENERATOR);

        List<InlineKeyboardButton> row1 = markup.getKeyboard().get(1);
        assertThat(row1.get(0).getText()).contains("Тренажёр");
        assertThat(row1.get(0).getCallbackData()).isEqualTo(CallbackData.MODE_TRAINER);

        List<InlineKeyboardButton> row2 = markup.getKeyboard().get(2);
        assertThat(row2.get(0).getCallbackData()).isEqualTo("help");
    }

    @Test
    @DisplayName("classSelectionKeyboard: Начальная/Средняя школа, Назад, Справка с правильным callbackData")
    void classSelectionKeyboard_hasCorrectButtonsAndCallbacks() {
        InlineKeyboardMarkup markup = GeneratorKeyboardFactory.classSelectionKeyboard("◀️ Назад", "ℹ️ Справка");

        assertThat(markup.getKeyboard()).hasSize(3);
        assertThat(markup.getKeyboard().get(0).get(0).getCallbackData()).isEqualTo(CallbackData.GEN_ELEMENTARY);
        assertThat(markup.getKeyboard().get(1).get(0).getCallbackData()).isEqualTo(CallbackData.GEN_SECONDARY);
        assertThat(markup.getKeyboard().get(2)).hasSize(2);
        assertThat(markup.getKeyboard().get(2).get(0).getCallbackData()).isEqualTo(CallbackData.BACK_TO_MODE);
        assertThat(markup.getKeyboard().get(2).get(1).getCallbackData()).isEqualTo("help");
    }

    @Test
    @DisplayName("subjectSelectionKeyboard: математика, Назад, Главное меню")
    void subjectSelectionKeyboard_hasMathBackAndMenu() {
        InlineKeyboardMarkup markup = GeneratorKeyboardFactory.subjectSelectionKeyboard(
                "Математика", "Назад", "Меню");

        assertThat(markup.getKeyboard()).hasSize(2);
        assertThat(markup.getKeyboard().get(0).get(0).getCallbackData()).isEqualTo(CallbackData.SUBJECT_MATH);
        assertThat(markup.getKeyboard().get(1).get(0).getCallbackData()).isEqualTo(CallbackData.BACK_TO_CLASS);
        assertThat(markup.getKeyboard().get(1).get(1).getCallbackData()).isEqualTo(CallbackData.MENU);
    }

    @Test
    @DisplayName("mainMenuOnlyKeyboard: одна кнопка с callbackData MENU")
    void mainMenuOnlyKeyboard_hasSingleMenuButton() {
        InlineKeyboardMarkup markup = GeneratorKeyboardFactory.mainMenuOnlyKeyboard("Главное меню");

        assertThat(markup.getKeyboard()).hasSize(1);
        assertThat(markup.getKeyboard().get(0)).hasSize(1);
        assertThat(markup.getKeyboard().get(0).get(0).getCallbackData()).isEqualTo(CallbackData.MENU);
    }
}
