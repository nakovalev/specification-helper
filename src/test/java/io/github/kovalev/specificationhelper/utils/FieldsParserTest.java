package io.github.kovalev.specificationhelper.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FieldsParserTest {

    @Test
    void shouldThrowExceptionWhenFieldsIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new FieldsParser(null).parse());
        assertEquals("fields is null or blank", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenFieldsIsEmpty() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new FieldsParser("").parse());
        assertEquals("fields is null or blank", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenFieldsIsBlank() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new FieldsParser("   ").parse());
        assertEquals("fields is null or blank", exception.getMessage());
    }

    @Test
    void shouldSplitSingleFieldByDot() {
        String[] result = new FieldsParser("order.item.price").parse();
        assertArrayEquals(new String[]{"order", "item", "price"}, result);
    }


    @Test
    void shouldNotSplitSingleFieldWithoutDot() {
        String[] result = new FieldsParser("status").parse();
        assertArrayEquals(new String[]{"status"}, result);
    }
}