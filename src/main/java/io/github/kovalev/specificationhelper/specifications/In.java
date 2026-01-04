package io.github.kovalev.specificationhelper.specifications;


import io.github.kovalev.specificationhelper.utils.CheckValue;
import io.github.kovalev.specificationhelper.utils.FieldsParser;
import io.github.kovalev.specificationhelper.utils.PathCalculator;
import lombok.val;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

import java.util.Collection;

/**
 * Спецификация "IN" для поиска значений в указанном наборе полей.
 *
 * <p>Возвращает все сущности, у которых хотя бы одно из указанных полей
 * содержит значение из коллекции {@code values}.</p>
 *
 * <p>Если коллекция {@code values} пустая или {@code null}, возвращается пустая спецификация {@link Empty}.</p>
 *
 * @param <E> тип сущности
 * @param <I> тип значения для сравнения (элементы коллекции)
 */
public final class In<E, I> implements CustomSpecification<E> {

    private final Collection<I> values;
    private final String[] fields;

    /**
     * Конструктор.
     *
     * @param values коллекция значений для поиска; может быть {@code null} или пустой
     * @param fields имена полей сущности, по которым выполняется поиск; не может быть {@code null}
     */
    public In(@NonNull String fields, Collection<I> values) {
        this.values = values;
        this.fields = new FieldsParser(fields).parse();
    }

    /**
     * Возвращает спецификацию "IN" для JPA Criteria API.
     *
     * <p>Если коллекция {@code values} не пуста, создаётся предикат {@code IN} по указанным полям.
     * В противном случае возвращается пустая спецификация {@link Empty}.</p>
     *
     * @return спецификация JPA Criteria API для условия IN
     */
    @Override
    public Specification<E> specification() {
        if (new CheckValue(values).isNull()) {
            return new Empty<>();
        }

        return (root, query, cb) -> {
            val path = new PathCalculator<E, Collection<I>>(root, fields).path();
            return cb.in(path).value(values);
        };
    }
}
