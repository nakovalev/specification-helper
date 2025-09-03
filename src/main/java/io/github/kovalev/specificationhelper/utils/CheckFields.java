package io.github.kovalev.specificationhelper.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.stream.Stream;

@RequiredArgsConstructor
public class CheckFields implements NonNullParams {

    private final String[] fields;

    @Override
    public boolean nonNull() {
        return fields != null && fields.length > 0 && Stream.of(fields).allMatch(StringUtils::hasLength);
    }
}
