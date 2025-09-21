package io.github.kovalev.specificationhelper.specifications;


import io.github.kovalev.specificationhelper.utils.FieldsParser;
import io.github.kovalev.specificationhelper.utils.PathCalculator;
import lombok.val;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

/**
 * Спецификация "IS NOT NULL" для проверки наличия значения в поле.
 *
 * <p>Возвращает сущности, у которых указанное поле не {@code null}.</p>
 *
 * @param <E> тип сущности
 */
public class IsNotNull<E> implements CustomSpecification<E> {

    private final String[] fields;

    /**
     * Конструктор.
     *
     * @param fields имена полей сущности, для которых проверяется ненулевое значение; не может быть {@code null}
     */
    public IsNotNull(@NonNull String fields) {
        this.fields = new FieldsParser().parse(fields);
    }

    /**
     * Возвращает спецификацию "IS NOT NULL" для JPA Criteria API.
     *
     * @return спецификация JPA Criteria API для проверки ненулевых значений
     */
    @Override
    public Specification<E> specification() {
        return (root, query, cb) -> {
            val path = new PathCalculator<>(root, fields).path();
            return cb.isNotNull(path);
        };
    }
}
