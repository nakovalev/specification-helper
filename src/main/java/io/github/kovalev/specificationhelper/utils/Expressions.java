package io.github.kovalev.specificationhelper.utils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;

import java.time.LocalDate;
import java.util.Objects;

public final class Expressions {

    public Expression<?> get(CriteriaBuilder cb, Expression<?> path, Object value) {
        if (Objects.requireNonNull(value) instanceof LocalDate) {
            return cb.function("DATE", LocalDate.class, path);
        }

        return path;
    }

    public Expression<String> toLower(CriteriaBuilder cb, Expression<?> path) {
        return cb.lower(path.as(String.class));
    }
}
