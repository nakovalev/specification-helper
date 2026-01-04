package io.github.kovalev.specificationhelper.utils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class FieldsParser {

    private final String fields;

    public String[] parse() {
        if (fields == null || fields.isBlank()) {
            throw new IllegalArgumentException("fields is null or blank");
        }

        if (fields.contains(".")) {
            return fields.split("\\.");
        }

        return new String[]{fields};
    }
}
