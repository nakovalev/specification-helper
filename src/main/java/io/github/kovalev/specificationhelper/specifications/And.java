package io.github.kovalev.specificationhelper.specifications;


import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Спецификация "AND" для объединения нескольких условий.
 *
 * <p>Возвращает сущности, которые удовлетворяют всем вложенным спецификациям.</p>
 *
 * <p><b>Особенности:</b></p>
 * <ul>
 *     <li>Поддерживает добавление спецификаций через метод {@link #add(Specification)}.</li>
 *     <li>Если список спецификаций пуст, возвращается пустая спецификация {@link Empty}.</li>
 * </ul>
 *
 * @param <E> тип сущности
 */
public class And<E> implements CustomSpecification<E> {

    private final List<Specification<E>> specifications = new ArrayList<>();

    /**
     * Конструктор.
     *
     * @param specifications спецификации
     */
    @SafeVarargs
    public And(@NonNull Specification<E>... specifications) {
        this.specifications.addAll(Arrays.asList(specifications));
    }

    /**
     * Конструктор.
     *
     * @param specifications спецификации
     */
    public And(@NonNull Collection<Specification<E>> specifications) {
        this.specifications.addAll(specifications);
    }

    /**
     * Добавляет спецификацию к текущей спецификации "И".
     *
     * <p>Добавленная спецификация будет объединена с уже существующими через логический оператор AND.</p>
     *
     * @param specification спецификация для добавления; не может быть {@code null}
     * @return текущий объект {@code And<E>} для поддержки цепочки вызовов
     */
    public And<E> add(@NonNull Specification<E> specification) {
        specifications.add(specification);
        return this;
    }

    /**
     * Возвращает спецификацию "И".
     *
     * @return спецификация "И"
     */
    @Override
    public Specification<E> specification() {
        return specifications.stream()
                .reduce(Specification::and)
                .orElseGet(Empty::new);
    }
}
