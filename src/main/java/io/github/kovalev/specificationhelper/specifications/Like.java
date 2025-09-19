package io.github.kovalev.specificationhelper.specifications;


import io.github.kovalev.specificationhelper.enums.LikeMatchMode;
import io.github.kovalev.specificationhelper.utils.CheckValue;
import io.github.kovalev.specificationhelper.utils.Expressions;
import io.github.kovalev.specificationhelper.utils.FieldsParser;
import io.github.kovalev.specificationhelper.utils.PathCalculator;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

import java.util.Objects;

/**
 * Спецификация "LIKE" для поиска строковых значений с шаблонами.
 *
 * <p>Поддерживает игнорирование регистра ({@code ignoreCase}) и режим подстановки символов
 * ({@link LikeMatchMode}).</p>
 *
 * <p>Если значение {@code null}, возвращается пустая спецификация {@link Empty}.</p>
 *
 * @param <E> тип сущности
 */
public class Like<E> implements CustomSpecification<E> {

    private static final boolean DEFAULT_IGNORE_CASE = false;

    private final transient Object value;
    private final String[] fields;
    private final boolean ignoreCase;
    private final LikeMatchMode likeMatchMode;
    private final transient Expressions expressions;

    /**
     * Конструктор со значением и полями.
     * Использует режим {@link LikeMatchMode#BOTH} и регистр не игнорируется.
     *
     * @param value  значение для поиска; может быть {@code null}
     * @param fields имена полей сущности; не может быть {@code null}
     */
    public Like(Object value, @NonNull String... fields) {
        this(value, LikeMatchMode.BOTH, DEFAULT_IGNORE_CASE, fields);
    }

    /**
     * Конструктор со значением, флагом игнорирования регистра и полями.
     *
     * @param value      значение для поиска; может быть {@code null}
     * @param ignoreCase если {@code true}, поиск игнорирует регистр
     * @param fields     имена полей сущности; не может быть {@code null}
     */
    public Like(Object value, boolean ignoreCase, @NonNull String... fields) {
        this(value, LikeMatchMode.BOTH, ignoreCase, fields);
    }

    /**
     * Конструктор со значением, режимом шаблона и полями.
     *
     * @param value         значение для поиска; может быть {@code null}
     * @param likeMatchMode режим добавления подстановочных символов
     * @param fields        имена полей сущности; не может быть {@code null}
     */
    public Like(Object value, @NonNull LikeMatchMode likeMatchMode, @NonNull String... fields) {
        this(value, likeMatchMode, DEFAULT_IGNORE_CASE, fields);
    }

    /**
     * Полный конструктор.
     *
     * @param value         значение для поиска; может быть {@code null}
     * @param likeMatchMode режим добавления подстановочных символов
     * @param ignoreCase    если {@code true}, поиск игнорирует регистр
     * @param fields        имена полей сущности; не может быть {@code null}
     */
    public Like(Object value, @NonNull LikeMatchMode likeMatchMode, boolean ignoreCase, @NonNull String... fields) {
        this.value = value;
        this.likeMatchMode = Objects.requireNonNull(likeMatchMode);
        this.ignoreCase = ignoreCase;
        this.fields = new FieldsParser().parse(fields);
        this.expressions = new Expressions();
    }

    /**
     * Возвращает спецификацию "LIKE" для JPA Criteria API.
     *
     * <p>Если {@code value} не {@code null}, создаётся предикат {@code LIKE} с учётом
     * режима шаблона и игнорирования регистра. В противном случае возвращается пустая
     * спецификация {@link Empty}.</p>
     *
     * @return спецификация JPA Criteria API для условия LIKE
     */
    @Override
    public Specification<E> specification() {
        if (new CheckValue(value).nonNull()) {
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

    /**
     * Применяет подстановочные символы к значению в зависимости от {@link LikeMatchMode}.
     *
     * @param value исходное значение
     * @return значение с подстановочными символами
     */
    private String applyWildcards(String value) {
        return switch (likeMatchMode) {
            case BOTH -> "%" + value + "%";
            case START_ONLY -> "%" + value;
            case END_ONLY -> value + "%";
            case NONE -> value;
        };
    }
}
