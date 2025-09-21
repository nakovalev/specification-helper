package io.github.kovalev.specificationhelper.specifications;


import io.github.kovalev.specificationhelper.utils.CheckValue;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Objects;

/**
 * Спецификация "BETWEEN" для поиска значений в диапазоне.
 *
 * <p>Возвращает сущности, у которых значения поля находятся между {@code from} и {@code to}.</p>
 *
 * <p><b>Особенности:</b></p>
 * <ul>
 *     <li>Если одно из значений {@code null}, используется {@link GreaterThanOrEqualTo} или {@link LessThanOrEqualTo}.</li>
 *     <li>Если оба значения {@code null}, возвращается пустая спецификация {@link Empty}.</li>
 * </ul>
 *
 * @param <E> тип сущности
 * @param <C> тип значения для сравнения
 */
public class Between<E, C extends Comparable<? super C>>
        implements CustomSpecification<E> {

    private final transient List<C> values;
    private final String fields;

    /**
     * Конструктор.
     *
     * @param values список значений диапазона: {@code values.get(0)} = from, {@code values.get(1)} = to
     * @param fields имена полей сущности, для которых применяется диапазон; не может быть {@code null}
     */
    public Between(@NonNull String fields, List<C> values) {
        this.values = values;
        this.fields = fields;
    }

    /**
     * Возвращает спецификацию "BETWEEN" для JPA Criteria API.
     *
     * <p>Если список {@code values} пуст или содержит только {@code null}, возвращается пустая спецификация {@link Empty}.</p>
     *
     * @return спецификация JPA Criteria API для условия BETWEEN
     */
    @Override
    public Specification<E> specification() {
        if (new CheckValue(values).nonNull()) {
            if (values.size() < 2 && values.stream().allMatch(Objects::isNull)) {
                return new Empty<>();
            }

            C from = values.get(0);
            C to = values.get(1);

            if (from != null && to == null) {
                return new GreaterThanOrEqualTo<E, C>(fields, from).specification();
            } else if (from == null && to != null) {
                return new LessThanOrEqualTo<E, C>(fields, to).specification();
            }

            return new And<E>(new GreaterThanOrEqualTo<>(fields, from), new LessThanOrEqualTo<>(fields, to))
                    .specification();
        }

        return new Empty<>();
    }
}

