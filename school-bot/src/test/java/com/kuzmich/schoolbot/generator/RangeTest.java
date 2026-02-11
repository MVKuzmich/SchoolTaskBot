package com.kuzmich.schoolbot.generator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Тесты {@link Range}.
 */
@DisplayName("Range")
class RangeTest {

    @Test
    @DisplayName("создаёт диапазон с min и max")
    void createsRangeWithMinMax() {
        Range r = new Range(0, 10);
        assertThat(r.min()).isZero();
        assertThat(r.max()).isEqualTo(10);
    }

    @Test
    @DisplayName("выбрасывает исключение при min > max")
    void throwsWhenMinGreaterThanMax() {
        assertThatThrownBy(() -> new Range(10, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("min");
    }
}
