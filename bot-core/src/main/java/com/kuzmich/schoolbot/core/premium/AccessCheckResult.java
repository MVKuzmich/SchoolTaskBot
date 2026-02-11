package com.kuzmich.schoolbot.core.premium;

import lombok.Builder;
import lombok.Value;

/**
 * Результат проверки доступа к фиче: разрешено/отказ, сообщение для пользователя, остаток по квоте.
 */
@Value
@Builder
public class AccessCheckResult {

    boolean granted;
    String message;
    Integer remaining;  // остаток по квоте (опционально)

    public static AccessCheckResult allowed(Integer remaining) {
        return AccessCheckResult.builder()
                .granted(true)
                .message(null)
                .remaining(remaining)
                .build();
    }

    public static AccessCheckResult denied(String message) {
        return AccessCheckResult.builder()
                .granted(false)
                .message(message)
                .remaining(null)
                .build();
    }
}
