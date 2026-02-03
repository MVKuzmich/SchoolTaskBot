package com.kuzmich.schoolbot;

import com.kuzmich.schoolbot.core.bot.AbstractTelegramBot;
import com.kuzmich.schoolbot.core.handler.callback.CallbackQueryProcessingHandler;
import com.kuzmich.schoolbot.core.handler.command.CommandProcessingHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Точка входа приложения SchoolBot.
 * Сканирует пакеты com.kuzmich.schoolbot (в т.ч. core через зависимость bot-core).
 * Регистрация бота выполняется telegrambots-springboot-longpolling-starter (бины типа SpringLongPollingBot).
 */
@SpringBootApplication(scanBasePackages = {"com.kuzmich.schoolbot", "com.kuzmich.schoolbot.core"})
public class SchoolBotApplication {

    @Value("${telegram.bot.token:}")
    private String botToken;

    public static void main(String[] args) {
        SpringApplication.run(SchoolBotApplication.class, args);
    }

    @Bean
    public AbstractTelegramBot schoolBot(
            CommandProcessingHandler commandHandler,
            CallbackQueryProcessingHandler callbackHandler
    ) {
        return new AbstractTelegramBot(commandHandler, callbackHandler) {
            @Override
            public String getBotToken() {
                return botToken;
            }
        };
    }
}
