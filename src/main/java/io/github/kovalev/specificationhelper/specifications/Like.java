package io.github.kovalev.specificationhelper.specifications;


import io.github.kovalev.specificationhelper.enums.LikeMatchMode;
import io.github.kovalev.specificationhelper.utils.CheckParams;
import io.github.kovalev.specificationhelper.utils.Expressions;
import io.github.kovalev.specificationhelper.utils.PathCalculator;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

import java.util.Objects;

public class Like<E> implements CustomSpecification<E> {

    private static final boolean DEFAULT_IGNORE_CASE = false;

    private final transient Object value;
    private final String[] fields;
    private final boolean ignoreCase;
    private final LikeMatchMode likeMatchMode;
    private final transient Expressions expressions;

    public Like(Object value, String... fields) {
        this(value, LikeMatchMode.BOTH, DEFAULT_IGNORE_CASE, fields);
    }

    public Like(Object value, boolean ignoreCase, String... fields) {
        this(value, LikeMatchMode.BOTH, ignoreCase, fields);
    }

    public Like(Object value, @NonNull LikeMatchMode likeMatchMode, String... fields) {
        this(value, likeMatchMode, DEFAULT_IGNORE_CASE, fields);
    }

    public Like(Object value, @NonNull LikeMatchMode likeMatchMode, boolean ignoreCase, String... fields) {
        this.value = value;
        this.likeMatchMode = Objects.requireNonNull(likeMatchMode);
        this.ignoreCase = ignoreCase;
        this.fields = fields;
        this.expressions = new Expressions();
    }

    @Override
    public Specification<E> specification() {
        if (new CheckParams(value, fields).nonNull()) {
            return (root, query, cb) -> {
                Path<Object> path = new PathCalculator<>(root, fields).path();
                Expression<String> stringExpression = expressions.get(cb, path, value).as(String.class);

                String pattern = applyWildcards(String.valueOf(value));

                if (ignoreCase) {
                    return cb.like(expressions.toLower(cb, stringExpression), pattern.toLowerCase(), '\\');
                }

                return cb.like(stringExpression, pattern, '\\');
            };
        }

        return new Empty<>();
    }

    private String applyWildcards(String value) {
        return switch (likeMatchMode) {
            case BOTH -> "%" + value + "%";
            case START_ONLY -> "%" + value;
            case END_ONLY -> value + "%";
            case NONE -> value;
        };
    }
}
