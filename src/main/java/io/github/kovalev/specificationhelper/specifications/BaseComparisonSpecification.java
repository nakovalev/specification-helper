package io.github.kovalev.specificationhelper.specifications;


import io.github.kovalev.specificationhelper.enums.NullHandling;
import io.github.kovalev.specificationhelper.utils.Expressions;
import io.github.kovalev.specificationhelper.utils.FieldsParser;
import io.github.kovalev.specificationhelper.utils.PathCalculator;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Базовый абстрактный класс для спецификаций сравнения полей.
 *
 * <p>Предоставляет общий механизм построения спецификации для сравнения значений полей сущности
 * с учётом различных типов данных, включая строки и временные типы.</p>
 *
 * <p><b>Особенности работы с временными типами:</b><br>
 * При сравнении типов {@link LocalTime}, {@link LocalDateTime}, {@link Instant},
 * {@link ZonedDateTime}, {@link OffsetDateTime} возможны расхождения из-за различной
 * точности представления времени в Java (наносекунды) и БД (обычно микро- или миллисекунды).
 * Рекомендуется заранее выравнивать точность с помощью {@code truncatedTo()}.
 *
 * <p><b>Пример:</b><br>
 * {@code LocalDateTime truncated = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);}</p>
 *
 * <p><b>Особенности работы со строками:</b><br>
 * Если значение является {@link CharSequence}, поддерживается игнорирование регистра
 * через флаг {@code ignoreCase} и использование вспомогательного класса {@link Expressions} для корректного построения предикатов.</p>
 *
 * @param <E> тип сущности
 */
public abstract class BaseComparisonSpecification<E> implements CustomSpecification<E> {

    protected static final boolean DEFAULT_IGNORE_CASE = false;

    protected final transient Object value;
    protected final String[] fields;
    protected final NullHandling nullHandling;
    protected final boolean ignoreCase;
    protected final transient Expressions expressions;

    /**
     * Конструктор базовой спецификации сравнения.
     *
     * @param value        значение для сравнения; может быть {@code null}
     * @param nullHandling способ обработки {@code null} значений
     * @param ignoreCase   если {@code true}, игнорируется регистр для строк
     * @param fields       имена полей сущности, к которым применяется сравнение; не может быть {@code null}
     */
    protected BaseComparisonSpecification(Object value, @NonNull NullHandling nullHandling,
                                          boolean ignoreCase, @NonNull String... fields) {
        this.value = value;
        this.nullHandling = Objects.requireNonNull(nullHandling);
        this.ignoreCase = ignoreCase;
        this.fields = new FieldsParser().parse(fields);
        this.expressions = new Expressions();
    }

    /**
     * Возвращает JPA {@link Specification} для текущей сравниваемой сущности.
     *
     * <p>Если значение {@code null}, вызывается {@link #handleNull(CriteriaBuilder, Path)}.
     * Если значение является строкой, вызывается {@link #resolveCase(CriteriaBuilder, Path, Expressions, String)}.
     * Для остальных типов создаётся предикат через {@link #createPredicate(CriteriaBuilder, Expression, Object)}.</p>
     *
     * @return спецификация JPA Criteria API
     */
    @Override
    public Specification<E> specification() {
        return (root, query, cb) -> {
            Path<Object> path = new PathCalculator<>(root, fields).path();

            if (value == null) {
                return handleNull(cb, path);
            }

            if (value instanceof CharSequence str) {
                return resolveCase(cb, path, expressions, String.valueOf(str));
            }

            Expression<?> expression = expressions.get(cb, path, value);
            return createPredicate(cb, expression, value);
        };
    }

    /**
     * Создаёт предикат для сравнения значения с полем.
     *
     * @param cb         {@link CriteriaBuilder} для создания предиката
     * @param expression выражение поля
     * @param value      значение для сравнения
     * @return предикат для JPA Criteria API
     */
    protected abstract Predicate createPredicate(CriteriaBuilder cb, Expression<?> expression, Object value);

    /**
     * Обрабатывает случай, когда значение равно {@code null}.
     *
     * @param criteriaBuilder {@link CriteriaBuilder} для создания предиката
     * @param path            путь к полю
     * @return предикат для JPA Criteria API
     */
    protected abstract Predicate handleNull(CriteriaBuilder criteriaBuilder, Path<Object> path);

    /**
     * Обрабатывает случай, когда значение является строкой.
     *
     * @param criteriaBuilder {@link CriteriaBuilder} для создания предиката
     * @param path            путь к полю
     * @param expressions     вспомогательный объект для работы с выражениями
     * @param str             строковое значение
     * @return предикат для JPA Criteria API
     */
    protected abstract Predicate resolveCase(CriteriaBuilder criteriaBuilder, Path<Object> path,
                                             Expressions expressions, String str);
}
