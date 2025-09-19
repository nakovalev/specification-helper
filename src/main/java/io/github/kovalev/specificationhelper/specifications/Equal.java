package io.github.kovalev.specificationhelper.specifications;


import io.github.kovalev.specificationhelper.enums.NullHandling;
import io.github.kovalev.specificationhelper.utils.Expressions;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.lang.NonNull;

/**
 * Спецификация для проверки равенства значений полей.
 *
 * <p>Наследует все особенности сравнения из {@link BaseComparisonSpecification},
 * включая ограничения точности для временных типов.</p>
 *
 * <p><b>Особенности реализации:</b></p>
 * <ul>
 *     <li>Генерирует SQL-условие с оператором {@code =}.</li>
 *     <li>Поддерживает регистронезависимое сравнение строк (при {@code ignoreCase = true}).</li>
 *     <li>Обработка {@code null}-значений согласно {@link NullHandling}.</li>
 * </ul>
 *
 * @param <E> тип сущности
 */
public class Equal<E> extends BaseComparisonSpecification<E> {

    public Equal(Object value, @NonNull String... fields) {
        super(value, NullHandling.IGNORE, DEFAULT_IGNORE_CASE, fields);
    }

    public Equal(Object value, boolean ignoreCase, @NonNull String... fields) {
        super(value, NullHandling.IGNORE, ignoreCase, fields);
    }

    public Equal(Object value, @NonNull NullHandling nullHandling, @NonNull String... fields) {
        super(value, nullHandling, DEFAULT_IGNORE_CASE, fields);
    }

    public Equal(Object value, @NonNull NullHandling nullHandling, boolean ignoreCase, @NonNull String... fields) {
        super(value, nullHandling, ignoreCase, fields);
    }

    @Override
    protected Predicate createPredicate(CriteriaBuilder cb, Expression<?> expression, Object value) {
        return cb.equal(expression, value);
    }

    @Override
    protected Predicate resolveCase(CriteriaBuilder cb, Path<Object> path, Expressions expressions, String str) {
        return ignoreCase
                ? cb.equal(expressions.toLower(cb, path), str.toLowerCase())
                : cb.equal(path, str);
    }

    @Override
    protected Predicate handleNull(CriteriaBuilder cb, Path<Object> path) {
        return switch (nullHandling) {
            case IGNORE -> null;
            case USE_IS_NULL -> cb.isNull(path);
        };
    }
}

