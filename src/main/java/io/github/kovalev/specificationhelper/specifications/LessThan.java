package io.github.kovalev.specificationhelper.specifications;

import io.github.kovalev.specificationhelper.utils.CheckValue;
import io.github.kovalev.specificationhelper.utils.Expressions;
import io.github.kovalev.specificationhelper.utils.FieldsParser;
import io.github.kovalev.specificationhelper.utils.PathCalculator;
import jakarta.persistence.criteria.Expression;
import lombok.val;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

/**
 * Спецификация для сравнения значений полей с использованием оператора {@code <}.
 *
 * <p>Возвращает сущности, у которых значение поля строго меньше заданного.</p>
 *
 * <p><b>Особенности:</b></p>
 * <ul>
 *     <li>Если {@code value} равен {@code null}, возвращается пустая спецификация {@link Empty}.</li>
 *     <li>Поддерживаются любые типы, реализующие {@link Comparable}.</li>
 * </ul>
 *
 * @param <E> тип сущности
 * @param <C> тип значения для сравнения
 */
public class LessThan<E, C extends Comparable<? super C>>
        implements CustomSpecification<E> {

    private final transient C value;
    private final String[] fields;

    /**
     * Конструктор.
     *
     * @param fields имена полей сущности, к которым применяется условие; не может быть {@code null}
     * @param value  значение для сравнения; если {@code null}, спецификация будет пустой
     */
    public LessThan(@NonNull String fields, C value) {
        this.value = value;
        this.fields = new FieldsParser().parse(fields);
    }

    /**
     * Возвращает спецификацию "LESS THAN" для JPA Criteria API.
     *
     * <p>Если {@code value} не {@code null}, создаётся предикат {@code lessThan} по указанным полям.
     * В противном случае возвращается пустая спецификация {@link Empty}.</p>
     *
     * @return спецификация JPA Criteria API для условия lessThan
     */
    @Override
    public Specification<E> specification() {
        if (new CheckValue(value).nonNull()) {
            return (root, query, cb) -> {
                val path = new PathCalculator<E, C>(root, fields).path();
                val expression = (Expression<C>) new Expressions().get(cb, path, value);
                return cb.lessThan(expression, value);
            };
        }

        return new Empty<>();
    }
}