package io.github.kovalev.specificationhelper.utils;

public class FieldsParser {

    public String[] parse(String fields) {
        if (fields == null || fields.isBlank()) {
            throw new IllegalArgumentException("fields is null or blank");
        }

        if (fields.contains(".")) {
            return fields.split("\\.");
        }

        return new String[]{fields};
    }
}
