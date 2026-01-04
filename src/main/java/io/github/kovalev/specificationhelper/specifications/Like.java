package io.github.kovalev.specificationhelper.specifications;


import io.github.kovalev.specificationhelper.enums.LikeMatchMode;
import io.github.kovalev.specificationhelper.utils.CheckValue;
import io.github.kovalev.specificationhelper.utils.ExpressionUtils;
import io.github.kovalev.specificationhelper.utils.FieldsParser;
import io.github.kovalev.specificationhelper.utils.PathCalculator;
import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

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
public final class Like<E> implements CustomSpecification<E> {

    private static final boolean DEFAULT_IGNORE_CASE = false;

    private final String value;
    private final String[] fields;
    private final boolean ignoreCase;
    private final LikeMatchMode likeMatchMode;

    /**
     * Конструктор со значением и полями.
     * Использует режим {@link LikeMatchMode#BOTH} и регистр не игнорируется.
     *
     * @param value  значение для поиска; может быть {@code null}
     * @param fields имена полей сущности; не может быть {@code null}
     */
    public Like(@NonNull String fields, String value) {
        this(fields, value, LikeMatchMode.BOTH, DEFAULT_IGNORE_CASE);
    }

    /**
     * Конструктор со значением, флагом игнорирования регистра и полями.
     *
     * @param value      значение для поиска; может быть {@code null}
     * @param ignoreCase если {@code true}, поиск игнорирует регистр
     * @param fields     имена полей сущности; не может быть {@code null}
     */
    public Like(@NonNull String fields, String value, boolean ignoreCase) {
        this(fields, value, LikeMatchMode.BOTH, ignoreCase);
    }

    /**
     * Конструктор со значением, режимом шаблона и полями.
     *
     * @param value         значение для поиска; может быть {@code null}
     * @param likeMatchMode режим добавления подстановочных символов
     * @param fields        имена полей сущности; не может быть {@code null}
     */
    public Like(@NonNull String fields, String value, @NonNull LikeMatchMode likeMatchMode) {
        this(fields, value, likeMatchMode, DEFAULT_IGNORE_CASE);
    }

    /**
     * Полный конструктор.
     *
     * @param value         значение для поиска; может быть {@code null}
     * @param likeMatchMode режим добавления подстановочных символов
     * @param ignoreCase    если {@code true}, поиск игнорирует регистр
     * @param fields        имена полей сущности; не может быть {@code null}
     */
    public Like(@NonNull String fields, String value, @NonNull LikeMatchMode likeMatchMode, boolean ignoreCase) {
        this.value = value;
        this.likeMatchMode = likeMatchMode;
        this.ignoreCase = ignoreCase;
        this.fields = new FieldsParser(fields).parse();
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
        if (new CheckValue(value).isNull()) {
            return new Empty<>();
        }

        return (root, query, cb) -> {
            Path<String> path = new PathCalculator<E, String>(root, fields).path();

            String valueWithPattern = switch (likeMatchMode) {
                case BOTH -> "%" + value + "%";
                case START_ONLY -> "%" + value;
                case END_ONLY -> value + "%";
                case NONE -> value;
            };

            if (ignoreCase) {
                return cb.like(ExpressionUtils.toLower(cb, path), valueWithPattern.toLowerCase(), '\\');
            }

            return cb.like(path, valueWithPattern, '\\');
        };
    }
}
