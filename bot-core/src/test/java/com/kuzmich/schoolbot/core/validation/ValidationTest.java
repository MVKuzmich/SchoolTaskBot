package com.kuzmich.schoolbot.core.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit-тесты класса {@link Validation} (docs/standards/validation.md).
 */
class ValidationTest {

    @Nested
    @DisplayName("requireNonNull")
    class RequireNonNull {

        @Test
        @DisplayName("возвращает значение при non-null")
        void returnsValue_whenNotNull() {
            String value = "ok";
            assertThat(Validation.requireNonNull(value, "field")).isSameAs(value);
        }

        @Test
        @DisplayName("выбрасывает ValidationException при null")
        void throws_whenNull() {
            assertThatThrownBy(() -> Validation.requireNonNull(null, "fieldName"))
                    .isInstanceOf(ValidationException.class)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("fieldName")
                    .hasMessageContaining("null");
        }
    }

    @Nested
    @DisplayName("requireNotBlank")
    class RequireNotBlank {

        @Test
        @DisplayName("возвращает строку при непустой")
        void returnsValue_whenNotBlank() {
            String value = "text";
            assertThat(Validation.requireNotBlank(value, "field")).isSameAs(value);
        }

        @Test
        @DisplayName("выбрасывает ValidationException при null")
        void throws_whenNull() {
            assertThatThrownBy(() -> Validation.requireNotBlank(null, "fieldName"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("fieldName");
        }

        @Test
        @DisplayName("выбрасывает ValidationException при пустой строке")
        void throws_whenEmpty() {
            assertThatThrownBy(() -> Validation.requireNotBlank("", "fieldName"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("fieldName");
        }

        @Test
        @DisplayName("выбрасывает ValidationException при только пробелы")
        void throws_whenBlank() {
            assertThatThrownBy(() -> Validation.requireNotBlank("   ", "fieldName"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("fieldName");
        }
    }

    @Nested
    @DisplayName("requirePositiveInt")
    class RequirePositiveInt {

        @Test
        @DisplayName("возвращает значение при положительном")
        void returnsValue_whenPositive() {
            assertThat(Validation.requirePositiveInt(1, "field")).isEqualTo(1);
            assertThat(Validation.requirePositiveInt(100, "field")).isEqualTo(100);
        }

        @Test
        @DisplayName("выбрасывает ValidationException при 0")
        void throws_whenZero() {
            assertThatThrownBy(() -> Validation.requirePositiveInt(0, "fieldName"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("fieldName")
                    .hasMessageContaining("положительным");
        }

        @Test
        @DisplayName("выбрасывает ValidationException при отрицательном")
        void throws_whenNegative() {
            assertThatThrownBy(() -> Validation.requirePositiveInt(-1, "fieldName"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("fieldName")
                    .hasMessageContaining("положительным");
        }
    }

    @Nested
    @DisplayName("requireInRange")
    class RequireInRange {

        @Test
        @DisplayName("возвращает значение в диапазоне [min, max]")
        void returnsValue_whenInRange() {
            assertThat(Validation.requireInRange(1, 1, 20, "field")).isEqualTo(1);
            assertThat(Validation.requireInRange(20, 1, 20, "field")).isEqualTo(20);
            assertThat(Validation.requireInRange(10, 1, 20, "field")).isEqualTo(10);
        }

        @Test
        @DisplayName("выбрасывает ValidationException при value < min")
        void throws_whenBelowMin() {
            assertThatThrownBy(() -> Validation.requireInRange(0, 1, 20, "fieldName"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("fieldName")
                    .hasMessageContaining("1")
                    .hasMessageContaining("20");
        }

        @Test
        @DisplayName("выбрасывает ValidationException при value > max")
        void throws_whenAboveMax() {
            assertThatThrownBy(() -> Validation.requireInRange(21, 1, 20, "fieldName"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("fieldName")
                    .hasMessageContaining("1")
                    .hasMessageContaining("20");
        }
    }

    @Nested
    @DisplayName("requirePositiveLong")
    class RequirePositiveLong {

        @Test
        @DisplayName("возвращает значение при положительном Long")
        void returnsValue_whenPositive() {
            assertThat(Validation.requirePositiveLong(1L, "field")).isEqualTo(1L);
            assertThat(Validation.requirePositiveLong(123L, "field")).isEqualTo(123L);
        }

        @Test
        @DisplayName("выбрасывает ValidationException при null")
        void throws_whenNull() {
            assertThatThrownBy(() -> Validation.requirePositiveLong(null, "fieldName"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("fieldName")
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("выбрасывает ValidationException при 0")
        void throws_whenZero() {
            assertThatThrownBy(() -> Validation.requirePositiveLong(0L, "fieldName"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("fieldName")
                    .hasMessageContaining("положительным");
        }

        @Test
        @DisplayName("выбрасывает ValidationException при отрицательном")
        void throws_whenNegative() {
            assertThatThrownBy(() -> Validation.requirePositiveLong(-1L, "fieldName"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("fieldName")
                    .hasMessageContaining("положительным");
        }
    }

    @Nested
    @DisplayName("requireOneOf")
    class RequireOneOf {

        @Test
        @DisplayName("возвращает trim-значение при совпадении с одним из allowed")
        void returnsTrimmedValue_whenOneOfAllowed() {
            assertThat(Validation.requireOneOf("  /privacy  ", "command", "/privacy")).isEqualTo("/privacy");
            assertThat(Validation.requireOneOf("menu", "callbackData", "mode_gen", "menu")).isEqualTo("menu");
        }

        @Test
        @DisplayName("выбрасывает ValidationException при null")
        void throws_whenNull() {
            assertThatThrownBy(() -> Validation.requireOneOf(null, "fieldName", "a", "b"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("fieldName")
                    .hasMessageContaining("пустым");
        }

        @Test
        @DisplayName("выбрасывает ValidationException при blank")
        void throws_whenBlank() {
            assertThatThrownBy(() -> Validation.requireOneOf("   ", "fieldName", "a", "b"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("fieldName");
        }

        @Test
        @DisplayName("выбрасывает ValidationException при значении не из списка")
        void throws_whenNotInAllowed() {
            assertThatThrownBy(() -> Validation.requireOneOf("unknown", "callbackData", "mode_gen", "menu"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("callbackData")
                    .hasMessageContaining("допустимых")
                    .hasMessageContaining("unknown");
        }
    }

    @Nested
    @DisplayName("requireStartsWith")
    class RequireStartsWith {

        @Test
        @DisplayName("возвращает trim-значение при начале с prefix")
        void returnsTrimmedValue_whenStartsWithPrefix() {
            assertThat(Validation.requireStartsWith("  /start  ", "/start", "command")).isEqualTo("/start");
            assertThat(Validation.requireStartsWith("/start@BotName", "/start", "command")).isEqualTo("/start@BotName");
        }

        @Test
        @DisplayName("выбрасывает ValidationException при null")
        void throws_whenNull() {
            assertThatThrownBy(() -> Validation.requireStartsWith(null, "/start", "fieldName"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("fieldName")
                    .hasMessageContaining("пустым");
        }

        @Test
        @DisplayName("выбрасывает ValidationException при blank")
        void throws_whenBlank() {
            assertThatThrownBy(() -> Validation.requireStartsWith("   ", "/start", "fieldName"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("fieldName");
        }

        @Test
        @DisplayName("выбрасывает ValidationException при отсутствии префикса")
        void throws_whenDoesNotStartWithPrefix() {
            assertThatThrownBy(() -> Validation.requireStartsWith("/privacy", "/start", "command"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("command")
                    .hasMessageContaining("/start")
                    .hasMessageContaining("/privacy");
        }
    }
}
