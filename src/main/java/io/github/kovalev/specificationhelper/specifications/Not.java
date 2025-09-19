package io.github.kovalev.specificationhelper.specifications;

import io.github.kovalev.specificationhelper.enums.CompositionMode;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Спецификация "NOT" для инверсии условия вложенной спецификации.
 *
 * <p>Возвращает сущности, которые не удовлетворяют вложенной спецификации.</p>
 *
 * <p><b>Особенности:</b></p>
 * <ul>
 *     <li>Поддерживает добавление спецификации через метод {@link #add(Specification)}.</li>
 *     <li>Если вложенная спецификация пустая, возвращается пустая спецификация {@link Empty}.</li>
 * </ul>
 *
 * @param <E> тип сущности
 */
public class Not<E> implements CustomSpecification<E> {

    private final List<Specification<E>> specifications = new ArrayList<>();
    private final CompositionMode mode;

    /**
     * Конструктор по умолчанию (режим AND).
     *
     * @param specifications спецификации
     */
    @SafeVarargs
    public Not(@NonNull Specification<E>... specifications) {
        this(CompositionMode.AND, specifications);
    }

    /**
     * Конструктор по умолчанию (режим AND).
     *
     * @param specifications спецификации
     */
    public Not(@NonNull Collection<Specification<E>> specifications) {
        this(CompositionMode.AND, specifications);
    }

    /**
     * Конструктор.
     *
     * @param mode           режим объединения спецификаций (AND/OR)
     * @param specifications спецификации
     */
    @SafeVarargs
    public Not(@NonNull CompositionMode mode, @NonNull Specification<E>... specifications) {
        this(mode, Arrays.asList(specifications));
    }

    /**
     * Конструктор.
     *
     * @param mode           режим объединения спецификаций (AND/OR)
     * @param specifications спецификации
     */
    public Not(@NonNull CompositionMode mode, @NonNull Collection<Specification<E>> specifications) {
        this.mode = Objects.requireNonNull(mode);
        this.specifications.addAll(specifications);
    }

    /**
     * Добавляет спецификацию к текущей спецификации "НЕ".
     *
     * <p>Добавленная спецификация будет инвертирована с помощью логического оператора NOT при построении итогового предиката.</p>
     *
     * @param specification спецификация для добавления; не может быть {@code null}
     * @return текущий объект {@code Not<E>} для поддержки цепочки вызовов
     */
    public Not<E> add(@NonNull Specification<E> specification) {
        specifications.add(specification);
        return this;
    }

    /**
     * Возвращает спецификацию "НЕ".
     *
     * @return спецификация "НЕ"
     */
    @Override
    public Specification<E> specification() {
        return specifications.stream()
                .reduce(mode == CompositionMode.AND ? Specification::and : Specification::or)
                .map(this::safeNot)
                .orElseGet(Empty::new);
    }

    private Specification<E> safeNot(Specification<E> spec) {
        return (root, query, cb) -> {
            Predicate inner = spec.toPredicate(root, query, cb);
            return inner != null ? cb.not(inner) : null;
        };
    }
}