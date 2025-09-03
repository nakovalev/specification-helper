package io.github.kovalev.specificationhelper.utils;

public class FieldsParser {

    public String[] parse(String... fields) {
        if (!new CheckFields(fields).nonNull()) {
            throw new IllegalArgumentException("fields is null or empty");
        }

        if (fields.length == 1 && fields[0].contains(".")) {
            return fields[0].split("\\.");
        }

        return fields;
    }
}
