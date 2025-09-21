package io.github.kovalev.specificationhelper.specifications;


import io.github.kovalev.specificationhelper.utils.FieldsParser;
import io.github.kovalev.specificationhelper.utils.PathCalculator;
import lombok.val;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

/**
 * Спецификация "IS NULL" для проверки отсутствия значения в поле.
 *
 * <p>Возвращает сущности, у которых указанное поле {@code null}.</p>
 *
 * @param <E> тип сущности
 */
public class IsNull<E> implements CustomSpecification<E> {

    private final String[] fields;

    /**
     * Конструктор.
     *
     * @param fields имена полей сущности, для которых проверяется значение {@code null}; не может быть {@code null}
     */
    public IsNull(@NonNull String fields) {
        this.fields = new FieldsParser().parse(fields);
    }

    /**
     * Возвращает спецификацию "IS NULL" для JPA Criteria API.
     *
     * @return спецификация JPA Criteria API для проверки значений {@code null}
     */
    @Override
    public Specification<E> specification() {
        return (root, query, cb) -> {
            val path = new PathCalculator<>(root, fields).path();
            return cb.isNull(path);
        };
    }
}