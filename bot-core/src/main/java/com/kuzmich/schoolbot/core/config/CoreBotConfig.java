package com.kuzmich.schoolbot.core.config;

import com.kuzmich.schoolbot.core.handler.callback.CallbackQueryHandler;
import com.kuzmich.schoolbot.core.handler.callback.CallbackQueryProcessingHandler;
import com.kuzmich.schoolbot.core.handler.command.CommandHandler;
import com.kuzmich.schoolbot.core.handler.command.CommandProcessingHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Конфигурация ядра бота: роутеры команд и callback-запросов.
 * Обработчики (CommandHandler, CallbackQueryHandler) регистрируются в модуле бота —
 * Spring соберёт их в списки и передаст в эти бины.
 */
@Configuration
public class CoreBotConfig {

    @Bean
    public CommandProcessingHandler commandProcessingHandler(List<CommandHandler> handlers) {
        return new CommandProcessingHandler(handlers);
    }

    @Bean
    public CallbackQueryProcessingHandler callbackQueryProcessingHandler(List<CallbackQueryHandler> handlers) {
        return new CallbackQueryProcessingHandler(handlers);
    }
}
