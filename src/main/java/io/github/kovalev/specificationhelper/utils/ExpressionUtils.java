package io.github.kovalev.specificationhelper.utils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExpressionUtils {

    public static Expression<?> expression(CriteriaBuilder cb, Expression<?> path, Object value) {
        if (Objects.requireNonNull(value) instanceof LocalDate) {
            return cb.function("DATE", LocalDate.class, path);
        }

        return path;
    }

    public static Expression<String> toLower(CriteriaBuilder cb, Expression<?> path) {
        return cb.lower(path.as(String.class));
    }
}
