package com.kuzmich.schoolbot.config;

import com.kuzmich.schoolbot.core.handler.callback.CallbackQueryHandler;
import com.kuzmich.schoolbot.core.handler.command.CommandHandler;
import com.kuzmich.schoolbot.core.privacy.AfterConsentHandler;
import com.kuzmich.schoolbot.core.privacy.ConsentGate;
import com.kuzmich.schoolbot.core.privacy.PrivacyCommandHandler;
import com.kuzmich.schoolbot.core.privacy.PrivacyConsentCallbackHandler;
import com.kuzmich.schoolbot.core.service.MessageService;
import com.kuzmich.schoolbot.core.service.PrivacyConsentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Бины для обработки согласия на обработку ПД: ConsentGate, обработчики /privacy и callback «Согласен».
 */
@Configuration
public class SchoolBotPrivacyConfig {

    private static final String PRIVACY_MESSAGE_KEY = "privacy.message";

    @Bean
    public ConsentGate consentGate(PrivacyConsentService privacyConsentService, MessageService messageService) {
        return new ConsentGate(privacyConsentService, messageService);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CallbackQueryHandler privacyConsentCallbackHandler(PrivacyConsentService privacyConsentService,
                                                            AfterConsentHandler afterConsentHandler) {
        return new PrivacyConsentCallbackHandler(privacyConsentService, afterConsentHandler);
    }

    @Bean
    public CommandHandler privacyCommandHandler(PrivacyConsentService privacyConsentService,
                                               MessageService messageService) {
        return new PrivacyCommandHandler(privacyConsentService, messageService, PRIVACY_MESSAGE_KEY);
    }
}
