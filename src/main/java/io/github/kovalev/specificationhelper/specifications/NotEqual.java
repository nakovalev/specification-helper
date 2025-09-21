package io.github.kovalev.specificationhelper.specifications;


import io.github.kovalev.specificationhelper.enums.NullHandling;
import io.github.kovalev.specificationhelper.utils.Expressions;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.lang.NonNull;

/**
 * Спецификация для проверки (не)равенства значений полей.
 *
 * <p>Наследует все особенности сравнения из {@link BaseComparisonSpecification},
 * включая ограничения точности для временных типов.</p>
 *
 * <p><b>Особенности реализации:</b></p>
 * <ul>
 *     <li>Генерирует SQL-условие с оператором {@code <>}.</li>
 *     <li>Поддерживает регистронезависимое сравнение строк (при {@code ignoreCase = true}).</li>
 *     <li>Обработка {@code null}-значений согласно {@link NullHandling}.</li>
 * </ul>
 *
 * @param <E> тип сущности
 */
public class NotEqual<E> extends BaseComparisonSpecification<E> {

    public NotEqual(@NonNull String fields, Object value) {
        super(fields, value, NullHandling.IGNORE, DEFAULT_IGNORE_CASE);
    }

    public NotEqual(@NonNull String fields,Object value, boolean ignoreCase) {
        super(fields, value, NullHandling.IGNORE, ignoreCase);
    }

    public NotEqual(@NonNull String fields,Object value, @NonNull NullHandling nullHandling) {
        super(fields, value, nullHandling, DEFAULT_IGNORE_CASE);
    }

    public NotEqual(@NonNull String fields,Object value, @NonNull NullHandling nullHandling, boolean ignoreCase) {
        super(fields, value, nullHandling, ignoreCase);
    }

    @Override
    protected Predicate createPredicate(CriteriaBuilder cb, Expression<?> expression, Object value) {
        return cb.notEqual(expression, value);
    }

    @Override
    protected Predicate resolveCase(CriteriaBuilder cb, Path<Object> path, Expressions expressions, String str) {
        return ignoreCase
                ? cb.notEqual(expressions.toLower(cb, path), str.toLowerCase())
                : cb.notEqual(path, str);
    }

    @Override
    protected Predicate handleNull(CriteriaBuilder cb, Path<Object> path) {
        return switch (nullHandling) {
            case IGNORE -> null;
            case USE_IS_NULL -> cb.isNotNull(path);
        };
    }
}
