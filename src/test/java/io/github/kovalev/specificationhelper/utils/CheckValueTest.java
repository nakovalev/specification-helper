package io.github.kovalev.specificationhelper.utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CheckValueTest {

    @Test
    void nonNull() {
        assertThat(new CheckValue(null).nonNull()).isFalse();
        assertThat(new CheckValue(Integer.MAX_VALUE).nonNull()).isTrue();

        assertThat(new CheckValue("").nonNull()).isFalse();
        assertThat(new CheckValue("any").nonNull()).isTrue();

        assertThat(new CheckValue(List.of()).nonNull()).isFalse();
        assertThat(new CheckValue(List.of(Integer.MAX_VALUE)).nonNull()).isTrue();
        assertThat(new CheckValue(onlyNullValuesList()).nonNull()).isFalse();
        assertThat(new CheckValue(withNullValueList()).nonNull()).isTrue();
    }

    @Test
    void isNull() {
        assertThat(new CheckValue(null).isNull()).isTrue();
        assertThat(new CheckValue(Integer.MAX_VALUE).isNull()).isFalse();

        assertThat(new CheckValue("").isNull()).isTrue();
        assertThat(new CheckValue("any").isNull()).isFalse();

        assertThat(new CheckValue(List.of()).isNull()).isTrue();
        assertThat(new CheckValue(List.of(Integer.MAX_VALUE)).isNull()).isFalse();
        assertThat(new CheckValue(onlyNullValuesList()).isNull()).isTrue();
        assertThat(new CheckValue(withNullValueList()).isNull()).isFalse();
    }

    @Test
    void isNull_isInverseOf_nonNull() {
        Object[] values = {
                null,
                Integer.MAX_VALUE,
                "",
                "any",
                List.of(),
                List.of(Integer.MAX_VALUE),
                onlyNullValuesList(),
                withNullValueList()
        };

        for (Object value : values) {
            CheckValue checkValue = new CheckValue(value);
            assertThat(checkValue.isNull()).isEqualTo(!checkValue.nonNull());
        }
    }

    List<Integer> withNullValueList() {
        return new ArrayList<>() {{
            add(null);
            add(Integer.MAX_VALUE);
            add(Integer.MIN_VALUE);
        }};
    }

    List<Integer> onlyNullValuesList() {
        return new ArrayList<>() {{
            add(null);
        }};
    }
}