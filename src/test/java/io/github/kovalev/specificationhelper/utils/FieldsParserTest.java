package io.github.kovalev.specificationhelper.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FieldsParserTest {

    FieldsParser parser = new FieldsParser();

    @Test
    void shouldThrowExceptionWhenFieldsIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> parser.parse(null));
        assertEquals("fields is null or blank", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenFieldsIsEmpty() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> parser.parse(""));
        assertEquals("fields is null or blank", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenFieldsIsBlank() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> parser.parse("   "));
        assertEquals("fields is null or blank", exception.getMessage());
    }

    @Test
    void shouldSplitSingleFieldByDot() {
        String[] result = parser.parse("order.item.price");
        assertArrayEquals(new String[]{"order", "item", "price"}, result);
    }


    @Test
    void shouldNotSplitSingleFieldWithoutDot() {
        String[] result = parser.parse("status");
        assertArrayEquals(new String[]{"status"}, result);
    }
}