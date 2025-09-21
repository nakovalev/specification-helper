package io.github.kovalev.specificationhelper.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Objects;

@RequiredArgsConstructor
public class CheckValue {

    private final Object value;

    public boolean nonNull() {
        if (value == null) {
            return false;
        } else if (value instanceof String str) {
            return StringUtils.hasText(str);
        } else if (value instanceof Collection<?> collection) {
            return !collection.isEmpty() && collection.stream().anyMatch(Objects::nonNull);
        }
        return true;
    }
}
