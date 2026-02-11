package com.kuzmich.schoolbot.i18n;

/**
 * Ключи сообщений при проверке доступа к фичам (GATE/QUOTA).
 * Значения берутся из messages*.properties через MessageService.
 */
public final class FeatureAccessMessageKeys {

    private FeatureAccessMessageKeys() {
    }

    /** Сообщение при отказе по GATE (фича только для Premium). */
    public static final String GATE_DISABLED = "feature.access.gate.disabled";

    /** Сообщение при исчерпании квоты. */
    public static final String QUOTA_EXCEEDED = "feature.access.quota.exceeded";
}
