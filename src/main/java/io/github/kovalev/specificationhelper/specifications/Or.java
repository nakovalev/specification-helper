package io.github.kovalev.specificationhelper.specifications;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Спецификация "OR" для объединения нескольких условий.
 *
 * <p>Возвращает сущности, которые удовлетворяют хотя бы одной из вложенных спецификаций.</p>
 *
 * <p><b>Особенности:</b></p>
 * <ul>
 *     <li>Поддерживает добавление спецификаций через метод {@link #add(Specification)}.</li>
 *     <li>Если список спецификаций пуст, возвращается пустая спецификация {@link Empty}.</li>
 * </ul>
 *
 * @param <E> тип сущности
 */
public class Or<E> implements CustomSpecification<E> {

    private final List<Specification<E>> specifications = new ArrayList<>();

    /**
     * Конструктор.
     *
     * @param specifications массив спецификаций для объединения через OR
     */
    @SafeVarargs
    public Or(@NonNull Specification<E>... specifications) {
        this.specifications.addAll(Arrays.asList(specifications));
    }

    /**
     * Конструктор.
     *
     * @param specifications коллекция спецификаций для объединения через OR
     */
    public Or(@NonNull Collection<Specification<E>> specifications) {
        this.specifications.addAll(specifications);
    }

    /**
     * Добавляет спецификацию к текущей спецификации "ИЛИ".
     *
     * <p>Добавленная спецификация будет объединена с уже существующими через логический оператор OR.</p>
     *
     * @param specification спецификация для добавления; не может быть {@code null}
     * @return текущий объект {@code Or<E>} для поддержки цепочки вызовов
     */
    public Or<E> add(@NonNull Specification<E> specification) {
        specifications.add(specification);
        return this;
    }

    /**
     * Возвращает спецификацию "ИЛИ".
     *
     * <p>Если список спецификаций пуст, возвращает пустую спецификацию {@link Empty}.</p>
     *
     * @return спецификация, объединяющая все вложенные спецификации через OR
     */
    @Override
    public Specification<E> specification() {
        return specifications.stream()
                .reduce(Specification::or)
                .orElseGet(Empty::new);
    }
}