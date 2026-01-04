package io.github.kovalev.specificationhelper.utils;


import jakarta.persistence.criteria.*;
import org.springframework.lang.NonNull;

public final class PathCalculator<E, P> {

    private final Root<E> root;
    private final String[] fields;

    public PathCalculator(@NonNull Root<E> root, @NonNull String... fields) {
        this.root = root;
        this.fields = fields;
    }

    public Path<P> path() {
        if (fields.length == 1) {
            return root.get(fields[0]);
        }

        Join<?, ?> join = getOrCreateJoin(root, fields[0]);

        if (fields.length == 2) {
            return join.get(fields[1]);
        }

        for (int i = 1; i < fields.length - 1; i++) {
            join = getOrCreateJoin(join, fields[i]);
        }

        return join.get(fields[fields.length - 1]);
    }

    private <X> Join<X, ?> getOrCreateJoin(From<?, X> rootOrJoin, String field) {
        // rootOrJoin.getFetches - коллекция жадных JOIN. Таких, где fetchType.EAGER
        for (Fetch<X, ?> fetch : rootOrJoin.getFetches()) {
            if (isLeftJoin(field, fetch.getAttribute().getName(), fetch.getJoinType())) {
                return rootOrJoin.join(field, fetch.getJoinType());
            }
        }

        // rootOrJoin.getJoins - коллекция основных JOIN, добавленных в ручную при построении запроса
        for (Join<X, ?> join : rootOrJoin.getJoins()) {
            if (isLeftJoin(field, join.getAttribute().getName(), join.getJoinType())) {
                return join;
            }
        }

        // rootOrJoin.join - создает новый JOIN в коллекции joins
        return rootOrJoin.join(field, JoinType.LEFT);
    }

    private boolean isLeftJoin(String field, String attributeName, JoinType joinType) {
        return field.equals(attributeName) && joinType == JoinType.LEFT;
    }
}
